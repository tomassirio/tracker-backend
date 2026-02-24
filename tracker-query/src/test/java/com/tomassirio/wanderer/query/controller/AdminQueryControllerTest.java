package com.tomassirio.wanderer.query.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tomassirio.wanderer.commons.exception.GlobalExceptionHandler;
import com.tomassirio.wanderer.commons.security.Role;
import com.tomassirio.wanderer.query.client.TrackerAuthClient;
import feign.FeignException;
import feign.Request;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AdminQueryControllerTest {

    private static final String ADMIN_USERS_URL = "/api/1/admin/users";

    private MockMvc mockMvc;

    @Mock private TrackerAuthClient trackerAuthClient;

    @InjectMocks private AdminQueryController adminQueryController;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.standaloneSetup(adminQueryController)
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .build();
    }

    @Test
    void getUserRoles_shouldReturnRoles() throws Exception {
        UUID userId = UUID.randomUUID();
        Set<Role> roles = Set.of(Role.USER, Role.ADMIN);
        when(trackerAuthClient.getUserRoles(userId)).thenReturn(roles);

        mockMvc.perform(get(ADMIN_USERS_URL + "/{userId}/roles", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getUserRoles_whenAuthServiceFails_shouldReturnInternalServerError() throws Exception {
        UUID userId = UUID.randomUUID();
        when(trackerAuthClient.getUserRoles(userId))
                .thenThrow(new RuntimeException("Auth service unavailable"));

        mockMvc.perform(get(ADMIN_USERS_URL + "/{userId}/roles", userId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getUserRoles_whenUserNotFoundInAuth_shouldReturnEmptyRoles() throws Exception {
        UUID userId = UUID.randomUUID();
        Request request =
                Request.create(
                        Request.HttpMethod.GET,
                        "/api/1/admin/users/" + userId + "/roles",
                        Collections.emptyMap(),
                        null,
                        null,
                        null);
        when(trackerAuthClient.getUserRoles(userId))
                .thenThrow(new FeignException.BadRequest("User not found", request, null, null));

        mockMvc.perform(get(ADMIN_USERS_URL + "/{userId}/roles", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
