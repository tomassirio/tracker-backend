package com.tomassirio.wanderer.command.websocket;

import com.tomassirio.wanderer.command.event.Broadcastable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Centralized event listener for broadcasting all {@link Broadcastable} events via WebSocket.
 *
 * <p>This listener handles all WebSocket broadcasting after transaction commits, removing the need
 * for individual persistence handlers to implement their own broadcast methods. Any event that
 * implements {@link Broadcastable} will automatically be broadcast to the appropriate topic.
 *
 * <p>The listener runs after transaction commit to ensure that only successfully persisted events
 * are broadcast to subscribers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BroadcastableEventListener {

    private final WebSocketEventService webSocketEventService;

    /**
     * Broadcasts any event that implements {@link Broadcastable} after the transaction commits.
     *
     * @param event the broadcastable event to send via WebSocket
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBroadcastableEvent(Broadcastable event) {
        log.debug("Broadcasting {} event to topic {}", event.getEventType(), event.getTopic());
        webSocketEventService.broadcast(event);
    }
}
