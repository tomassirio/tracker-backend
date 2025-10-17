package com.tomassirio.wanderer.query.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tomassirio.wanderer.commons.dto.UserFollowResponse;
import com.tomassirio.wanderer.commons.exception.GlobalExceptionHandler;
import com.tomassirio.wanderer.commons.security.CurrentUserIdArgumentResolver;
import com.tomassirio.wanderer.commons.security.JwtUtils;
import com.tomassirio.wanderer.query.service.UserFollowQueryService;
import java.time.Instant;
import java.util.List;
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
class UserFollowQueryControllerTest {

    private MockMvc mockMvc;

    @Mock private UserFollowQueryService userFollowQueryService;

    @Mock private JwtUtils jwtUtils;

    @InjectMocks private UserFollowQueryController userFollowQueryController;

    private UUID userId;
    private UUID followedId;
    private String token;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        followedId = UUID.randomUUID();
        token = "Bearer test-token";

        mockMvc =
                MockMvcBuilders.standaloneSetup(userFollowQueryController)
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .setCustomArgumentResolvers(new CurrentUserIdArgumentResolver(jwtUtils))
                        .build();

        when(jwtUtils.getUserIdFromAuthorizationHeader(token)).thenReturn(userId);
    }

    @Test
    void getFollowing_Success() throws Exception {
        UserFollowResponse response =
                new UserFollowResponse(UUID.randomUUID(), userId, followedId, Instant.now());

        when(userFollowQueryService.getFollowing(userId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/1/users/follows/following").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].followerId").value(userId.toString()))
                .andExpect(jsonPath("$[0].followedId").value(followedId.toString()));

        verify(userFollowQueryService).getFollowing(userId);
    }

    @Test
    void getFollowing_EmptyList() throws Exception {
        when(userFollowQueryService.getFollowing(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/1/users/follows/following").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(userFollowQueryService).getFollowing(userId);
    }

    @Test
    void getFollowers_Success() throws Exception {
        UserFollowResponse response =
                new UserFollowResponse(UUID.randomUUID(), followedId, userId, Instant.now());

        when(userFollowQueryService.getFollowers(userId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/1/users/follows/followers").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].followerId").value(followedId.toString()))
                .andExpect(jsonPath("$[0].followedId").value(userId.toString()));

        verify(userFollowQueryService).getFollowers(userId);
    }

    @Test
    void getFollowers_EmptyList() throws Exception {
        when(userFollowQueryService.getFollowers(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/1/users/follows/followers").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(userFollowQueryService).getFollowers(userId);
    }
}
