package com.tomassirio.wanderer.command.service;

import com.tomassirio.wanderer.command.dto.TripCreationRequest;
import com.tomassirio.wanderer.command.dto.TripUpdateRequest;
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
     * Updates an existing trip with new details.
     *
     * <p>This method updates all fields of an existing trip including name, dates, distance, and
     * locations. If new location coordinates are provided, new location entities will be created
     * and associated with the trip.
     *
     * @param id the UUID of the trip to update
     * @param request the trip update request containing the new trip details
     * @return a {@link TripDTO} containing the updated trip data
     * @throws jakarta.persistence.EntityNotFoundException if no trip exists with the given ID
     * @throws IllegalArgumentException if the request contains invalid data
     */
    TripDTO updateTrip(UUID id, TripUpdateRequest request);

    /**
     * Deletes a trip by its ID.
     *
     * <p>This operation will cascade and delete all associated data including locations and
     * messages related to this trip.
     *
     * @param id the UUID of the trip to delete
     * @throws jakarta.persistence.EntityNotFoundException if no trip exists with the given ID
     */
    void deleteTrip(UUID id);
}
