package com.tomassirio.wanderer.command.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tomassirio.wanderer.command.service.AdminService;
import com.tomassirio.wanderer.command.service.UserService;
import com.tomassirio.wanderer.commons.exception.GlobalExceptionHandler;
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
class AdminControllerTest {

    private static final String ADMIN_USERS_URL = "/api/1/admin/users";

    private MockMvc mockMvc;

    @Mock private AdminService adminService;

    @Mock private UserService userService;

    @InjectMocks private AdminController adminController;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.standaloneSetup(adminController)
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .build();
    }

    @Test
    void promoteToAdmin_shouldReturnNoContent() throws Exception {
        UUID userId = UUID.randomUUID();
        doNothing().when(adminService).promoteToAdmin(userId);

        mockMvc.perform(post(ADMIN_USERS_URL + "/{userId}/promote", userId))
                .andExpect(status().isNoContent());

        verify(adminService).promoteToAdmin(userId);
    }

    @Test
    void demoteFromAdmin_shouldReturnNoContent() throws Exception {
        UUID userId = UUID.randomUUID();
        doNothing().when(adminService).demoteFromAdmin(userId);

        mockMvc.perform(delete(ADMIN_USERS_URL + "/{userId}/promote", userId))
                .andExpect(status().isNoContent());

        verify(adminService).demoteFromAdmin(userId);
    }

    @Test
    void deleteUser_shouldReturnNoContent() throws Exception {
        UUID userId = UUID.randomUUID();
        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete(ADMIN_USERS_URL + "/{userId}", userId))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(userId);
    }

    @Test
    void promoteToAdmin_whenServiceFails_shouldReturnInternalServerError() throws Exception {
        UUID userId = UUID.randomUUID();
        doThrow(new RuntimeException("Auth service unavailable"))
                .when(adminService)
                .promoteToAdmin(userId);

        mockMvc.perform(post(ADMIN_USERS_URL + "/{userId}/promote", userId))
                .andExpect(status().isInternalServerError());
    }
}
