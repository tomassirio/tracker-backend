package com.tomassirio.wanderer.command.handler;

import com.tomassirio.wanderer.command.event.TripDeletedEvent;
import com.tomassirio.wanderer.commons.domain.Trip;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event handler for TripDeletedEvent that handles persistence.
 *
 * <p>This handler deletes the trip during the transaction. WebSocket broadcasting is handled
 * centrally by {@link com.tomassirio.wanderer.command.websocket.BroadcastableEventListener}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TripDeletedEventHandler implements EventHandler<TripDeletedEvent> {

    private final EntityManager entityManager;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(TripDeletedEvent event) {
        log.debug("Persisting TripDeletedEvent for trip: {}", event.getTripId());

        Trip trip = entityManager.find(Trip.class, event.getTripId());
        if (trip != null) {
            entityManager.remove(trip);
        }
        log.info("Trip deleted: {}", event.getTripId());
    }
}
