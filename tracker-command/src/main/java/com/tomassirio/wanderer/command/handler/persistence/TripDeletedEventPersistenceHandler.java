package com.tomassirio.wanderer.command.handler.persistence;

import com.tomassirio.wanderer.command.event.Broadcastable;
import com.tomassirio.wanderer.command.event.TripDeletedEvent;
import com.tomassirio.wanderer.command.handler.EventHandler;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event handler for TripDeletedEvent that handles both persistence and WebSocket broadcasting.
 *
 * <p>This handler deletes the trip during the transaction and broadcasts via WebSocket after the
 * transaction commits.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TripDeletedEventPersistenceHandler implements EventHandler<TripDeletedEvent> {

    private final TripRepository tripRepository;
    private final WebSocketEventService webSocketEventService;

    @Override
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(TripDeletedEvent event) {
        log.debug("Persisting TripDeletedEvent for trip: {}", event.getTripId());

        tripRepository.deleteById(event.getTripId());
        log.info("Trip deleted: {}", event.getTripId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void broadcast(TripDeletedEvent event) {
        if (event instanceof Broadcastable broadcastable) {
            log.debug("Broadcasting TripDeletedEvent for trip: {}", event.getTripId());
            webSocketEventService.broadcast(broadcastable);
        }
    }
}
