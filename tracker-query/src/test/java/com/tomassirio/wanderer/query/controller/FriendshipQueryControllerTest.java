package com.tomassirio.wanderer.query.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tomassirio.wanderer.commons.dto.FriendshipResponse;
import com.tomassirio.wanderer.commons.exception.GlobalExceptionHandler;
import com.tomassirio.wanderer.commons.security.CurrentUserIdArgumentResolver;
import com.tomassirio.wanderer.commons.security.JwtUtils;
import com.tomassirio.wanderer.query.service.FriendshipQueryService;
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
class FriendshipQueryControllerTest {

    private MockMvc mockMvc;

    @Mock private FriendshipQueryService friendshipQueryService;

    @Mock private JwtUtils jwtUtils;

    @InjectMocks private FriendshipQueryController friendshipQueryController;

    private UUID userId;
    private UUID friendId;
    private String token;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        friendId = UUID.randomUUID();
        token = "Bearer test-token";

        mockMvc =
                MockMvcBuilders.standaloneSetup(friendshipQueryController)
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .setCustomArgumentResolvers(new CurrentUserIdArgumentResolver(jwtUtils))
                        .build();

        when(jwtUtils.getUserIdFromAuthorizationHeader(token)).thenReturn(userId);
    }

    @Test
    void getFriends_Success() throws Exception {
        FriendshipResponse response = new FriendshipResponse(userId, friendId);

        when(friendshipQueryService.getFriends(userId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/1/users/friends").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$[0].friendId").value(friendId.toString()));

        verify(friendshipQueryService).getFriends(userId);
    }

    @Test
    void getFriends_EmptyList() throws Exception {
        when(friendshipQueryService.getFriends(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/1/users/friends").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(friendshipQueryService).getFriends(userId);
    }
}
