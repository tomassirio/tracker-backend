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
import com.tomassirio.wanderer.command.service.FriendRequestService;
import com.tomassirio.wanderer.commons.dto.FriendRequestRequest;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class FriendRequestControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock private FriendRequestService friendRequestService;

    @Mock private JwtUtils jwtUtils;

    @InjectMocks private FriendRequestController friendRequestController;

    private UUID senderId;
    private UUID receiverId;
    private UUID requestId;
    private String token;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        senderId = UUID.randomUUID();
        receiverId = UUID.randomUUID();
        requestId = UUID.randomUUID();
        token = "Bearer valid-token";

        mockMvc =
                MockMvcBuilders.standaloneSetup(friendRequestController)
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .setCustomArgumentResolvers(new CurrentUserIdArgumentResolver(jwtUtils))
                        .build();

        doReturn(senderId).when(jwtUtils).getUserIdFromAuthorizationHeader(token);
    }

    @Test
    void sendFriendRequest_Success() throws Exception {
        FriendRequestRequest request = new FriendRequestRequest(receiverId);

        doReturn(requestId)
                .when(friendRequestService)
                .sendFriendRequest(eq(senderId), eq(receiverId));

        mockMvc.perform(
                        post("/api/1/users/friends/requests")
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$").value(requestId.toString()));
    }

    @Test
    void acceptFriendRequest_Success() throws Exception {
        doReturn(requestId)
                .when(friendRequestService)
                .acceptFriendRequest(eq(requestId), eq(senderId));

        mockMvc.perform(
                        post("/api/1/users/friends/requests/" + requestId + "/accept")
                                .header("Authorization", token))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$").value(requestId.toString()));

        verify(friendRequestService).acceptFriendRequest(requestId, senderId);
    }

    @Test
    void deleteFriendRequest_Success() throws Exception {
        doReturn(requestId)
                .when(friendRequestService)
                .deleteFriendRequest(eq(requestId), eq(senderId));

        mockMvc.perform(
                        delete("/api/1/users/friends/requests/" + requestId)
                                .header("Authorization", token))
                .andExpect(status().isAccepted());

        verify(friendRequestService).deleteFriendRequest(requestId, senderId);
    }
}
