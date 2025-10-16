package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.command.dto.TripPlanCreationRequest;
import com.tomassirio.wanderer.command.dto.TripPlanUpdateRequest;
import com.tomassirio.wanderer.command.repository.TripPlanRepository;
import com.tomassirio.wanderer.command.service.TripPlanMetadataProcessor;
import com.tomassirio.wanderer.command.service.TripPlanService;
import com.tomassirio.wanderer.command.service.validator.OwnershipValidator;
import com.tomassirio.wanderer.command.service.validator.TripPlanValidator;
import com.tomassirio.wanderer.commons.domain.TripPlan;
import com.tomassirio.wanderer.commons.dto.TripPlanDTO;
import com.tomassirio.wanderer.commons.mapper.TripPlanMapper;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TripPlanServiceImpl implements TripPlanService {

    private final TripPlanRepository tripPlanRepository;
    private final TripPlanMetadataProcessor metadataProcessor;
    private final TripPlanMapper tripPlanMapper = TripPlanMapper.INSTANCE;;
    private final OwnershipValidator ownershipValidator;
    private final TripPlanValidator tripPlanValidator;

    @Override
    @Transactional
    public TripPlanDTO createTripPlan(UUID userId, TripPlanCreationRequest request) {
        // Validate dates
        tripPlanValidator.validateDates(request.startDate(), request.endDate());

        TripPlan tripPlan =
                TripPlan.builder()
                        .name(request.name())
                        .planType(request.planType())
                        .userId(userId)
                        .createdTimestamp(Instant.now())
                        .startDate(request.startDate())
                        .endDate(request.endDate())
                        .startLocation(request.startLocation())
                        .endLocation(request.endLocation())
                        .waypoints(Optional.ofNullable(request.waypoints()).orElse(List.of()))
                        .metadata(new HashMap<>())
                        .build();

        metadataProcessor.applyMetadata(tripPlan, tripPlan.getMetadata());

        return tripPlanMapper.toDTO(tripPlanRepository.save(tripPlan));
    }

    @Override
    @Transactional
    public TripPlanDTO updateTripPlan(UUID userId, UUID planId, TripPlanUpdateRequest request) {
        TripPlan tripPlan =
                tripPlanRepository
                        .findById(planId)
                        .orElseThrow(() -> new EntityNotFoundException("Trip plan not found"));

        ownershipValidator.validateOwnership(
                tripPlan, userId, TripPlan::getUserId, TripPlan::getId, "trip plan");

        tripPlan.setName(request.name());
        tripPlan.setStartDate(request.startDate());
        tripPlan.setEndDate(request.endDate());
        tripPlan.setStartLocation(request.startLocation());
        tripPlan.setEndLocation(request.endLocation());
        tripPlan.setWaypoints(
                request.waypoints() != null ? request.waypoints() : new java.util.ArrayList<>());

        // Re-validate metadata for the plan type
        metadataProcessor.applyMetadata(tripPlan, tripPlan.getMetadata());

        return tripPlanMapper.toDTO(tripPlanRepository.save(tripPlan));
    }

    @Override
    @Transactional
    public void deleteTripPlan(UUID userId, UUID planId) {
        TripPlan tripPlan =
                tripPlanRepository
                        .findById(planId)
                        .orElseThrow(() -> new EntityNotFoundException("Trip plan not found"));

        ownershipValidator.validateOwnership(
                tripPlan, userId, TripPlan::getUserId, TripPlan::getId, "trip plan");

        tripPlanRepository.deleteById(planId);
    }
}
