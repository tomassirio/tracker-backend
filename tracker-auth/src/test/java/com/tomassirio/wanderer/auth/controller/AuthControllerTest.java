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
import com.tomassirio.wanderer.auth.service.JwtService;
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

    @Mock private JwtService jwtService;

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
        String token = "jwt.token.here";
        long expiresIn = 3600000L;

        when(authService.login(request.username(), request.password())).thenReturn(token);
        when(jwtService.getExpirationMs()).thenReturn(expiresIn);

        mockMvc.perform(
                        post("/api/1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(token))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(expiresIn));
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
        LoginResponse response = new LoginResponse("jwt.token", "Bearer", 3600000L);

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(
                        post("/api/1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("jwt.token"))
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
}
