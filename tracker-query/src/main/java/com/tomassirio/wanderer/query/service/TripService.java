package com.tomassirio.wanderer.query.service;

import com.tomassirio.wanderer.commons.dto.TripDTO;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for querying trip data in the query side of the CQRS architecture.
 *
 * <p>This service handles all read operations for trips. It provides methods to retrieve trip
 * information without modifying the underlying data.
 *
 * @author tomassirio
 * @since 0.1.0
 */
public interface TripService {

    /**
     * Retrieves a single trip by its unique identifier.
     *
     * @param id the UUID of the trip to retrieve
     * @return a {@link TripDTO} containing the trip data
     * @throws jakarta.persistence.EntityNotFoundException if no trip exists with the given ID
     */
    TripDTO getTrip(UUID id);

    /**
     * Retrieves all trips in the system.
     *
     * <p>This method returns all trips regardless of visibility or status. The results are mapped
     * to DTOs for safe transmission to clients.
     *
     * @return a list of {@link TripDTO} objects representing all trips, or an empty list if no
     *     trips exist
     */
    List<TripDTO> getAllTrips();

    /**
     * Retrieves all trips with PUBLIC visibility.
     *
     * <p>This method is intended for unauthenticated users or public browsing. It returns only the
     * trips that are marked as PUBLIC, ensuring that sensitive or private trip data is not exposed.
     *
     * @return a list of {@link TripDTO} objects representing all public trips, or an empty list if
     *     no public trips exist
     */
    List<TripDTO> getPublicTrips();

    /**
     * Retrieves all trips that belong to the given user.
     *
     * @param userId the UUID of the owner/user
     * @return a list of {@link TripDTO} objects representing trips owned by the user, or an empty
     *     list if none exist
     */
    List<TripDTO> getTripsForUser(UUID userId);

    /**
     * Retrieves trips by another user, respecting visibility rules. Returns PUBLIC trips and
     * PROTECTED trips if the requesting user is friends with the trip owner.
     *
     * @param userId the UUID of the user whose trips to retrieve
     * @param requestingUserId the UUID of the user making the request (optional)
     * @return a list of {@link TripDTO} objects representing visible trips owned by the user
     */
    List<TripDTO> getTripsForUserWithVisibility(UUID userId, UUID requestingUserId);

    /**
     * Retrieves all ongoing public trips (trips that are PUBLIC and IN_PROGRESS). If a requesting
     * user ID is provided, trips from followed users are prioritized.
     *
     * @param requestingUserId the UUID of the user making the request (optional)
     * @return a list of {@link TripDTO} objects representing ongoing public trips
     */
    List<TripDTO> getOngoingPublicTrips(UUID requestingUserId);
}
