package com.tomassirio.wanderer.command.handler;

import com.tomassirio.wanderer.command.event.TripStatusChangedEvent;
import com.tomassirio.wanderer.command.repository.ActiveTripRepository;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.service.helper.TripEmbeddedObjectsInitializer;
import com.tomassirio.wanderer.command.service.helper.TripStatusTransitionHandler;
import com.tomassirio.wanderer.commons.domain.ActiveTrip;
import com.tomassirio.wanderer.commons.domain.TripStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
    private final ActiveTripRepository activeTripRepository;
    private final TripEmbeddedObjectsInitializer embeddedObjectsInitializer;
    private final TripStatusTransitionHandler statusTransitionHandler;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
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

                            // Manage active_trips table based on status
                            manageActiveTrip(trip.getUserId(), trip.getId(), newStatus);

                            // No need to call save() - entity is managed and will be flushed
                            // automatically
                            log.info("Trip status changed: {}", event.getTripId());
                        });
    }

    /**
     * Manages the active_trips table based on trip status changes.
     *
     * <p>Adds a record when status becomes IN_PROGRESS, removes it when status changes from
     * IN_PROGRESS to anything else.
     *
     * @param userId the ID of the user who owns the trip
     * @param tripId the ID of the trip
     * @param newStatus the new status of the trip
     */
    private void manageActiveTrip(
            java.util.UUID userId, java.util.UUID tripId, TripStatus newStatus) {
        if (newStatus == TripStatus.IN_PROGRESS) {
            // Add or update active trip record
            ActiveTrip activeTrip =
                    activeTripRepository
                            .findById(userId)
                            .orElse(ActiveTrip.builder().userId(userId).build());
            activeTrip.setTripId(tripId);
            activeTripRepository.save(activeTrip);
            log.debug("Set active trip for user {}: {}", userId, tripId);
        } else {
            // Remove active trip record if exists
            activeTripRepository
                    .findById(userId)
                    .filter(activeTrip -> activeTrip.getTripId().equals(tripId))
                    .ifPresent(
                            activeTrip -> {
                                activeTripRepository.delete(activeTrip);
                                log.debug("Removed active trip for user {}", userId);
                            });
        }
    }
}
