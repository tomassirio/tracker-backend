package com.tomassirio.wanderer.command.handler;

import com.tomassirio.wanderer.command.event.TripMetadataUpdatedEvent;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.service.helper.TripEmbeddedObjectsInitializer;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event handler for persisting trip metadata update events to the database.
 *
 * <p>This handler implements the CQRS write side by handling TripMetadataUpdatedEvent and updating
 * trip metadata in the database. WebSocket broadcasting is handled centrally by {@link
 * com.tomassirio.wanderer.command.websocket.BroadcastableEventListener}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class TripMetadataUpdatedEventHandler implements EventHandler<TripMetadataUpdatedEvent> {

    private final TripRepository tripRepository;
    private final TripEmbeddedObjectsInitializer embeddedObjectsInitializer;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
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
}
