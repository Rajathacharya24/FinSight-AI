package com.finsight.auth.service;

import com.finsight.auth.domain.RefreshToken;
import com.finsight.auth.domain.Role;
import com.finsight.auth.domain.User;
import com.finsight.auth.dto.*;
import com.finsight.auth.exception.AuthException;
import com.finsight.auth.repository.RefreshTokenRepository;
import com.finsight.auth.repository.RoleRepository;
import com.finsight.auth.repository.UserRepository;
import com.finsight.auth.security.JwtProvider;
import com.finsight.auth.security.RateLimitationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RateLimitationService rateLimitationService;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtProvider jwtProvider,
                       RateLimitationService rateLimitationService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.rateLimitationService = rateLimitationService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AuthException("Username is already taken!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException("Email is already in use!");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(true)
                .roles(Collections.singleton(userRole))
                .build();

        userRepository.save(user);

        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername(request.getUsername());
        loginReq.setPassword(request.getPassword());
        return login(loginReq);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        if (rateLimitationService.isRateLimited(request.getUsername())) {
            throw new AuthException("Too many failed login attempts. Please try again in 1 minute.", HttpStatus.TOO_MANY_REQUESTS);
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    rateLimitationService.incrementAttempt(request.getUsername());
                    return new AuthException("Invalid username or password", HttpStatus.UNAUTHORIZED);
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            rateLimitationService.incrementAttempt(request.getUsername());
            throw new AuthException("Invalid username or password", HttpStatus.UNAUTHORIZED);
        }

        rateLimitationService.resetAttempt(request.getUsername());

        var roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
        String accessToken = jwtProvider.generateToken(user.getUsername(), user.getId(), roles);
        
        RefreshToken refreshToken = createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AuthException("Invalid refresh token", HttpStatus.UNAUTHORIZED));

        if (refreshToken.isRevoked() || refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new AuthException("Refresh token was expired or revoked", HttpStatus.UNAUTHORIZED);
        }

        User user = refreshToken.getUser();
        var roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
        String newAccessToken = jwtProvider.generateToken(user.getUsername(), user.getId(), roles);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }

    private RefreshToken createRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusSeconds(7 * 24 * 60 * 60)) // 7 days
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    public UserDto getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException("User not found"));
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .enabled(user.getEnabled())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }
}
