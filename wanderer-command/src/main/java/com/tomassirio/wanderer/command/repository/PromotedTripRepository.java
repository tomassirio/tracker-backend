package com.tomassirio.wanderer.command.repository;

import com.tomassirio.wanderer.commons.domain.PromotedTrip;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing PromotedTrip entities.
 *
 * <p>This repository handles the persistence of promoted trips, which are trips that have been
 * promoted by administrators and may include donation links.
 *
 * @author tomassirio
 * @since 0.5.0
 */
@Repository
public interface PromotedTripRepository extends JpaRepository<PromotedTrip, UUID> {

    /**
     * Finds a promoted trip by its trip ID.
     *
     * @param tripId the UUID of the trip
     * @return an Optional containing the PromotedTrip if found
     */
    Optional<PromotedTrip> findByTripId(UUID tripId);

    /**
     * Checks if a trip has been promoted.
     *
     * @param tripId the UUID of the trip
     * @return true if the trip is promoted, false otherwise
     */
    boolean existsByTripId(UUID tripId);

    /**
     * Deletes a promoted trip by its trip ID.
     *
     * @param tripId the UUID of the trip
     */
    void deleteByTripId(UUID tripId);
}
