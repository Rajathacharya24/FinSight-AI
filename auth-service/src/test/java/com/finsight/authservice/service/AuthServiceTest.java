package com.finsight.authservice.service;

import com.finsight.authservice.config.JwtUtils;
import com.finsight.authservice.dto.JwtResponse;
import com.finsight.authservice.dto.LoginRequest;
import com.finsight.authservice.dto.RegisterRequest;
import com.finsight.authservice.exception.TokenRefreshException;
import com.finsight.authservice.exception.UserAlreadyExistsException;
import com.finsight.authservice.model.ERole;
import com.finsight.authservice.model.RefreshToken;
import com.finsight.authservice.model.User;
import com.finsight.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtils jwtUtils;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private Authentication authentication;

    private AuthService authService;

    @BeforeEach
    void setUp() throws Exception {
        authService = new AuthService();
        inject(authService, "userRepository", userRepository);
        inject(authService, "passwordEncoder", passwordEncoder);
        inject(authService, "jwtUtils", jwtUtils);
        inject(authService, "authenticationManager", authenticationManager);
        inject(authService, "refreshTokenService", refreshTokenService);
    }

    private void inject(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void register_shouldCreateUser() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("testuser");
        req.setEmail("test@email.com");
        req.setPassword("password123");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@email.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });

        User result = authService.register(req);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getRole()).isEqualTo(ERole.ROLE_USER);
        verify(userRepository).save(any());
    }

    @Test
    void register_shouldThrowWhenUsernameTaken() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("existing");
        req.setEmail("e@e.com");
        req.setPassword("pass");

        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("already taken");
    }

    @Test
    void register_shouldThrowWhenEmailTaken() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setEmail("taken@email.com");
        req.setPassword("pass");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("taken@email.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("already in use");
    }

    @Test
    void login_shouldReturnTokens() {
        LoginRequest req = new LoginRequest();
        req.setUsername("demo");
        req.setPassword("demo123");

        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getId()).thenReturn(1L);
        when(userDetails.getUsername()).thenReturn("demo");
        when(userDetails.getEmail()).thenReturn("demo@finsight.ai");
        when(userDetails.getAuthorities()).thenReturn((Collection) List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("access-token");

        RefreshToken rt = new RefreshToken();
        rt.setToken("refresh-token");
        when(refreshTokenService.createRefreshToken(1L)).thenReturn(rt);

        JwtResponse response = authService.login(req);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUsername()).isEqualTo("demo");
        assertThat(response.getRole()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void refreshToken_shouldReturnNewAccessToken() {
        User user = new User();
        user.setId(1L);
        user.setUsername("demo");
        user.setEmail("demo@finsight.ai");
        user.setPassword("encoded");
        user.setRole(ERole.ROLE_USER);

        RefreshToken rt = new RefreshToken();
        rt.setToken("valid-refresh-token");
        rt.setUser(user);
        rt.setExpiryDate(Instant.now().plusSeconds(3600));

        when(refreshTokenService.findByToken("valid-refresh-token")).thenReturn(Optional.of(rt));
        when(refreshTokenService.verifyExpiration(rt)).thenReturn(rt);
        when(jwtUtils.generateJwtToken(any())).thenReturn("new-access-token");

        JwtResponse response = authService.refreshToken("valid-refresh-token");

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("valid-refresh-token");
        assertThat(response.getUsername()).isEqualTo("demo");
    }

    @Test
    void refreshToken_shouldThrowWhenNotFound() {
        when(refreshTokenService.findByToken("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken("invalid"))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessageContaining("not found");
    }
}
