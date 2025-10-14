package com.tomassirio.wanderer.command.service;

import com.tomassirio.wanderer.command.dto.TripPlanCreationRequest;
import com.tomassirio.wanderer.command.dto.TripPlanUpdateRequest;
import com.tomassirio.wanderer.commons.dto.TripPlanDTO;
import java.util.UUID;

/**
 * Service interface for managing trip plan operations in the command side.
 *
 * @since 0.2.0
 */
public interface TripPlanService {

    /**
     * Creates a new trip plan.
     *
     * @param userId the UUID of the user creating the plan
     * @param request the trip plan creation request containing plan details
     * @return a {@link TripPlanDTO} containing the created trip plan
     * @throws IllegalArgumentException if the request contains invalid data
     */
    TripPlanDTO createTripPlan(UUID userId, TripPlanCreationRequest request);

    /**
     * Updates an existing trip plan.
     *
     * @param userId the UUID of the user making the request (for ownership validation)
     * @param planId the UUID of the plan to update
     * @param request the trip plan update request containing the new plan details
     * @return a {@link TripPlanDTO} containing the updated trip plan
     * @throws jakarta.persistence.EntityNotFoundException if no plan exists with the given ID
     * @throws org.springframework.security.access.AccessDeniedException if user doesn't own the
     *     plan
     */
    TripPlanDTO updateTripPlan(UUID userId, UUID planId, TripPlanUpdateRequest request);

    /**
     * Deletes a trip plan by its ID.
     *
     * @param userId the UUID of the user making the request (for ownership validation)
     * @param planId the UUID of the plan to delete
     * @throws jakarta.persistence.EntityNotFoundException if no plan exists with the given ID
     * @throws org.springframework.security.access.AccessDeniedException if user doesn't own the
     *     plan
     */
    void deleteTripPlan(UUID userId, UUID planId);
}
