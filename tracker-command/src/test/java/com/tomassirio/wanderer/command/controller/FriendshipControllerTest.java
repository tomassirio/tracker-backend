package com.tomassirio.wanderer.command.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tomassirio.wanderer.command.service.FriendshipService;
import com.tomassirio.wanderer.commons.exception.GlobalExceptionHandler;
import com.tomassirio.wanderer.commons.security.CurrentUserIdArgumentResolver;
import com.tomassirio.wanderer.commons.security.JwtUtils;
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
class FriendshipControllerTest {

    private MockMvc mockMvc;

    @Mock private FriendshipService friendshipService;

    @Mock private JwtUtils jwtUtils;

    @InjectMocks private FriendshipController friendshipController;

    private UUID userId;
    private UUID friendId;
    private String token;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        friendId = UUID.randomUUID();
        token = "Bearer valid-token";

        mockMvc =
                MockMvcBuilders.standaloneSetup(friendshipController)
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .setCustomArgumentResolvers(new CurrentUserIdArgumentResolver(jwtUtils))
                        .build();

        when(jwtUtils.getUserIdFromAuthorizationHeader(token)).thenReturn(userId);
    }

    @Test
    void removeFriend_Success() throws Exception {
        doNothing().when(friendshipService).removeFriendship(eq(userId), eq(friendId));

        mockMvc.perform(
                        delete("/api/1/users/friends/" + friendId).header("Authorization", token))
                .andExpect(status().isAccepted());

        verify(friendshipService).removeFriendship(userId, friendId);
    }

    @Test
    void removeFriend_WhenNotFriends_ReturnsBadRequest() throws Exception {
        doThrow(new IllegalArgumentException("Users are not friends"))
                .when(friendshipService)
                .removeFriendship(eq(userId), eq(friendId));

        mockMvc.perform(
                        delete("/api/1/users/friends/" + friendId).header("Authorization", token))
                .andExpect(status().isBadRequest());

        verify(friendshipService).removeFriendship(userId, friendId);
    }
}

