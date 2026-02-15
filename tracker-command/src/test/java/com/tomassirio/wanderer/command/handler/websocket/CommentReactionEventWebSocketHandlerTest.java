package com.tomassirio.wanderer.command.handler.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.tomassirio.wanderer.command.event.CommentReactionEvent;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import com.tomassirio.wanderer.command.websocket.WebSocketEventType;
import com.tomassirio.wanderer.command.websocket.payload.CommentReactionPayload;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentReactionEventWebSocketHandlerTest {

    @Mock private WebSocketEventService webSocketEventService;

    @InjectMocks private CommentReactionEventWebSocketHandler handler;

    @Test
    void handle_whenReactionAdded_shouldBroadcastToTrip() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentReactionEvent event =
                CommentReactionEvent.builder()
                        .tripId(tripId)
                        .commentId(commentId)
                        .userId(userId)
                        .reactionType("HEART")
                        .added(true)
                        .build();

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<CommentReactionPayload> payloadCaptor =
                ArgumentCaptor.forClass(CommentReactionPayload.class);
        verify(webSocketEventService)
                .broadcastToTrip(
                        eq(tripId),
                        eq(WebSocketEventType.COMMENT_REACTION_ADDED),
                        payloadCaptor.capture());

        CommentReactionPayload payload = payloadCaptor.getValue();
        assertThat(payload.getTripId()).isEqualTo(tripId);
        assertThat(payload.getCommentId()).isEqualTo(commentId);
        assertThat(payload.getUserId()).isEqualTo(userId);
        assertThat(payload.getReactionType()).isEqualTo("HEART");
    }

    @Test
    void handle_whenReactionRemoved_shouldBroadcastToTrip() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentReactionEvent event =
                CommentReactionEvent.builder()
                        .tripId(tripId)
                        .commentId(commentId)
                        .userId(userId)
                        .reactionType("SMILEY")
                        .added(false)
                        .build();

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<CommentReactionPayload> payloadCaptor =
                ArgumentCaptor.forClass(CommentReactionPayload.class);
        verify(webSocketEventService)
                .broadcastToTrip(
                        eq(tripId),
                        eq(WebSocketEventType.COMMENT_REACTION_REMOVED),
                        payloadCaptor.capture());

        CommentReactionPayload payload = payloadCaptor.getValue();
        assertThat(payload.getTripId()).isEqualTo(tripId);
        assertThat(payload.getCommentId()).isEqualTo(commentId);
        assertThat(payload.getUserId()).isEqualTo(userId);
        assertThat(payload.getReactionType()).isEqualTo("SMILEY");
    }
}
