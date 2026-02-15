package com.tomassirio.wanderer.command.websocket;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tomassirio.wanderer.command.websocket.payload.CommentAddedPayload;
import com.tomassirio.wanderer.command.websocket.payload.CommentReactionPayload;
import com.tomassirio.wanderer.command.websocket.payload.FriendRequestPayload;
import com.tomassirio.wanderer.command.websocket.payload.TripUpdatedPayload;
import com.tomassirio.wanderer.command.websocket.payload.UserFollowPayload;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WebSocketEventServiceTest {

    @Mock private WebSocketSessionManager sessionManager;

    private WebSocketEventService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new WebSocketEventService(sessionManager, objectMapper);
    }

    @Test
    void broadcastToTrip_shouldBroadcastToTripTopic() {
        // Given
        UUID tripId = UUID.randomUUID();
        TripUpdatedPayload payload =
                TripUpdatedPayload.builder()
                        .tripId(tripId)
                        .latitude(40.7128)
                        .longitude(-74.0060)
                        .batteryLevel(85)
                        .message("Test message")
                        .build();

        // When
        service.broadcastToTrip(tripId, WebSocketEventType.TRIP_UPDATED, payload);

        // Then
        verify(sessionManager).broadcast(eq("/topic/trips/" + tripId), anyString());
    }

    @Test
    void broadcastToTrip_withCommentAdded_shouldBroadcastToTripTopic() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        CommentAddedPayload payload =
                CommentAddedPayload.builder()
                        .tripId(tripId)
                        .commentId(commentId)
                        .id(commentId)
                        .userId(UUID.randomUUID())
                        .username("testuser")
                        .message("Test comment")
                        .build();

        // When
        service.broadcastToTrip(tripId, WebSocketEventType.COMMENT_ADDED, payload);

        // Then
        verify(sessionManager).broadcast(eq("/topic/trips/" + tripId), anyString());
    }

    @Test
    void broadcastToTrip_withCommentReactionAdded_shouldBroadcastToTripTopic() {
        // Given
        UUID tripId = UUID.randomUUID();
        CommentReactionPayload payload =
                CommentReactionPayload.builder()
                        .tripId(tripId)
                        .commentId(UUID.randomUUID())
                        .reactionType("HEART")
                        .userId(UUID.randomUUID())
                        .build();

        // When
        service.broadcastToTrip(tripId, WebSocketEventType.COMMENT_REACTION_ADDED, payload);

        // Then
        verify(sessionManager).broadcast(eq("/topic/trips/" + tripId), anyString());
    }

    @Test
    void broadcastToTrip_withCommentReactionRemoved_shouldBroadcastToTripTopic() {
        // Given
        UUID tripId = UUID.randomUUID();
        CommentReactionPayload payload =
                CommentReactionPayload.builder()
                        .tripId(tripId)
                        .commentId(UUID.randomUUID())
                        .reactionType("HEART")
                        .userId(UUID.randomUUID())
                        .build();

        // When
        service.broadcastToTrip(tripId, WebSocketEventType.COMMENT_REACTION_REMOVED, payload);

        // Then
        verify(sessionManager).broadcast(eq("/topic/trips/" + tripId), anyString());
    }

    @Test
    void broadcastToUser_shouldBroadcastToUserTopic() {
        // Given
        UUID userId = UUID.randomUUID();
        FriendRequestPayload payload =
                FriendRequestPayload.builder()
                        .requestId(UUID.randomUUID())
                        .senderId(UUID.randomUUID())
                        .receiverId(userId)
                        .status("PENDING")
                        .build();

        // When
        service.broadcastToUser(userId, WebSocketEventType.FRIEND_REQUEST_RECEIVED, payload);

        // Then
        verify(sessionManager).broadcast(eq("/topic/users/" + userId), anyString());
    }

    @Test
    void broadcastToUser_withUserFollowed_shouldBroadcastToUserTopic() {
        // Given
        UUID followedId = UUID.randomUUID();
        UserFollowPayload payload =
                UserFollowPayload.builder()
                        .followId(UUID.randomUUID())
                        .followerId(UUID.randomUUID())
                        .followedId(followedId)
                        .build();

        // When
        service.broadcastToUser(followedId, WebSocketEventType.USER_FOLLOWED, payload);

        // Then
        verify(sessionManager).broadcast(eq("/topic/users/" + followedId), anyString());
    }
}
