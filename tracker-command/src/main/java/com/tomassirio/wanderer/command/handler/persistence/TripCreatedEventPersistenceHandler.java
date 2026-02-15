package com.tomassirio.wanderer.command.handler.persistence;

import com.tomassirio.wanderer.command.event.TripCreatedEvent;
import com.tomassirio.wanderer.command.handler.EventHandler;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.service.helper.TripEmbeddedObjectsInitializer;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event handler for persisting trip creation events to the database.
 *
 * <p>This handler implements the CQRS write side by handling TripCreatedEvent and persisting new
 * trips to the database.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(1) // Execute before WebSocket broadcasting
public class TripCreatedEventPersistenceHandler implements EventHandler<TripCreatedEvent> {

    private final TripRepository tripRepository;
    private final TripEmbeddedObjectsInitializer embeddedObjectsInitializer;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(TripCreatedEvent event) {
        log.debug("Persisting TripCreatedEvent for trip: {}", event.getTripId());

        Trip trip =
                Trip.builder()
                        .id(event.getTripId())
                        .name(event.getTripName())
                        .userId(event.getOwnerId())
                        .tripSettings(
                                embeddedObjectsInitializer.createTripSettings(
                                        TripVisibility.valueOf(event.getVisibility())))
                        .tripDetails(
                                event.getStartLocation() != null
                                                || event.getStartTimestamp() != null
                                        ? embeddedObjectsInitializer.createTripDetailsFromEvent(
                                                event.getStartLocation(),
                                                event.getEndLocation(),
                                                event.getWaypoints(),
                                                event.getStartTimestamp(),
                                                event.getEndTimestamp())
                                        : embeddedObjectsInitializer.createTripDetails())
                        .tripPlanId(event.getTripPlanId())
                        .creationTimestamp(event.getCreationTimestamp())
                        .enabled(true)
                        .build();

        tripRepository.save(trip);
        log.info("Trip created and persisted: {}", event.getTripId());
    }
}
