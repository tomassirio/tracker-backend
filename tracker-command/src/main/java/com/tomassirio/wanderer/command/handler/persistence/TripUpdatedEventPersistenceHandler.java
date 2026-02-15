package com.tomassirio.wanderer.command.handler.persistence;

import com.tomassirio.wanderer.command.event.Broadcastable;
import com.tomassirio.wanderer.command.event.TripUpdatedEvent;
import com.tomassirio.wanderer.command.handler.EventHandler;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.repository.TripUpdateRepository;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event handler for persisting trip update events to the database.
 *
 * <p>This handler implements the CQRS write side by handling TripUpdatedEvent and persisting trip
 * updates to the database. Validation is performed in the service layer before the event is
 * emitted.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TripUpdatedEventPersistenceHandler implements EventHandler<TripUpdatedEvent> {

    private final TripUpdateRepository tripUpdateRepository;
    private final TripRepository tripRepository;
    private final WebSocketEventService webSocketEventService;

    @Override
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(TripUpdatedEvent event) {
        log.debug("Persisting TripUpdatedEvent for trip: {}", event.getTripId());

        // Trip is validated in the service layer before event emission
        Trip trip = tripRepository.getReferenceById(event.getTripId());

        TripUpdate tripUpdate =
                TripUpdate.builder()
                        .id(event.getTripUpdateId())
                        .trip(trip)
                        .location(event.getLocation())
                        .battery(event.getBatteryLevel())
                        .message(event.getMessage())
                        .timestamp(event.getTimestamp())
                        .build();

        tripUpdateRepository.save(tripUpdate);
        log.info("Trip update created and persisted: {}", event.getTripUpdateId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void broadcast(TripUpdatedEvent event) {
        if (event instanceof Broadcastable broadcastable) {
            log.debug("Broadcasting TripUpdatedEvent for trip: {}", event.getTripId());
            webSocketEventService.broadcast(broadcastable);
        }
    }
}
