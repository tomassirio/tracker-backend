package com.tomassirio.wanderer.command.service;

import com.tomassirio.wanderer.command.controller.request.TripUpdateCreationRequest;
import java.util.UUID;

/**
 * Service interface for managing trip update operations in the command side.
 *
 * @since 0.2.0
 */
public interface TripUpdateService {

    /**
     * Creates a new trip update (location, message, battery).
     *
     * @param userId the UUID of the user making the request (for ownership validation)
     * @param tripId the UUID of the trip to add the update to
     * @param request the trip update creation request containing location and optional data
     * @return the UUID of the created trip update
     * @throws jakarta.persistence.EntityNotFoundException if no trip exists with the given ID
     * @throws org.springframework.security.access.AccessDeniedException if user doesn't own the
     *     trip
     */
    UUID createTripUpdate(UUID userId, UUID tripId, TripUpdateCreationRequest request);
}
