package com.tomassirio.wanderer.command.handler.persistence;

import com.tomassirio.wanderer.command.event.TripUpdatedEvent;
import com.tomassirio.wanderer.command.handler.EventHandler;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.repository.TripUpdateRepository;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripUpdate;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class TripUpdatedEventPersistenceHandler implements EventHandler<TripUpdatedEvent> {

    private final TripUpdateRepository tripUpdateRepository;
    private final TripRepository tripRepository;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(TripUpdatedEvent event) {
        log.debug("Persisting TripUpdatedEvent for trip: {}", event.getTripId());

        Trip trip =
                tripRepository
                        .findById(event.getTripId())
                        .orElseThrow(() -> new EntityNotFoundException("Trip not found"));

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
