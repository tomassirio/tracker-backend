package com.tomassirio.wanderer.command.handler.persistence;

import com.tomassirio.wanderer.command.event.Broadcastable;
import com.tomassirio.wanderer.command.event.TripVisibilityChangedEvent;
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
 * Event handler for persisting trip visibility change events to the database.
 *
 * <p>This handler implements the CQRS write side by handling TripVisibilityChangedEvent and
 * updating trip visibility in the database.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TripVisibilityChangedEventPersistenceHandler
        implements EventHandler<TripVisibilityChangedEvent> {

    private final TripRepository tripRepository;
    private final TripEmbeddedObjectsInitializer embeddedObjectsInitializer;
    private final WebSocketEventService webSocketEventService;

    @Override
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(TripVisibilityChangedEvent event) {
        log.debug("Persisting TripVisibilityChangedEvent for trip: {}", event.getTripId());

        tripRepository
                .findById(event.getTripId())
                .ifPresent(
                        trip -> {
                            embeddedObjectsInitializer.ensureTripSettings(
                                    trip, TripVisibility.valueOf(event.getNewVisibility()));
                            trip.getTripSettings()
                                    .setVisibility(
                                            TripVisibility.valueOf(event.getNewVisibility()));
                            tripRepository.save(trip);
                            log.info("Trip visibility changed: {}", event.getTripId());
                        });
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void broadcast(TripVisibilityChangedEvent event) {
        if (event instanceof Broadcastable broadcastable) {
            log.debug("Broadcasting TripVisibilityChangedEvent for trip: {}", event.getTripId());
            webSocketEventService.broadcast(broadcastable);
        }
    }
}
