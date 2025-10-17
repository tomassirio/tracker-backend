package com.tomassirio.wanderer.query.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tomassirio.wanderer.commons.constants.ApiConstants;
import com.tomassirio.wanderer.commons.domain.FriendRequestStatus;
import com.tomassirio.wanderer.commons.dto.FriendRequestResponse;
import com.tomassirio.wanderer.commons.exception.GlobalExceptionHandler;
import com.tomassirio.wanderer.commons.security.CurrentUserIdArgumentResolver;
import com.tomassirio.wanderer.commons.security.JwtUtils;
import com.tomassirio.wanderer.query.service.FriendRequestQueryService;
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
class FriendRequestQueryControllerTest {

    private static final String RECEIVED_REQUESTS_URL =
            ApiConstants.FRIEND_REQUESTS_PATH + ApiConstants.FRIEND_REQUESTS_RECEIVED_ENDPOINT;
    private static final String SENT_REQUESTS_URL =
            ApiConstants.FRIEND_REQUESTS_PATH + ApiConstants.FRIEND_REQUESTS_SENT_ENDPOINT;

    private MockMvc mockMvc;

    @Mock private FriendRequestQueryService friendRequestQueryService;

    @Mock private JwtUtils jwtUtils;

    @InjectMocks private FriendRequestQueryController friendRequestQueryController;

    private UUID userId;
    private UUID requestId;
    private String token;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        requestId = UUID.randomUUID();
        token = "Bearer test-token";

        mockMvc =
                MockMvcBuilders.standaloneSetup(friendRequestQueryController)
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .setCustomArgumentResolvers(new CurrentUserIdArgumentResolver(jwtUtils))
                        .build();

        when(jwtUtils.getUserIdFromAuthorizationHeader(token)).thenReturn(userId);
    }

    @Test
    void getReceivedFriendRequests_Success() throws Exception {
        FriendRequestResponse response =
                new FriendRequestResponse(
                        requestId,
                        UUID.randomUUID(),
                        userId,
                        FriendRequestStatus.PENDING,
                        Instant.now(),
                        null);

        when(friendRequestQueryService.getPendingReceivedRequests(userId))
                .thenReturn(List.of(response));

        mockMvc.perform(get(RECEIVED_REQUESTS_URL).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(requestId.toString()));

        verify(friendRequestQueryService).getPendingReceivedRequests(userId);
    }

    @Test
    void getReceivedFriendRequests_EmptyList() throws Exception {
        when(friendRequestQueryService.getPendingReceivedRequests(userId)).thenReturn(List.of());

        mockMvc.perform(get(RECEIVED_REQUESTS_URL).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(friendRequestQueryService).getPendingReceivedRequests(userId);
    }

    @Test
    void getSentFriendRequests_Success() throws Exception {
        FriendRequestResponse response =
                new FriendRequestResponse(
                        requestId,
                        userId,
                        UUID.randomUUID(),
                        FriendRequestStatus.PENDING,
                        Instant.now(),
                        null);

        when(friendRequestQueryService.getPendingSentRequests(userId))
                .thenReturn(List.of(response));

        mockMvc.perform(get(SENT_REQUESTS_URL).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(requestId.toString()));

        verify(friendRequestQueryService).getPendingSentRequests(userId);
    }

    @Test
    void getSentFriendRequests_EmptyList() throws Exception {
        when(friendRequestQueryService.getPendingSentRequests(userId)).thenReturn(List.of());

        mockMvc.perform(get(SENT_REQUESTS_URL).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(friendRequestQueryService).getPendingSentRequests(userId);
    }
}
