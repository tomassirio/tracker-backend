package com.tomassirio.wanderer.command.handler.persistence;

import com.tomassirio.wanderer.command.event.Broadcastable;
import com.tomassirio.wanderer.command.event.TripCreatedEvent;
import com.tomassirio.wanderer.command.handler.EventHandler;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.service.helper.TripEmbeddedObjectsInitializer;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event handler for TripCreatedEvent that handles both persistence and WebSocket broadcasting.
 *
 * <p>This handler persists the trip during the transaction and broadcasts via WebSocket after the
 * transaction commits.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TripCreatedEventPersistenceHandler implements EventHandler<TripCreatedEvent> {

    private final TripRepository tripRepository;
    private final TripEmbeddedObjectsInitializer embeddedObjectsInitializer;
    private final WebSocketEventService webSocketEventService;

    @Override
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
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

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void broadcast(TripCreatedEvent event) {
        if (event instanceof Broadcastable broadcastable) {
            log.debug("Broadcasting TripCreatedEvent for trip: {}", event.getTripId());
            webSocketEventService.broadcast(broadcastable);
        }
    }
}
