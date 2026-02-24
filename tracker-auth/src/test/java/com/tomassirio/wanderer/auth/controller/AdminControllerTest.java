package com.tomassirio.wanderer.auth.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tomassirio.wanderer.auth.service.AdminService;
import com.tomassirio.wanderer.auth.service.UserRoleService;
import com.tomassirio.wanderer.commons.exception.GlobalExceptionHandler;
import com.tomassirio.wanderer.commons.security.Role;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Unit tests for AdminController.
 *
 * <p>Note: These tests use standalone MockMvc setup which doesn't enforce @PreAuthorize
 * annotations. Security authorization is tested in integration tests.
 */
@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock private UserRoleService userRoleService;

    @Mock private AdminService adminService;

    @InjectMocks private AdminController adminController;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.standaloneSetup(adminController)
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .build();
        SecurityContextHolder.clearContext();
    }

    /** Helper method to create a JWT authentication with admin role */
    private RequestPostProcessor jwtAuthAdmin() {
        return request -> {
            Jwt jwt =
                    Jwt.withTokenValue("mock.jwt.token")
                            .header("alg", "HS256")
                            .subject(UUID.randomUUID().toString())
                            .claim("roles", List.of("ADMIN"))
                            .issuedAt(Instant.now())
                            .expiresAt(Instant.now().plusSeconds(3600))
                            .build();
            JwtAuthenticationToken authentication =
                    new JwtAuthenticationToken(
                            jwt, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return request;
        };
    }

    @Test
    void promoteToAdmin_whenValidUser_shouldReturn204() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        doNothing().when(userRoleService).promoteToAdmin(userId);

        // When & Then
        mockMvc.perform(post("/api/1/admin/users/{userId}/promote", userId).with(jwtAuthAdmin()))
                .andExpect(status().isNoContent());
    }

    @Test
    void promoteToAdmin_whenUserNotFound_shouldReturn400() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        doThrow(new IllegalArgumentException("User not found"))
                .when(userRoleService)
                .promoteToAdmin(userId);

        // When & Then
        mockMvc.perform(post("/api/1/admin/users/{userId}/promote", userId).with(jwtAuthAdmin()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void promoteToAdmin_whenUserAlreadyAdmin_shouldReturn400() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        doThrow(new IllegalArgumentException("User already has admin role"))
                .when(userRoleService)
                .promoteToAdmin(userId);

        // When & Then
        mockMvc.perform(post("/api/1/admin/users/{userId}/promote", userId).with(jwtAuthAdmin()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void demoteFromAdmin_whenValidUser_shouldReturn204() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        doNothing().when(userRoleService).demoteFromAdmin(userId);

        // When & Then
        mockMvc.perform(delete("/api/1/admin/users/{userId}/promote", userId).with(jwtAuthAdmin()))
                .andExpect(status().isNoContent());
    }

    @Test
    void demoteFromAdmin_whenUserNotAdmin_shouldReturn400() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        doThrow(new IllegalArgumentException("User does not have admin role"))
                .when(userRoleService)
                .demoteFromAdmin(userId);

        // When & Then
        mockMvc.perform(delete("/api/1/admin/users/{userId}/promote", userId).with(jwtAuthAdmin()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserRoles_whenValidUser_shouldReturnRoles() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRoleService.getUserRoles(userId)).thenReturn(Set.of(Role.USER, Role.ADMIN));

        // When & Then
        mockMvc.perform(get("/api/1/admin/users/{userId}/roles", userId).with(jwtAuthAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getUserRoles_whenUserNotFound_shouldReturn400() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        doThrow(new IllegalArgumentException("User not found"))
                .when(userRoleService)
                .getUserRoles(userId);

        // When & Then
        mockMvc.perform(get("/api/1/admin/users/{userId}/roles", userId).with(jwtAuthAdmin()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteCredentials_whenValidUser_shouldReturn204() throws Exception {
        UUID userId = UUID.randomUUID();
        doNothing().when(adminService).deleteCredentials(userId);

        mockMvc.perform(
                        delete("/api/1/admin/users/{userId}/credentials", userId)
                                .with(jwtAuthAdmin()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCredentials_whenUserNotFound_shouldReturn400() throws Exception {
        UUID userId = UUID.randomUUID();
        doThrow(new IllegalArgumentException("User not found"))
                .when(adminService)
                .deleteCredentials(userId);

        mockMvc.perform(
                        delete("/api/1/admin/users/{userId}/credentials", userId)
                                .with(jwtAuthAdmin()))
                .andExpect(status().isBadRequest());
    }
}
