package com.finsight.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.auth.controller.AuthController;
import com.finsight.auth.dto.AuthResponse;
import com.finsight.auth.dto.LoginRequest;
import com.finsight.auth.dto.RegisterRequest;
import com.finsight.auth.dto.UserDto;
import com.finsight.auth.exception.AuthException;
import com.finsight.auth.exception.GlobalExceptionHandler;
import com.finsight.auth.security.JwtProvider;
import com.finsight.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private AuthService authService;
    private JwtProvider jwtProvider;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        jwtProvider = mock(JwtProvider.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(authService, jwtProvider))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private AuthResponse response(String username) {
        return AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .id(1L)
                .username(username)
                .email(username + "@finsight.ai")
                .roles(Set.of("ROLE_USER"))
                .build();
    }

    @Test
    void register_shouldReturnTokens() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@user.com");
        request.setPassword("password123");

        when(authService.register(any(RegisterRequest.class))).thenReturn(response("newuser"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    @Test
    void register_shouldRejectDuplicateUsername() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("other@user.com");
        request.setPassword("password123");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new AuthException("Username is already taken!", HttpStatus.CONFLICT));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username is already taken!"));
    }

    @Test
    void login_shouldReturnTokens() throws Exception {
        LoginRequest request = new LoginRequest("newuser", "password123");

        when(authService.login(any(LoginRequest.class))).thenReturn(response("newuser"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    void login_shouldRejectBadCredentials() throws Exception {
        LoginRequest request = new LoginRequest("newuser", "wrongpassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new AuthException("Invalid username or password", HttpStatus.UNAUTHORIZED));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void me_shouldReturnCurrentUser() throws Exception {
        UserDto user = UserDto.builder()
                .id(1L)
                .username("demo")
                .email("demo@finsight.ai")
                .enabled(true)
                .roles(Set.of("ROLE_USER"))
                .build();

        when(jwtProvider.getUsernameFromToken(anyString())).thenReturn("demo");
        when(authService.getCurrentUser("demo")).thenReturn(user);

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("demo"))
                .andExpect(jsonPath("$.email").value("demo@finsight.ai"));
    }
}
