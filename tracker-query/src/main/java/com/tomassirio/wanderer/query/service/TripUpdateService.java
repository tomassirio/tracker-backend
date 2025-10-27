package com.tomassirio.wanderer.query.service;

import com.tomassirio.wanderer.commons.dto.TripUpdateDTO;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for querying trip update data in the query side of the CQRS architecture.
 *
 * <p>This service handles all read operations for trip updates. It provides methods to retrieve
 * trip update information without modifying the underlying data.
 *
 * @author tomassirio
 * @since 0.4.2
 */
public interface TripUpdateService {

    /**
     * Retrieves a single trip update by its unique identifier.
     *
     * @param id the UUID of the trip update to retrieve
     * @return a {@link TripUpdateDTO} containing the trip update data
     * @throws jakarta.persistence.EntityNotFoundException if no trip update exists with the given
     *     ID
     */
    TripUpdateDTO getTripUpdate(UUID id);

    /**
     * Retrieves all trip updates for a specific trip, ordered by timestamp descending (most recent
     * first).
     *
     * @param tripId the UUID of the trip
     * @return a list of {@link TripUpdateDTO} objects representing the trip updates, or an empty
     *     list if none exist
     */
    List<TripUpdateDTO> getTripUpdatesForTrip(UUID tripId);
}

