package com.tomassirio.wanderer.query.service;

import com.tomassirio.wanderer.query.dto.PromotedTripResponse;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for promoted trip query operations. Provides methods to retrieve information
 * about promoted trips.
 *
 * @author tomassirio
 * @since 0.5.0
 */
public interface PromotedTripQueryService {

    /**
     * Retrieves all promoted trips.
     *
     * @return a list of all promoted trips
     */
    List<PromotedTripResponse> getAllPromotedTrips();

    /**
     * Retrieves promotion information for a specific trip.
     *
     * @param tripId the UUID of the trip
     * @return the promoted trip response containing promotion details
     * @throws jakarta.persistence.EntityNotFoundException if the trip is not promoted
     */
    PromotedTripResponse getPromotionByTripId(UUID tripId);

    /**
     * Checks if a trip is currently promoted.
     *
     * @param tripId the UUID of the trip
     * @return true if the trip is promoted, false otherwise
     */
    boolean isTripPromoted(UUID tripId);
}
