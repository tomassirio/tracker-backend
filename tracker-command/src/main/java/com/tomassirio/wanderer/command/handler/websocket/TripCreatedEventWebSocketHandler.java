package com.tomassirio.wanderer.command.handler.websocket;

import com.tomassirio.wanderer.command.event.TripCreatedEvent;
import com.tomassirio.wanderer.command.handler.EventHandler;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import com.tomassirio.wanderer.command.websocket.WebSocketEventType;
import com.tomassirio.wanderer.command.websocket.payload.TripLifecyclePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * WebSocket event handler for broadcasting trip creation events.
 *
 * <p>This handler broadcasts TripCreatedEvent to WebSocket clients after the transaction commits.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(2) // Execute after persistence
public class TripCreatedEventWebSocketHandler implements EventHandler<TripCreatedEvent> {

    private final WebSocketEventService webSocketEventService;

    @Override
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(TripCreatedEvent event) {
        log.debug("Handling TripCreatedEvent: {}", event.getTripId());
        TripLifecyclePayload payload =
                TripLifecyclePayload.builder()
                        .tripId(event.getTripId())
                        .tripName(event.getTripName())
                        .ownerId(event.getOwnerId())
                        .visibility(event.getVisibility())
                        .build();
        webSocketEventService.broadcastToTrip(
                event.getTripId(), WebSocketEventType.TRIP_CREATED, payload);
    }
}
