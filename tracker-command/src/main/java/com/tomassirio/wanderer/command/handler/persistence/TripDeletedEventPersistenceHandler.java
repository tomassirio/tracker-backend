package com.tomassirio.wanderer.command.handler.persistence;

import com.tomassirio.wanderer.command.event.TripDeletedEvent;
import com.tomassirio.wanderer.command.handler.EventHandler;
import com.tomassirio.wanderer.command.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event handler for persisting trip deletion events to the database.
 *
 * <p>This handler implements the CQRS write side by handling TripDeletedEvent and removing trips
 * from the database.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(1) // Execute before WebSocket broadcasting
public class TripDeletedEventPersistenceHandler implements EventHandler<TripDeletedEvent> {

    private final TripRepository tripRepository;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(TripDeletedEvent event) {
        log.debug("Persisting TripDeletedEvent for trip: {}", event.getTripId());

        tripRepository.deleteById(event.getTripId());
        log.info("Trip deleted: {}", event.getTripId());
    }
}
