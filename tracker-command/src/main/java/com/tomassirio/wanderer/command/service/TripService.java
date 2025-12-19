package com.tomassirio.wanderer.command.service;

import com.tomassirio.wanderer.command.dto.TripCreationRequest;
import com.tomassirio.wanderer.command.dto.TripFromPlanCreationRequest;
import com.tomassirio.wanderer.command.dto.TripUpdateRequest;
import com.tomassirio.wanderer.commons.domain.TripStatus;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import com.tomassirio.wanderer.commons.dto.TripDTO;
import java.util.UUID;

/**
 * Service interface for managing trip operations in the command side of the CQRS architecture.
 *
 * <p>This service handles all write operations (create, update, delete) for trips. Trips represent
 * journeys or travel plans with starting and ending locations defined by GPS coordinates.
 *
 * @author tomassirio
 * @since 0.1.0
 */
public interface TripService {

    /**
     * Creates a new trip with the provided details.
     *
     * @param ownerId the UUID of the user creating the trip
     * @param request the trip creation request containing trip details and location coordinates
     * @return a {@link TripDTO} containing the created trip with generated ID and all associated
     *     data
     * @throws IllegalArgumentException if the request contains invalid data
     */
    TripDTO createTrip(UUID ownerId, TripCreationRequest request);

    /**
     * Creates a new trip from an existing trip plan.
     *
     * @param userId the UUID of the user creating the trip
     * @param tripPlanId the UUID of the trip plan to create a trip from
     * @param request the trip from plan creation request containing visibility
     * @return a {@link TripDTO} containing the created trip with data inherited from the trip plan
     * @throws jakarta.persistence.EntityNotFoundException if no trip plan exists with the given ID
     * @throws org.springframework.security.access.AccessDeniedException if user doesn't own the
     *     trip plan
     */
    TripDTO createTripFromPlan(UUID userId, UUID tripPlanId, TripFromPlanCreationRequest request);

    /**
     * Updates an existing trip with new details.
     *
     * <p>This method updates all fields of an existing trip including name, dates, distance, and
     * locations. If new location coordinates are provided, new location entities will be created
     * and associated with the trip.
     *
     * @param userId the UUID of the user making the request (for ownership validation)
     * @param id the UUID of the trip to update
     * @param request the trip update request containing the new trip details
     * @return a {@link TripDTO} containing the updated trip data
     * @throws jakarta.persistence.EntityNotFoundException if no trip exists with the given ID
     * @throws IllegalArgumentException if the request contains invalid data
     * @throws org.springframework.security.access.AccessDeniedException if user doesn't own the
     *     trip
     */
    TripDTO updateTrip(UUID userId, UUID id, TripUpdateRequest request);

    /**
     * Deletes a trip by its ID.
     *
     * <p>This operation will cascade and delete all associated data including locations and
     * messages related to this trip.
     *
     * @param userId the UUID of the user making the request (for ownership validation)
     * @param id the UUID of the trip to delete
     * @throws jakarta.persistence.EntityNotFoundException if no trip exists with the given ID
     * @throws org.springframework.security.access.AccessDeniedException if user doesn't own the
     *     trip
     */
    void deleteTrip(UUID userId, UUID id);

    /**
     * Changes the visibility of a trip.
     *
     * @param userId the UUID of the user making the request (for ownership validation)
     * @param id the UUID of the trip to update
     * @param visibility the new visibility setting
     * @return a {@link TripDTO} containing the updated trip data
     * @throws jakarta.persistence.EntityNotFoundException if no trip exists with the given ID
     * @throws org.springframework.security.access.AccessDeniedException if user doesn't own the
     *     trip
     */
    TripDTO changeVisibility(UUID userId, UUID id, TripVisibility visibility);

    /**
     * Changes the status of a trip (start, pause, finish).
     *
     * @param userId the UUID of the user making the request (for ownership validation)
     * @param id the UUID of the trip to update
     * @param status the new status
     * @return a {@link TripDTO} containing the updated trip data
     * @throws jakarta.persistence.EntityNotFoundException if no trip exists with the given ID
     * @throws org.springframework.security.access.AccessDeniedException if user doesn't own the
     *     trip
     */
    TripDTO changeStatus(UUID userId, UUID id, TripStatus status);
}
