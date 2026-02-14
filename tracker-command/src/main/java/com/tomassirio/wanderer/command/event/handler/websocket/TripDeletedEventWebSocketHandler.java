package com.tomassirio.wanderer.command.event.handler.websocket;

import com.tomassirio.wanderer.command.event.EventHandler;
import com.tomassirio.wanderer.command.event.TripDeletedEvent;
import com.tomassirio.wanderer.command.websocket.TripLifecyclePayload;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * WebSocket event handler for broadcasting trip deletion events.
 *
 * <p>This handler broadcasts TripDeletedEvent to WebSocket clients after the transaction commits.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(2) // Execute after persistence
public class TripDeletedEventWebSocketHandler implements EventHandler<TripDeletedEvent> {

    private final WebSocketEventService webSocketEventService;

    @Override
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(TripDeletedEvent event) {
        log.debug("Handling TripDeletedEvent: {}", event.getTripId());
        TripLifecyclePayload payload =
                TripLifecyclePayload.builder()
                        .tripId(event.getTripId())
                        .tripName(null)
                        .ownerId(event.getOwnerId())
                        .visibility(null)
                        .build();
        webSocketEventService.broadcastTripDeleted(payload);
    }
}
