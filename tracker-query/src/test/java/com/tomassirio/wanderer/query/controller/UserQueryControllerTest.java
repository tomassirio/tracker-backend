package com.tomassirio.wanderer.query.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tomassirio.wanderer.commons.domain.User;
import com.tomassirio.wanderer.commons.exception.GlobalExceptionHandler;
import com.tomassirio.wanderer.query.dto.UserResponse;
import com.tomassirio.wanderer.query.service.UserQueryService;
import jakarta.persistence.EntityNotFoundException;
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
class UserQueryControllerTest {

    private MockMvc mockMvc;

    @Mock private UserQueryService userQueryService;

    @InjectMocks private UserQueryController userQueryController;

    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.standaloneSetup(userQueryController)
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .build();

        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .build();
    }

    @Test
    void getUser_whenUserExists_shouldReturnUser() throws Exception {
        UserResponse userResponse = new UserResponse(testUser.getId(), testUser.getUsername(), testUser.getEmail());
        when(userQueryService.getUserById(testUser.getId())).thenReturn(userResponse);

        mockMvc.perform(get("/api/1/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId().toString()))
                .andExpect(jsonPath("$.username").value(testUser.getUsername()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()));
    }

    @Test
    void getUser_whenUserDoesNotExist_shouldReturnNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(userQueryService.getUserById(nonExistentId)).thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(get("/api/1/users/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserByUsername_whenUserExists_shouldReturnUser() throws Exception {
        UserResponse userResponse = new UserResponse(testUser.getId(), testUser.getUsername(), testUser.getEmail());
        when(userQueryService.getUserByUsername(testUser.getUsername())).thenReturn(userResponse);

        mockMvc.perform(get("/api/1/users/username/{username}", testUser.getUsername()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId().toString()))
                .andExpect(jsonPath("$.username").value(testUser.getUsername()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()));
    }

    @Test
    void getUserByUsername_whenUserDoesNotExist_shouldReturnNotFound() throws Exception {
        String nonExistentUsername = "nonexistent";
        when(userQueryService.getUserByUsername(nonExistentUsername)).thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(get("/api/1/users/username/{username}", nonExistentUsername))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserByEmail_whenUserExists_shouldReturnUser() throws Exception {
        UserResponse userResponse = new UserResponse(testUser.getId(), testUser.getUsername(), testUser.getEmail());
        when(userQueryService.getUserByEmail(testUser.getEmail())).thenReturn(userResponse);

        mockMvc.perform(get("/api/1/users/email/{email}", testUser.getEmail()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId().toString()))
                .andExpect(jsonPath("$.username").value(testUser.getUsername()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()));
    }

    @Test
    void getUserByEmail_whenUserDoesNotExist_shouldReturnNotFound() throws Exception {
        String nonExistentEmail = "nonexistent@example.com";
        when(userQueryService.getUserByEmail(nonExistentEmail)).thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(get("/api/1/users/email/{email}", nonExistentEmail))
                .andExpect(status().isNotFound());
    }
}
