package com.tomassirio.wanderer.command.handler;

import com.tomassirio.wanderer.command.event.TripVisibilityChangedEvent;
import com.tomassirio.wanderer.command.service.helper.TripEmbeddedObjectsInitializer;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event handler for persisting trip visibility change events to the database.
 *
 * <p>This handler implements the CQRS write side by handling TripVisibilityChangedEvent and
 * updating trip visibility in the database. WebSocket broadcasting is handled centrally by {@link
 * com.tomassirio.wanderer.command.websocket.BroadcastableEventListener}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TripVisibilityChangedEventHandler implements EventHandler<TripVisibilityChangedEvent> {

    private final EntityManager entityManager;
    private final TripEmbeddedObjectsInitializer embeddedObjectsInitializer;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(TripVisibilityChangedEvent event) {
        log.debug("Persisting TripVisibilityChangedEvent for trip: {}", event.getTripId());

        Trip trip = entityManager.find(Trip.class, event.getTripId());
        if (trip != null) {
            embeddedObjectsInitializer.ensureTripSettings(
                    trip, TripVisibility.valueOf(event.getNewVisibility()));
            trip.getTripSettings().setVisibility(TripVisibility.valueOf(event.getNewVisibility()));
            // No need to call save() - entity is managed and will be flushed automatically
            log.info("Trip visibility changed: {}", event.getTripId());
        }
    }
}
