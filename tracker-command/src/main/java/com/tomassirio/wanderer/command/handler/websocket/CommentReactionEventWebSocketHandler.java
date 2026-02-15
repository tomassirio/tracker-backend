package com.tomassirio.wanderer.command.handler.websocket;

import com.tomassirio.wanderer.command.event.CommentReactionEvent;
import com.tomassirio.wanderer.command.handler.EventHandler;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import com.tomassirio.wanderer.command.websocket.WebSocketEventType;
import com.tomassirio.wanderer.command.websocket.payload.CommentReactionPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * WebSocket event handler for broadcasting comment reaction events.
 *
 * <p>This handler broadcasts CommentReactionEvent to WebSocket clients after the transaction
 * commits.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(2) // Execute after persistence
public class CommentReactionEventWebSocketHandler implements EventHandler<CommentReactionEvent> {

    private final WebSocketEventService webSocketEventService;

    @Override
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(CommentReactionEvent event) {
        log.debug(
                "Handling CommentReactionEvent for trip: {}, comment: {}, added: {}",
                event.getTripId(),
                event.getCommentId(),
                event.isAdded());
        CommentReactionPayload payload =
                CommentReactionPayload.builder()
                        .tripId(event.getTripId())
                        .commentId(event.getCommentId())
                        .reactionType(event.getReactionType())
                        .userId(event.getUserId())
                        .build();

        String eventType =
                event.isAdded()
                        ? WebSocketEventType.COMMENT_REACTION_ADDED
                        : WebSocketEventType.COMMENT_REACTION_REMOVED;
        webSocketEventService.broadcastToTrip(event.getTripId(), eventType, payload);
    }
}
