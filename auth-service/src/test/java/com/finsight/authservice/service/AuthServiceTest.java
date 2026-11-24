package com.finsight.authservice.service;

import com.finsight.auth.domain.RefreshToken;
import com.finsight.auth.domain.Role;
import com.finsight.auth.domain.User;
import com.finsight.auth.dto.AuthResponse;
import com.finsight.auth.dto.LoginRequest;
import com.finsight.auth.dto.RefreshTokenRequest;
import com.finsight.auth.dto.RegisterRequest;
import com.finsight.auth.exception.AuthException;
import com.finsight.auth.repository.RefreshTokenRepository;
import com.finsight.auth.repository.RoleRepository;
import com.finsight.auth.repository.UserRepository;
import com.finsight.auth.security.JwtProvider;
import com.finsight.auth.security.RateLimitationService;
import com.finsight.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtProvider jwtProvider;
    @Mock private RateLimitationService rateLimitationService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, roleRepository, refreshTokenRepository,
                passwordEncoder, jwtProvider, rateLimitationService);
    }

    private User demoUser() {
        Role role = new Role(1L, "ROLE_USER");
        User user = User.builder()
                .id(1L)
                .username("demo")
                .email("demo@finsight.ai")
                .password("encoded")
                .enabled(true)
                .roles(Set.of(role))
                .build();
        return user;
    }

    private RefreshTokenRequest refreshTokenRequest(String token) {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(token);
        return request;
    }

    private void stubSuccessfulLogin(User user) {
        when(rateLimitationService.isRateLimited(anyString())).thenReturn(false);
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtProvider.generateToken(anyString(), anyLong(), anyList())).thenReturn("access-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken token = invocation.getArgument(0);
            token.setToken("refresh-token");
            return token;
        });
    }

    @Test
    void register_shouldCreateUserAndReturnTokens() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("demo");
        request.setEmail("demo@finsight.ai");
        request.setPassword("demo123");
        request.setFirstName("Demo");
        request.setLastName("User");

        User user = demoUser();

        when(userRepository.existsByUsername("demo")).thenReturn(false);
        when(userRepository.existsByEmail("demo@finsight.ai")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(new Role(1L, "ROLE_USER")));
        when(passwordEncoder.encode("demo123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        stubSuccessfulLogin(user);

        AuthResponse response = authService.register(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUsername()).isEqualTo("demo");
        assertThat(response.getRoles()).contains("ROLE_USER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrowWhenUsernameTaken() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existing");
        request.setEmail("existing@email.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("already taken");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_shouldThrowWhenEmailTaken() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("taken@email.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("taken@email.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("already in use");
    }

    @Test
    void login_shouldReturnTokens() {
        User user = demoUser();
        stubSuccessfulLogin(user);

        LoginRequest request = new LoginRequest("demo", "demo123");
        AuthResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUsername()).isEqualTo("demo");
        assertThat(response.getRoles()).containsExactly("ROLE_USER");
        verify(rateLimitationService).resetAttempt("demo");
    }

    @Test
    void login_shouldRejectBadPasswordAndTrackAttempt() {
        User user = demoUser();
        when(rateLimitationService.isRateLimited("demo")).thenReturn(false);
        when(userRepository.findByUsername("demo")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        LoginRequest request = new LoginRequest("demo", "wrong");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOfSatisfying(AuthException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));

        verify(rateLimitationService).incrementAttempt("demo");
    }

    @Test
    void login_shouldRejectWhenRateLimited() {
        when(rateLimitationService.isRateLimited("demo")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(new LoginRequest("demo", "demo123")))
                .isInstanceOfSatisfying(AuthException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS));

        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void refreshToken_shouldReturnNewAccessToken() {
        User user = demoUser();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token("valid-refresh-token")
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("valid-refresh-token")).thenReturn(Optional.of(refreshToken));
        when(jwtProvider.generateToken(anyString(), anyLong(), anyList())).thenReturn("new-access-token");

        AuthResponse response = authService.refreshToken(refreshTokenRequest("valid-refresh-token"));

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("valid-refresh-token");
        assertThat(response.getUsername()).isEqualTo("demo");
    }

    @Test
    void refreshToken_shouldThrowWhenNotFound() {
        when(refreshTokenRepository.findByToken("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken(refreshTokenRequest("invalid")))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("Invalid refresh token");
    }

    @Test
    void refreshToken_shouldDeleteAndThrowWhenExpired() {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(demoUser())
                .token("expired")
                .expiryDate(Instant.now().minusSeconds(60))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("expired")).thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> authService.refreshToken(refreshTokenRequest("expired")))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("expired or revoked");

        verify(refreshTokenRepository).delete(refreshToken);
    }
}
