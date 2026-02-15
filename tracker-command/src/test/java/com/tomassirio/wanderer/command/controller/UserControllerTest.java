package com.tomassirio.wanderer.command.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tomassirio.wanderer.command.dto.UserCreationRequest;
import com.tomassirio.wanderer.command.dto.UserResponse;
import com.tomassirio.wanderer.command.service.UserService;
import com.tomassirio.wanderer.commons.exception.GlobalExceptionHandler;
import java.util.UUID;
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
class UserControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock private UserService userService;

    @InjectMocks private UserController userController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc =
                MockMvcBuilders.standaloneSetup(userController)
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .build();
    }

    @Test
    void createUser_whenValidRequest_shouldReturnCreated() throws Exception {
        UserCreationRequest req = new UserCreationRequest("johndoe", "john@example.com");
        UUID id = UUID.randomUUID();
        UserResponse resp = new UserResponse(id, "johndoe");

        doReturn(resp).when(userService).createUser(any(UserCreationRequest.class));

        mockMvc.perform(
                        post("/api/1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.username").value("johndoe"));
    }

    @Test
    void createUser_whenUsernameTooShort_shouldReturnBadRequest() throws Exception {
        UserCreationRequest req = new UserCreationRequest("ab", "john@example.com");

        mockMvc.perform(
                        post("/api/1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_whenEmailInvalid_shouldReturnBadRequest() throws Exception {
        UserCreationRequest req = new UserCreationRequest("johndoe", "not-an-email");

        mockMvc.perform(
                        post("/api/1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_whenServiceThrowsIllegalArgument_shouldReturnBadRequest() throws Exception {
        UserCreationRequest req = new UserCreationRequest("johndoe", "john@example.com");
        when(userService.createUser(any(UserCreationRequest.class)))
                .thenThrow(new IllegalArgumentException("Username already in use"));

        mockMvc.perform(
                        post("/api/1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
