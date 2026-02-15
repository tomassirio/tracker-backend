package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.command.dto.TripUpdateCreationRequest;
import com.tomassirio.wanderer.command.event.TripUpdatedEvent;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.service.TripUpdateService;
import com.tomassirio.wanderer.command.service.validator.OwnershipValidator;
import com.tomassirio.wanderer.commons.domain.Trip;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TripUpdateServiceImpl implements TripUpdateService {

    private final TripRepository tripRepository;
    private final OwnershipValidator ownershipValidator;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public UUID createTripUpdate(UUID userId, UUID tripId, TripUpdateCreationRequest request) {
        Trip trip =
                tripRepository
                        .findById(tripId)
                        .orElseThrow(() -> new EntityNotFoundException("Trip not found"));

        ownershipValidator.validateOwnership(trip, userId, Trip::getUserId, Trip::getId, "trip");

        // Pre-generate ID and timestamp
        UUID tripUpdateId = UUID.randomUUID();
        Instant timestamp = Instant.now();

        // Publish event - persistence handler will write to DB
        eventPublisher.publishEvent(
                TripUpdatedEvent.builder()
                        .tripUpdateId(tripUpdateId)
                        .tripId(tripId)
                        .location(request.location())
                        .batteryLevel(request.battery())
                        .message(request.message())
                        .timestamp(timestamp)
                        .build());

        return tripUpdateId;
    }
}
