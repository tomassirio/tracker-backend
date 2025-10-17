package com.tomassirio.wanderer.command.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tomassirio.wanderer.command.service.UserFollowService;
import com.tomassirio.wanderer.commons.dto.UserFollowRequest;
import com.tomassirio.wanderer.commons.dto.UserFollowResponse;
import com.tomassirio.wanderer.commons.exception.GlobalExceptionHandler;
import com.tomassirio.wanderer.commons.security.CurrentUserIdArgumentResolver;
import com.tomassirio.wanderer.commons.security.JwtUtils;
import java.time.Instant;
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
class UserFollowControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock private UserFollowService userFollowService;

    @Mock private JwtUtils jwtUtils;

    @InjectMocks private UserFollowController userFollowController;

    private UUID followerId;
    private UUID followedId;
    private String token;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        followerId = UUID.randomUUID();
        followedId = UUID.randomUUID();
        token = "Bearer valid-token";

        mockMvc =
                MockMvcBuilders.standaloneSetup(userFollowController)
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .setCustomArgumentResolvers(new CurrentUserIdArgumentResolver(jwtUtils))
                        .build();

        doReturn(followerId).when(jwtUtils).getUserIdFromAuthorizationHeader(token);
    }

    @Test
    void followUser_Success() throws Exception {
        UserFollowRequest request = new UserFollowRequest(followedId);
        UserFollowResponse response =
                new UserFollowResponse(UUID.randomUUID(), followerId, followedId, Instant.now());

        doReturn(response).when(userFollowService).followUser(eq(followerId), eq(followedId));

        mockMvc.perform(
                        post("/api/1/users/follows")
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.followerId").value(followerId.toString()))
                .andExpect(jsonPath("$.followedId").value(followedId.toString()));

        verify(userFollowService).followUser(followerId, followedId);
    }

    @Test
    void unfollowUser_Success() throws Exception {
        mockMvc.perform(delete("/api/1/users/follows/" + followedId).header("Authorization", token))
                .andExpect(status().isNoContent());

        verify(userFollowService).unfollowUser(followerId, followedId);
    }
}
