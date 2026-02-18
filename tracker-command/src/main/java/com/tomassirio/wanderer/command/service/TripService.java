package com.tomassirio.wanderer.command.service;

import com.tomassirio.wanderer.command.controller.request.TripCreationRequest;
import com.tomassirio.wanderer.command.controller.request.TripUpdateRequest;
import com.tomassirio.wanderer.commons.domain.TripStatus;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
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
     * @return the UUID of the created trip
     * @throws IllegalArgumentException if the request contains invalid data
     */
    UUID createTrip(UUID ownerId, TripCreationRequest request);

    /**
     * Creates a new trip from an existing trip plan.
     *
     * @param userId the UUID of the user creating the trip
     * @param tripPlanId the UUID of the trip plan to create a trip from
     * @param visibility the visibility setting for the new trip
     * @return the UUID of the created trip
     * @throws jakarta.persistence.EntityNotFoundException if no trip plan exists with the given ID
     * @throws org.springframework.security.access.AccessDeniedException if user doesn't own the
     *     trip plan
     */
    UUID createTripFromPlan(UUID userId, UUID tripPlanId, TripVisibility visibility);

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
     * @return the UUID of the updated trip
     * @throws jakarta.persistence.EntityNotFoundException if no trip exists with the given ID
     * @throws IllegalArgumentException if the request contains invalid data
     * @throws org.springframework.security.access.AccessDeniedException if user doesn't own the
     *     trip
     */
    UUID updateTrip(UUID userId, UUID id, TripUpdateRequest request);

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
     * @return the UUID of the trip
     * @throws jakarta.persistence.EntityNotFoundException if no trip exists with the given ID
     * @throws org.springframework.security.access.AccessDeniedException if user doesn't own the
     *     trip
     */
    UUID changeVisibility(UUID userId, UUID id, TripVisibility visibility);

    /**
     * Changes the status of a trip (start, pause, finish).
     *
     * @param userId the UUID of the user making the request (for ownership validation)
     * @param id the UUID of the trip to update
     * @param status the new status
     * @return the UUID of the trip
     * @throws jakarta.persistence.EntityNotFoundException if no trip exists with the given ID
     * @throws org.springframework.security.access.AccessDeniedException if user doesn't own the
     *     trip
     */
    UUID changeStatus(UUID userId, UUID id, TripStatus status);

    /**
     * Promotes a trip. Only administrators can promote trips.
     *
     * @param adminId the UUID of the admin making the request
     * @param tripId the UUID of the trip to promote
     * @param donationLink optional donation link for the promoted trip
     * @return the UUID of the promoted trip record
     * @throws jakarta.persistence.EntityNotFoundException if no trip exists with the given ID
     * @throws IllegalStateException if the trip is already promoted
     */
    UUID promoteTrip(UUID adminId, UUID tripId, String donationLink);
}
