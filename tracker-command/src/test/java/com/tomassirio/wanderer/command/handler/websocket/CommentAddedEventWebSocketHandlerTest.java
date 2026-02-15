package com.tomassirio.wanderer.command.handler.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.tomassirio.wanderer.command.event.CommentAddedEvent;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import com.tomassirio.wanderer.command.websocket.WebSocketEventType;
import com.tomassirio.wanderer.command.websocket.payload.CommentAddedPayload;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentAddedEventWebSocketHandlerTest {

    @Mock private WebSocketEventService webSocketEventService;

    @InjectMocks private CommentAddedEventWebSocketHandler handler;

    @Test
    void handle_whenCommentAdded_shouldBroadcastToTrip() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentAddedEvent event =
                CommentAddedEvent.builder()
                        .tripId(tripId)
                        .commentId(commentId)
                        .userId(userId)
                        .username("testuser")
                        .message("Great journey!")
                        .parentCommentId(null)
                        .build();

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<CommentAddedPayload> payloadCaptor =
                ArgumentCaptor.forClass(CommentAddedPayload.class);
        verify(webSocketEventService)
                .broadcastToTrip(
                        eq(tripId), eq(WebSocketEventType.COMMENT_ADDED), payloadCaptor.capture());

        CommentAddedPayload payload = payloadCaptor.getValue();
        assertThat(payload.getTripId()).isEqualTo(tripId);
        assertThat(payload.getCommentId()).isEqualTo(commentId);
        assertThat(payload.getUserId()).isEqualTo(userId);
        assertThat(payload.getUsername()).isEqualTo("testuser");
        assertThat(payload.getMessage()).isEqualTo("Great journey!");
        assertThat(payload.getParentCommentId()).isNull();
    }

    @Test
    void handle_whenReplyAdded_shouldBroadcastToTripWithParent() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID parentCommentId = UUID.randomUUID();
        CommentAddedEvent event =
                CommentAddedEvent.builder()
                        .tripId(tripId)
                        .commentId(commentId)
                        .userId(userId)
                        .username("testuser")
                        .message("Thanks for sharing!")
                        .parentCommentId(parentCommentId)
                        .build();

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<CommentAddedPayload> payloadCaptor =
                ArgumentCaptor.forClass(CommentAddedPayload.class);
        verify(webSocketEventService)
                .broadcastToTrip(
                        eq(tripId), eq(WebSocketEventType.COMMENT_ADDED), payloadCaptor.capture());

        CommentAddedPayload payload = payloadCaptor.getValue();
        assertThat(payload.getParentCommentId()).isEqualTo(parentCommentId);
    }
}
