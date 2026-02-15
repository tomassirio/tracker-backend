package com.tomassirio.wanderer.command.handler;

import com.tomassirio.wanderer.command.event.TripUpdatedEvent;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.repository.TripUpdateRepository;
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
 * emitted. WebSocket broadcasting is handled centrally by {@link
 * com.tomassirio.wanderer.command.websocket.BroadcastableEventListener}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TripUpdatedEventHandler implements EventHandler<TripUpdatedEvent> {

    private final TripUpdateRepository tripUpdateRepository;
    private final TripRepository tripRepository;

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
}
