package com.finsight.authservice.controller;

import com.finsight.authservice.dto.*;
import com.finsight.authservice.model.User;
import com.finsight.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.finsight.authservice.service.UserDetailsImpl;

import java.util.Map;

@Tag(name = "Authentication", description = "Endpoints for user registration, login, and token management")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /register
    // ─────────────────────────────────────────────────────────────────────────

    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account with the ROLE_USER role. " +
                      "Returns 409 Conflict if the username or email is already taken."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new MessageResponse(
                        "User '" + user.getUsername() + "' registered successfully."));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /login
    // ─────────────────────────────────────────────────────────────────────────

    @Operation(
        summary = "Authenticate user and issue tokens",
        description = "Validates credentials and returns a JWT access token (24h) " +
                      "along with a refresh token (7 days)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(schema = @Schema(implementation = JwtResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        JwtResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /refresh
    // ─────────────────────────────────────────────────────────────────────────

    @Operation(
        summary = "Refresh access token",
        description = "Accepts a valid refresh token and issues a new JWT access token. " +
                      "The refresh token itself remains unchanged."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
            content = @Content(schema = @Schema(implementation = JwtResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "403", description = "Refresh token expired or not found")
    })
    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        JwtResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test endpoints (protected)
    // ─────────────────────────────────────────────────────────────────────────

    @Operation(summary = "Public health check", description = "Returns a simple OK — no authentication required.")
    @ApiResponse(responseCode = "200", description = "Service is up")
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "auth-service"
        ));
    }

    @Operation(summary = "Test — USER role access",
               description = "Returns 200 only for users with ROLE_USER or higher. Requires Bearer token.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Access granted"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    })
    @GetMapping("/test/user")
    @PreAuthorize("hasAnyRole('USER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> testUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(Map.of(
                "message", "User access granted",
                "userId", userDetails.getId(),
                "username", userDetails.getUsername(),
                "email", userDetails.getEmail(),
                "authorities", userDetails.getAuthorities().toString()
        ));
    }

    @Operation(summary = "Test — ANALYST role access",
               description = "Returns 200 only for users with ROLE_ANALYST or ROLE_ADMIN. Requires Bearer token.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Access granted"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
        @ApiResponse(responseCode = "403", description = "Insufficient role")
    })
    @GetMapping("/test/analyst")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<Map<String, String>> testAnalyst() {
        return ResponseEntity.ok(Map.of(
                "message", "Analyst access granted",
                "role", "ROLE_ANALYST or ROLE_ADMIN required"
        ));
    }

    @Operation(summary = "Test — ADMIN role access",
               description = "Returns 200 only for users with ROLE_ADMIN. Requires Bearer token.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Access granted"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
        @ApiResponse(responseCode = "403", description = "Insufficient role")
    })
    @GetMapping("/test/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> testAdmin() {
        return ResponseEntity.ok(Map.of(
                "message", "Admin access granted",
                "role", "ROLE_ADMIN required"
        ));
    }
}
