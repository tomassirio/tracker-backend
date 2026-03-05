package com.tomassirio.wanderer.query.service;

import com.tomassirio.wanderer.commons.dto.TripPlanDTO;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for querying trip plan data.
 *
 * @since 0.3.0
 */
public interface TripPlanService {

    /**
     * Retrieves a single trip plan by its unique identifier.
     *
     * @param planId the UUID of the trip plan to retrieve
     * @return a {@link TripPlanDTO} containing the trip plan data
     * @throws jakarta.persistence.EntityNotFoundException if no trip plan exists with the given ID
     */
    TripPlanDTO getTripPlan(UUID planId);

    /**
     * Retrieves all trip plans that belong to the given user.
     *
     * @param userId the UUID of the owner/user
     * @return a list of {@link TripPlanDTO} objects representing trip plans owned by the user
     */
    List<TripPlanDTO> getTripPlansForUser(UUID userId);
}
