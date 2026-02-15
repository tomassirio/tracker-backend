package com.tomassirio.wanderer.command.event.handler.websocket;

import com.tomassirio.wanderer.command.event.CommentReactionEvent;
import com.tomassirio.wanderer.command.event.EventHandler;
import com.tomassirio.wanderer.command.websocket.CommentReactionPayload;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
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

        if (event.isAdded()) {
            webSocketEventService.broadcastCommentReactionAdded(payload);
        } else {
            webSocketEventService.broadcastCommentReactionRemoved(payload);
        }
    }
}
