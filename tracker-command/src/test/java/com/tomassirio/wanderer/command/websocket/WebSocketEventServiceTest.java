package com.tomassirio.wanderer.command.websocket;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    void shouldBroadcastTripStatusChanged() {
        // Given
        UUID tripId = UUID.randomUUID();
        String newStatus = "IN_PROGRESS";
        String previousStatus = "CREATED";

        // When
        service.broadcastTripStatusChanged(tripId, newStatus, previousStatus);

        // Then
        verify(sessionManager).broadcast(eq("/topic/trips/" + tripId), anyString());
    }

    @Test
    void shouldBroadcastTripUpdated() {
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
        service.broadcastTripUpdated(payload);

        // Then
        verify(sessionManager).broadcast(eq("/topic/trips/" + tripId), anyString());
    }

    @Test
    void shouldBroadcastCommentAdded() {
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
        service.broadcastCommentAdded(payload);

        // Then
        verify(sessionManager).broadcast(eq("/topic/trips/" + tripId), anyString());
    }

    @Test
    void shouldBroadcastCommentReactionAdded() {
        // Given
        UUID tripId = UUID.randomUUID();
        CommentReactionPayload payload =
                CommentReactionPayload.builder()
                        .tripId(tripId)
                        .commentId(UUID.randomUUID())
                        .reactionType("LIKE")
                        .userId(UUID.randomUUID())
                        .build();

        // When
        service.broadcastCommentReactionAdded(payload);

        // Then
        verify(sessionManager).broadcast(eq("/topic/trips/" + tripId), anyString());
    }

    @Test
    void shouldBroadcastCommentReactionRemoved() {
        // Given
        UUID tripId = UUID.randomUUID();
        CommentReactionPayload payload =
                CommentReactionPayload.builder()
                        .tripId(tripId)
                        .commentId(UUID.randomUUID())
                        .reactionType("LIKE")
                        .userId(UUID.randomUUID())
                        .build();

        // When
        service.broadcastCommentReactionRemoved(payload);

        // Then
        verify(sessionManager).broadcast(eq("/topic/trips/" + tripId), anyString());
    }
}
