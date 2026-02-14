package com.tomassirio.wanderer.command.event.handler.websocket;

import com.tomassirio.wanderer.command.event.EventHandler;
import com.tomassirio.wanderer.command.event.TripUpdatedEvent;
import com.tomassirio.wanderer.command.websocket.TripUpdatedPayload;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * WebSocket event handler for broadcasting trip update events.
 *
 * <p>This handler broadcasts TripUpdatedEvent to WebSocket clients after the transaction commits.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(2) // Execute after persistence
public class TripUpdatedEventWebSocketHandler implements EventHandler<TripUpdatedEvent> {

    private final WebSocketEventService webSocketEventService;

    @Override
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(TripUpdatedEvent event) {
        log.debug("Handling TripUpdatedEvent for trip: {}", event.getTripId());
        TripUpdatedPayload payload =
                TripUpdatedPayload.builder()
                        .tripId(event.getTripId())
                        .latitude(event.getLatitude())
                        .longitude(event.getLongitude())
                        .batteryLevel(event.getBatteryLevel())
                        .message(event.getMessage())
                        .build();
        webSocketEventService.broadcastTripUpdated(payload);
    }
}
