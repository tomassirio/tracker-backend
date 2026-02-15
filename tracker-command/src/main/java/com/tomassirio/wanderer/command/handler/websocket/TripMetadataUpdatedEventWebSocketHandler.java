package com.tomassirio.wanderer.command.handler.websocket;

import com.tomassirio.wanderer.command.event.TripMetadataUpdatedEvent;
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
 * WebSocket event handler for broadcasting trip metadata update events.
 *
 * <p>This handler broadcasts TripMetadataUpdatedEvent to WebSocket clients after the transaction
 * commits.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(2) // Execute after persistence
public class TripMetadataUpdatedEventWebSocketHandler
        implements EventHandler<TripMetadataUpdatedEvent> {

    private final WebSocketEventService webSocketEventService;

    @Override
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(TripMetadataUpdatedEvent event) {
        log.debug("Handling TripMetadataUpdatedEvent: {}", event.getTripId());
        TripLifecyclePayload payload =
                TripLifecyclePayload.builder()
                        .tripId(event.getTripId())
                        .tripName(event.getTripName())
                        .ownerId(null)
                        .visibility(event.getVisibility())
                        .build();
        webSocketEventService.broadcastToTrip(
                event.getTripId(), WebSocketEventType.TRIP_METADATA_UPDATED, payload);
    }
}
