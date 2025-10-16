package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.command.dto.TripCreationRequest;
import com.tomassirio.wanderer.command.dto.TripUpdateRequest;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.repository.UserRepository;
import com.tomassirio.wanderer.command.service.TripService;
import com.tomassirio.wanderer.command.service.helper.TripEmbeddedObjectsInitializer;
import com.tomassirio.wanderer.command.service.helper.TripStatusTransitionHandler;
import com.tomassirio.wanderer.command.service.validator.OwnershipValidator;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripStatus;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import com.tomassirio.wanderer.commons.dto.TripDTO;
import com.tomassirio.wanderer.commons.mapper.TripMapper;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TripMapper tripMapper = TripMapper.INSTANCE;
    private final TripEmbeddedObjectsInitializer embeddedObjectsInitializer;
    private final TripStatusTransitionHandler statusTransitionHandler;
    private final OwnershipValidator ownershipValidator;

    @Override
    @Transactional
    public TripDTO createTrip(UUID ownerId, TripCreationRequest request) {
        userRepository
                .findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Trip trip =
                Trip.builder()
                        .name(request.name())
                        .userId(ownerId)
                        .tripSettings(
                                embeddedObjectsInitializer.createTripSettings(request.visibility()))
                        .tripDetails(embeddedObjectsInitializer.createTripDetails())
                        .tripPlanId(null)
                        .creationTimestamp(Instant.now())
                        .enabled(true)
                        .build();

        return tripMapper.toDTO(tripRepository.save(trip));
    }

    @Override
    @Transactional
    public TripDTO updateTrip(UUID userId, UUID id, TripUpdateRequest request) {
        Trip trip =
                tripRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Trip not found"));

        ownershipValidator.validateOwnership(trip, userId, Trip::getUserId, Trip::getId, "trip");

        trip.setName(request.name());

        embeddedObjectsInitializer.ensureTripSettings(trip, request.visibility());
        trip.getTripSettings().setVisibility(request.visibility());

        embeddedObjectsInitializer.ensureTripDetails(trip);

        return tripMapper.toDTO(tripRepository.save(trip));
    }

    @Override
    @Transactional
    public void deleteTrip(UUID userId, UUID id) {
        Trip trip =
                tripRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Trip not found"));

        ownershipValidator.validateOwnership(trip, userId, Trip::getUserId, Trip::getId, "trip");

        tripRepository.deleteById(id);
    }

    @Override
    @Transactional
    public TripDTO changeVisibility(UUID userId, UUID id, TripVisibility visibility) {
        Trip trip =
                tripRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Trip not found"));

        ownershipValidator.validateOwnership(trip, userId, Trip::getUserId, Trip::getId, "trip");

        embeddedObjectsInitializer.ensureTripSettings(trip, visibility);
        trip.getTripSettings().setVisibility(visibility);

        return tripMapper.toDTO(tripRepository.save(trip));
    }

    @Override
    @Transactional
    public TripDTO changeStatus(UUID userId, UUID id, TripStatus status) {
        Trip trip =
                tripRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Trip not found"));

        ownershipValidator.validateOwnership(trip, userId, Trip::getUserId, Trip::getId, "trip");

        TripStatus previousStatus =
                embeddedObjectsInitializer.ensureTripSettingsAndGetPreviousStatus(trip, status);
        trip.getTripSettings().setTripStatus(status);

        embeddedObjectsInitializer.ensureTripDetails(trip);
        statusTransitionHandler.handleStatusTransition(trip, previousStatus, status);

        return tripMapper.toDTO(tripRepository.save(trip));
    }
}
