package com.tomassirio.wanderer.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tomassirio.wanderer.auth.dto.LoginRequest;
import com.tomassirio.wanderer.auth.dto.LoginResponse;
import com.tomassirio.wanderer.auth.dto.RegisterRequest;
import com.tomassirio.wanderer.auth.service.AuthService;
import com.tomassirio.wanderer.commons.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private AuthService authService;

    @Mock private com.tomassirio.wanderer.auth.service.TokenService tokenService;

    @InjectMocks private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.standaloneSetup(authController)
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .build();
    }

    @Test
    void login_whenValidRequest_shouldReturnOk() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "password123");
        LoginResponse response =
                new LoginResponse("jwt.access.token", "refresh.token", "Bearer", 3600000L);

        when(authService.login(request.username(), request.password())).thenReturn(response);

        mockMvc.perform(
                        post("/api/1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt.access.token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600000L));
    }

    @Test
    void login_whenInvalidPassword_shouldReturnBadRequest() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "short");

        mockMvc.perform(
                        post("/api/1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_whenValidRequest_shouldReturnCreated() throws Exception {
        RegisterRequest request =
                new RegisterRequest("testuser", "test@example.com", "password123");
        LoginResponse response =
                new LoginResponse("jwt.access.token", "refresh.token", "Bearer", 3600000L);

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(
                        post("/api/1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("jwt.access.token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600000L));
    }

    @Test
    void register_whenInvalidEmail_shouldReturnBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest("testuser", "invalid-email", "password123");

        mockMvc.perform(
                        post("/api/1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_whenValidRequest_shouldReturnOk() throws Exception {
        com.tomassirio.wanderer.auth.dto.RefreshTokenRequest request =
                new com.tomassirio.wanderer.auth.dto.RefreshTokenRequest("valid.refresh.token");
        com.tomassirio.wanderer.auth.dto.RefreshTokenResponse response =
                new com.tomassirio.wanderer.auth.dto.RefreshTokenResponse(
                        "new.access.token", "new.refresh.token", "Bearer", 3600000L);

        when(tokenService.refreshAccessToken(request.refreshToken())).thenReturn(response);

        mockMvc.perform(
                        post("/api/1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new.access.token"))
                .andExpect(jsonPath("$.refreshToken").value("new.refresh.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600000L));
    }

    @Test
    void passwordReset_whenValidEmail_shouldReturnOk() throws Exception {
        com.tomassirio.wanderer.auth.dto.PasswordResetRequest request =
                new com.tomassirio.wanderer.auth.dto.PasswordResetRequest("test@example.com");

        when(authService.initiatePasswordReset(request.email())).thenReturn("reset.token.here");

        mockMvc.perform(
                        post("/api/1/auth/password/reset")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset token generated"))
                .andExpect(jsonPath("$.token").value("reset.token.here"));
    }
}
