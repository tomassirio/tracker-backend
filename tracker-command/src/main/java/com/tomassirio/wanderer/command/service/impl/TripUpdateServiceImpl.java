package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.command.dto.TripUpdateCreationRequest;
import com.tomassirio.wanderer.command.event.TripUpdatedEvent;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.repository.TripUpdateRepository;
import com.tomassirio.wanderer.command.service.TripUpdateService;
import com.tomassirio.wanderer.command.service.validator.OwnershipValidator;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripUpdate;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TripUpdateServiceImpl implements TripUpdateService {

    private final TripUpdateRepository tripUpdateRepository;
    private final TripRepository tripRepository;
    private final OwnershipValidator ownershipValidator;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public UUID createTripUpdate(UUID userId, UUID tripId, TripUpdateCreationRequest request) {
        Trip trip =
                tripRepository
                        .findById(tripId)
                        .orElseThrow(() -> new EntityNotFoundException("Trip not found"));

        ownershipValidator.validateOwnership(trip, userId, Trip::getUserId, Trip::getId, "trip");

        TripUpdate tripUpdate =
                TripUpdate.builder()
                        .trip(trip)
                        .location(request.location())
                        .battery(request.battery())
                        .message(request.message())
                        .timestamp(Instant.now())
                        .build();

        TripUpdate saved = tripUpdateRepository.save(tripUpdate);

        // Publish domain event - decoupled from WebSocket
        eventPublisher.publishEvent(
                TripUpdatedEvent.builder()
                        .tripId(tripId)
                        .latitude(request.location() != null ? request.location().getLat() : null)
                        .longitude(request.location() != null ? request.location().getLon() : null)
                        .batteryLevel(request.battery())
                        .message(request.message())
                        .build());

        return saved.getId();
    }
}
