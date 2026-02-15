package com.tomassirio.wanderer.command.handler.persistence;

import com.tomassirio.wanderer.command.event.Broadcastable;
import com.tomassirio.wanderer.command.event.TripMetadataUpdatedEvent;
import com.tomassirio.wanderer.command.handler.EventHandler;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.service.helper.TripEmbeddedObjectsInitializer;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event handler for persisting trip metadata update events to the database.
 *
 * <p>This handler implements the CQRS write side by handling TripMetadataUpdatedEvent and updating
 * trip metadata in the database.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TripMetadataUpdatedEventPersistenceHandler
        implements EventHandler<TripMetadataUpdatedEvent> {

    private final TripRepository tripRepository;
    private final TripEmbeddedObjectsInitializer embeddedObjectsInitializer;
    private final WebSocketEventService webSocketEventService;

    @Override
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(TripMetadataUpdatedEvent event) {
        log.debug("Persisting TripMetadataUpdatedEvent for trip: {}", event.getTripId());

        tripRepository
                .findById(event.getTripId())
                .ifPresent(
                        trip -> {
                            trip.setName(event.getTripName());
                            embeddedObjectsInitializer.ensureTripSettings(
                                    trip, TripVisibility.valueOf(event.getVisibility()));
                            trip.getTripSettings()
                                    .setVisibility(TripVisibility.valueOf(event.getVisibility()));
                            embeddedObjectsInitializer.ensureTripDetails(trip);
                            tripRepository.save(trip);
                            log.info("Trip metadata updated: {}", event.getTripId());
                        });
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void broadcast(TripMetadataUpdatedEvent event) {
        if (event instanceof Broadcastable broadcastable) {
            log.debug("Broadcasting TripMetadataUpdatedEvent for trip: {}", event.getTripId());
            webSocketEventService.broadcast(broadcastable);
        }
    }
}
