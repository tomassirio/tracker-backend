package com.tomassirio.wanderer.command.event;

import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.service.helper.TripEmbeddedObjectsInitializer;
import com.tomassirio.wanderer.command.service.helper.TripStatusTransitionHandler;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripStatus;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event listener that handles database persistence for trip-related domain events.
 *
 * <p>This implements the CQRS write side by handling events and persisting changes to the database.
 * Commands publish events, and this listener performs the actual database operations.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(1) // Execute before WebSocket broadcasting
public class TripPersistenceEventListener {

    private final TripRepository tripRepository;
    private final TripEmbeddedObjectsInitializer embeddedObjectsInitializer;
    private final TripStatusTransitionHandler statusTransitionHandler;

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleTripCreated(TripCreatedEvent event) {
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

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleTripMetadataUpdated(TripMetadataUpdatedEvent event) {
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

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleTripDeleted(TripDeletedEvent event) {
        log.debug("Persisting TripDeletedEvent for trip: {}", event.getTripId());

        tripRepository.deleteById(event.getTripId());
        log.info("Trip deleted: {}", event.getTripId());
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleTripVisibilityChanged(TripVisibilityChangedEvent event) {
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

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleTripStatusChanged(TripStatusChangedEvent event) {
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
