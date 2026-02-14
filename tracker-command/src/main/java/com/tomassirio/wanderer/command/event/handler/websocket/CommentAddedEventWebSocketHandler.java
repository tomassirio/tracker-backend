package com.tomassirio.wanderer.command.event.handler.websocket;

import com.tomassirio.wanderer.command.event.CommentAddedEvent;
import com.tomassirio.wanderer.command.event.EventHandler;
import com.tomassirio.wanderer.command.websocket.CommentAddedPayload;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * WebSocket event handler for broadcasting comment added events.
 *
 * <p>This handler broadcasts CommentAddedEvent to WebSocket clients after the transaction commits.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(2) // Execute after persistence
public class CommentAddedEventWebSocketHandler implements EventHandler<CommentAddedEvent> {

    private final WebSocketEventService webSocketEventService;

    @Override
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(CommentAddedEvent event) {
        log.debug(
                "Handling CommentAddedEvent for trip: {}, comment: {}",
                event.getTripId(),
                event.getCommentId());
        CommentAddedPayload payload =
                CommentAddedPayload.create(
                        event.getTripId(),
                        event.getCommentId(),
                        event.getUserId(),
                        event.getUsername(),
                        event.getMessage(),
                        event.getParentCommentId());
        webSocketEventService.broadcastCommentAdded(payload);
    }
}
