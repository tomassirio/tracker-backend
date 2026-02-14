package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.command.dto.TripCreationRequest;
import com.tomassirio.wanderer.command.dto.TripUpdateRequest;
import com.tomassirio.wanderer.command.event.TripCreatedEvent;
import com.tomassirio.wanderer.command.event.TripDeletedEvent;
import com.tomassirio.wanderer.command.event.TripMetadataUpdatedEvent;
import com.tomassirio.wanderer.command.event.TripStatusChangedEvent;
import com.tomassirio.wanderer.command.event.TripVisibilityChangedEvent;
import com.tomassirio.wanderer.command.repository.TripPlanRepository;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.repository.UserRepository;
import com.tomassirio.wanderer.command.service.TripService;
import com.tomassirio.wanderer.command.service.validator.OwnershipValidator;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripDetails;
import com.tomassirio.wanderer.commons.domain.TripPlan;
import com.tomassirio.wanderer.commons.domain.TripStatus;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import com.tomassirio.wanderer.commons.dto.TripDTO;
import com.tomassirio.wanderer.commons.dto.TripDetailsDTO;
import com.tomassirio.wanderer.commons.dto.TripSettingsDTO;
import com.tomassirio.wanderer.commons.mapper.TripMapper;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TripPlanRepository tripPlanRepository;
    private final TripMapper tripMapper = TripMapper.INSTANCE;
    private final OwnershipValidator ownershipValidator;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public TripDTO createTrip(UUID ownerId, TripCreationRequest request) {
        // Validate user exists
        userRepository
                .findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Pre-generate ID for the trip
        UUID tripId = UUID.randomUUID();
        Instant creationTimestamp = Instant.now();

        // Publish event - persistence handler will write to DB
        eventPublisher.publishEvent(
                TripCreatedEvent.builder()
                        .tripId(tripId)
                        .tripName(request.name())
                        .ownerId(ownerId)
                        .visibility(request.visibility().name())
                        .tripPlanId(null)
                        .creationTimestamp(creationTimestamp)
                        .build());

        // Return DTO with available data (eventual consistency - full data will be in DB
        // eventually)
        return new TripDTO(
                tripId.toString(),
                request.name(),
                ownerId.toString(),
                null, // username not available in command
                new TripSettingsDTO(TripStatus.CREATED, request.visibility(), null),
                new TripDetailsDTO(
                        null, null, null, null, List.of()), // details with nulls initially
                null, // tripPlanId
                List.of(), // comments
                List.of(), // tripUpdates
                creationTimestamp,
                true);
    }

    @Override
    public TripDTO updateTrip(UUID userId, UUID id, TripUpdateRequest request) {
        // Validate trip exists and ownership
        Trip trip =
                tripRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Trip not found"));

        ownershipValidator.validateOwnership(trip, userId, Trip::getUserId, Trip::getId, "trip");

        // Publish event - persistence handler will write to DB
        eventPublisher.publishEvent(
                TripMetadataUpdatedEvent.builder()
                        .tripId(id)
                        .tripName(request.name())
                        .visibility(request.visibility().name())
                        .build());

        // Return DTO with updated data (eventual consistency)
        return new TripDTO(
                id.toString(),
                request.name(),
                userId.toString(),
                null,
                new TripSettingsDTO(
                        trip.getTripSettings() != null
                                ? trip.getTripSettings().getTripStatus()
                                : null,
                        request.visibility(),
                        null),
                trip.getTripDetails() != null
                        ? new TripDetailsDTO(
                                trip.getTripDetails().getStartTimestamp(),
                                trip.getTripDetails().getEndTimestamp(),
                                trip.getTripDetails().getStartLocation(),
                                trip.getTripDetails().getEndLocation(),
                                trip.getTripDetails().getWaypoints())
                        : null,
                null,
                List.of(),
                List.of(),
                trip.getCreationTimestamp(),
                trip.getEnabled());
    }

    @Override
    public void deleteTrip(UUID userId, UUID id) {
        // Validate trip exists and ownership
        Trip trip =
                tripRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Trip not found"));

        ownershipValidator.validateOwnership(trip, userId, Trip::getUserId, Trip::getId, "trip");

        // Publish event - persistence handler will delete from DB
        eventPublisher.publishEvent(TripDeletedEvent.builder().tripId(id).ownerId(userId).build());
    }

    @Override
    public TripDTO changeVisibility(UUID userId, UUID id, TripVisibility visibility) {
        // Validate trip exists and ownership
        Trip trip =
                tripRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Trip not found"));

        ownershipValidator.validateOwnership(trip, userId, Trip::getUserId, Trip::getId, "trip");

        TripVisibility previousVisibility =
                trip.getTripSettings() != null ? trip.getTripSettings().getVisibility() : null;

        // Publish event - persistence handler will write to DB
        eventPublisher.publishEvent(
                TripVisibilityChangedEvent.builder()
                        .tripId(id)
                        .newVisibility(visibility.name())
                        .previousVisibility(
                                previousVisibility != null ? previousVisibility.name() : null)
                        .build());

        // Return DTO with updated visibility (eventual consistency)
        return new TripDTO(
                id.toString(),
                trip.getName(),
                userId.toString(),
                null,
                new TripSettingsDTO(
                        trip.getTripSettings() != null
                                ? trip.getTripSettings().getTripStatus()
                                : null,
                        visibility,
                        null),
                trip.getTripDetails() != null
                        ? new TripDetailsDTO(
                                trip.getTripDetails().getStartTimestamp(),
                                trip.getTripDetails().getEndTimestamp(),
                                trip.getTripDetails().getStartLocation(),
                                trip.getTripDetails().getEndLocation(),
                                trip.getTripDetails().getWaypoints())
                        : null,
                null,
                List.of(),
                List.of(),
                trip.getCreationTimestamp(),
                trip.getEnabled());
    }

    @Override
    public TripDTO changeStatus(UUID userId, UUID id, TripStatus status) {
        // Validate trip exists and ownership
        Trip trip =
                tripRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Trip not found"));

        ownershipValidator.validateOwnership(trip, userId, Trip::getUserId, Trip::getId, "trip");

        TripStatus previousStatus =
                trip.getTripSettings() != null ? trip.getTripSettings().getTripStatus() : null;

        // Publish event - persistence handler will write to DB
        eventPublisher.publishEvent(
                TripStatusChangedEvent.builder()
                        .tripId(id)
                        .newStatus(status.name())
                        .previousStatus(previousStatus != null ? previousStatus.name() : null)
                        .build());

        // Return DTO with updated status (eventual consistency)
        return new TripDTO(
                id.toString(),
                trip.getName(),
                userId.toString(),
                null,
                new TripSettingsDTO(
                        status,
                        trip.getTripSettings() != null
                                ? trip.getTripSettings().getVisibility()
                                : null,
                        null),
                trip.getTripDetails() != null
                        ? new TripDetailsDTO(
                                trip.getTripDetails().getStartTimestamp(),
                                trip.getTripDetails().getEndTimestamp(),
                                trip.getTripDetails().getStartLocation(),
                                trip.getTripDetails().getEndLocation(),
                                trip.getTripDetails().getWaypoints())
                        : null,
                null,
                List.of(),
                List.of(),
                trip.getCreationTimestamp(),
                trip.getEnabled());
    }

    @Override
    public TripDTO createTripFromPlan(UUID userId, UUID tripPlanId, TripVisibility visibility) {
        // Validate user exists
        userRepository
                .findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Fetch and validate trip plan ownership
        TripPlan tripPlan =
                tripPlanRepository
                        .findById(tripPlanId)
                        .orElseThrow(() -> new EntityNotFoundException("Trip plan not found"));

        ownershipValidator.validateOwnership(
                tripPlan, userId, TripPlan::getUserId, TripPlan::getId, "trip plan");

        // Create trip details from trip plan data
        TripDetails tripDetails =
                TripDetails.builder()
                        .startLocation(tripPlan.getStartLocation())
                        .endLocation(tripPlan.getEndLocation())
                        .waypoints(
                                tripPlan.getWaypoints() != null
                                        ? tripPlan.getWaypoints()
                                        : List.of())
                        .startTimestamp(
                                tripPlan.getStartDate().atStartOfDay().toInstant(ZoneOffset.UTC))
                        .endTimestamp(
                                tripPlan.getEndDate().atStartOfDay().toInstant(ZoneOffset.UTC))
                        .build();

        // Create trip from plan
        Trip trip =
                Trip.builder()
                        .name(tripPlan.getName())
                        .userId(userId)
                        .tripSettings(null) // Will be created by persistence handler
                        .tripDetails(tripDetails)
                        .tripPlanId(tripPlan.getId())
                        .creationTimestamp(Instant.now())
                        .enabled(true)
                        .build();

        return tripMapper.toDTO(trip);
    }
}
