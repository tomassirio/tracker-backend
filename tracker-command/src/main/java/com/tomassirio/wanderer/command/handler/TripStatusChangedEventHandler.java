package com.tomassirio.wanderer.command.handler;

import com.tomassirio.wanderer.command.event.TripStatusChangedEvent;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.service.helper.TripEmbeddedObjectsInitializer;
import com.tomassirio.wanderer.command.service.helper.TripStatusTransitionHandler;
import com.tomassirio.wanderer.commons.domain.TripStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event handler for persisting trip status change events to the database.
 *
 * <p>This handler implements the CQRS write side by handling TripStatusChangedEvent and updating
 * trip status in the database. WebSocket broadcasting is handled centrally by {@link
 * com.tomassirio.wanderer.command.websocket.BroadcastableEventListener}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TripStatusChangedEventHandler implements EventHandler<TripStatusChangedEvent> {

    private final TripRepository tripRepository;
    private final TripEmbeddedObjectsInitializer embeddedObjectsInitializer;
    private final TripStatusTransitionHandler statusTransitionHandler;

    @Override
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(TripStatusChangedEvent event) {
        log.debug("Persisting TripStatusChangedEvent for trip: {}", event.getTripId());

        tripRepository
                .findById(event.getTripId())
                .ifPresent(
                        trip -> {
                            TripStatus previousStatus =
                                    event.getPreviousStatus() != null
                                            ? TripStatus.valueOf(event.getPreviousStatus())
                                            : null;
                            TripStatus newStatus = TripStatus.valueOf(event.getNewStatus());

                            embeddedObjectsInitializer.ensureTripSettingsAndGetPreviousStatus(
                                    trip, newStatus);
                            trip.getTripSettings().setTripStatus(newStatus);
                            embeddedObjectsInitializer.ensureTripDetails(trip);
                            statusTransitionHandler.handleStatusTransition(
                                    trip, previousStatus, newStatus);

                            tripRepository.save(trip);
                            log.info("Trip status changed: {}", event.getTripId());
                        });
    }
}
