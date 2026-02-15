package com.tomassirio.wanderer.command.handler.websocket;

import com.tomassirio.wanderer.command.event.TripStatusChangedEvent;
import com.tomassirio.wanderer.command.handler.EventHandler;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import com.tomassirio.wanderer.command.websocket.WebSocketEventType;
import com.tomassirio.wanderer.command.websocket.payload.TripStatusChangedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * WebSocket event handler for broadcasting trip status change events.
 *
 * <p>This handler broadcasts TripStatusChangedEvent to WebSocket clients after the transaction
 * commits.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(2) // Execute after persistence
public class TripStatusChangedEventWebSocketHandler
        implements EventHandler<TripStatusChangedEvent> {

    private final WebSocketEventService webSocketEventService;

    @Override
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(TripStatusChangedEvent event) {
        log.debug(
                "Handling TripStatusChangedEvent for trip: {}, status: {}",
                event.getTripId(),
                event.getNewStatus());
        TripStatusChangedPayload payload =
                TripStatusChangedPayload.builder()
                        .tripId(event.getTripId())
                        .newStatus(event.getNewStatus())
                        .previousStatus(event.getPreviousStatus())
                        .build();
        webSocketEventService.broadcastToTrip(
                event.getTripId(), WebSocketEventType.TRIP_STATUS_CHANGED, payload);
    }
}
