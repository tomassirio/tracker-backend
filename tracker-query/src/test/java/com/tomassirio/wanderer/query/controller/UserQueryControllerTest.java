package com.tomassirio.wanderer.query.controller;

import static com.tomassirio.wanderer.commons.utils.BaseTestEntityFactory.USER_ID;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tomassirio.wanderer.commons.exception.GlobalExceptionHandler;
import com.tomassirio.wanderer.commons.utils.MockMvcTestUtils;
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

@ExtendWith(MockitoExtension.class)
class UserQueryControllerTest {

    private static final String USERS_BASE_URL = "/api/1/users";
    private static final String USERS_ME_URL = USERS_BASE_URL + "/me";

    private MockMvc mockMvc;

    @Mock private UserQueryService userQueryService;

    @InjectMocks private UserQueryController userQueryController;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcTestUtils.buildMockMvcWithCurrentUserResolver(
                        userQueryController, new GlobalExceptionHandler());
    }

    @Test
    void getUser_whenUserExists_shouldReturnUser() throws Exception {
        UUID id = UUID.randomUUID();
        UserResponse resp = new UserResponse(id, "johndoe", "john@example.com");

        when(userQueryService.getUserById(id)).thenReturn(resp);

        mockMvc.perform(get(USERS_BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void getUser_whenUserDoesNotExist_shouldReturnNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(userQueryService.getUserById(id))
                .thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(get(USERS_BASE_URL + "/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void getUserByUsername_whenUserExists_shouldReturnUser() throws Exception {
        String username = "alice";
        UserResponse resp = new UserResponse(UUID.randomUUID(), username, "alice@example.com");

        when(userQueryService.getUserByUsername(username)).thenReturn(resp);

        mockMvc.perform(get(USERS_BASE_URL + "/username/{username}", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void getUserByEmail_whenUserExists_shouldReturnUser() throws Exception {
        String email = "bob@example.com";
        UserResponse resp = new UserResponse(UUID.randomUUID(), "bob", email);

        when(userQueryService.getUserByEmail(email)).thenReturn(resp);

        mockMvc.perform(get(USERS_BASE_URL + "/email/{email}", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("bob"))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void getMyUser_whenUserExists_shouldReturnUser() throws Exception {
        UserResponse resp = new UserResponse(USER_ID, "currentuser", "me@example.com");

        when(userQueryService.getUserById(USER_ID)).thenReturn(resp);

        mockMvc.perform(get(USERS_ME_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID.toString()))
                .andExpect(jsonPath("$.username").value("currentuser"))
                .andExpect(jsonPath("$.email").value("me@example.com"));
    }

    @Test
    void getMyUser_whenUserDoesNotExist_shouldReturnNotFound() throws Exception {
        when(userQueryService.getUserById(USER_ID))
                .thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(get(USERS_ME_URL)).andExpect(status().isNotFound());
    }
}
