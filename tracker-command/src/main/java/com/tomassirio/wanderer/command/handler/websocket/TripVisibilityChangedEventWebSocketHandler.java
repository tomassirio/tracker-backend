package com.tomassirio.wanderer.command.handler.websocket;

import com.tomassirio.wanderer.command.event.TripVisibilityChangedEvent;
import com.tomassirio.wanderer.command.handler.EventHandler;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import com.tomassirio.wanderer.command.websocket.WebSocketEventType;
import com.tomassirio.wanderer.command.websocket.payload.TripVisibilityChangedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * WebSocket event handler for broadcasting trip visibility change events.
 *
 * <p>This handler broadcasts TripVisibilityChangedEvent to WebSocket clients after the transaction
 * commits.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(2) // Execute after persistence
public class TripVisibilityChangedEventWebSocketHandler
        implements EventHandler<TripVisibilityChangedEvent> {

    private final WebSocketEventService webSocketEventService;

    @Override
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(TripVisibilityChangedEvent event) {
        log.debug("Handling TripVisibilityChangedEvent: {}", event.getTripId());
        TripVisibilityChangedPayload payload =
                TripVisibilityChangedPayload.builder()
                        .tripId(event.getTripId())
                        .newVisibility(event.getNewVisibility())
                        .previousVisibility(event.getPreviousVisibility())
                        .build();
        webSocketEventService.broadcastToTrip(
                event.getTripId(), WebSocketEventType.TRIP_VISIBILITY_CHANGED, payload);
    }
}
