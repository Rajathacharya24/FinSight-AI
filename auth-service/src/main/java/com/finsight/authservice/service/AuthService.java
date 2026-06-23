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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RefreshTokenService refreshTokenService;

    /**
     * Registers a new user account.
     *
     * @param request registration payload with username, email, and password
     * @return the persisted User entity
     * @throws UserAlreadyExistsException if username or email is already taken
     */
    @Transactional
    public User register(RegisterRequest request) {
        logger.info("Registering new user: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException(
                    "Username '" + request.getUsername() + "' is already taken.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "Email '" + request.getEmail() + "' is already in use.");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(ERole.ROLE_USER);

        return userRepository.save(user);
    }

    /**
     * Authenticates a user and issues a JWT access token + refresh token.
     *
     * @param request login payload with username/email and password
     * @return JwtResponse containing access token, refresh token, and user info
     */
    @Transactional
    public JwtResponse login(LoginRequest request) {
        logger.info("Login attempt for user: {}", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtUtils.generateJwtToken(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(item -> item.getAuthority())
                .orElse("ROLE_USER");

        logger.info("User '{}' logged in successfully", userDetails.getUsername());

        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .role(role)
                .build();
    }

    /**
     * Issues a new access token given a valid, non-expired refresh token.
     *
     * @param requestRefreshToken the raw refresh token string
     * @return JwtResponse with a fresh access token and the same refresh token
     * @throws TokenRefreshException if the token is unknown or expired
     */
    @Transactional
    public JwtResponse refreshToken(String requestRefreshToken) {
        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    UserDetailsImpl userDetails = UserDetailsImpl.build(user);
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    String newAccessToken = jwtUtils.generateJwtToken(authentication);

                    String role = userDetails.getAuthorities().stream()
                            .findFirst()
                            .map(item -> item.getAuthority())
                            .orElse("ROLE_USER");

                    logger.info("Access token refreshed for user: {}", user.getUsername());

                    return JwtResponse.builder()
                            .accessToken(newAccessToken)
                            .refreshToken(requestRefreshToken)
                            .id(userDetails.getId())
                            .username(userDetails.getUsername())
                            .email(userDetails.getEmail())
                            .role(role)
                            .build();
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token not found in database."));
    }
}
