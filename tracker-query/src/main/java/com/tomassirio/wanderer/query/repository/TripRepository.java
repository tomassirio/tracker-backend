package com.tomassirio.wanderer.query.repository;

import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripStatus;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID> {

    List<Trip> findByTripSettingsVisibility(TripVisibility visibility);

    List<Trip> findByUserId(UUID userId);

    /**
     * Find trips by user ID that are visible to the requester based on visibility rules. Returns
     * PUBLIC and PROTECTED trips for any requester.
     */
    @Query(
            "SELECT t FROM Trip t WHERE t.userId = :userId AND t.tripSettings.visibility IN :visibilities")
    List<Trip> findByUserIdAndVisibilityIn(
            @Param("userId") UUID userId, @Param("visibilities") List<TripVisibility> visibilities);

    /** Find all public trips that are currently in progress. */
    @Query(
            "SELECT t FROM Trip t WHERE t.tripSettings.visibility = :visibility AND t.tripSettings.tripStatus = :status")
    List<Trip> findByVisibilityAndStatus(
            @Param("visibility") TripVisibility visibility, @Param("status") TripStatus status);
}
