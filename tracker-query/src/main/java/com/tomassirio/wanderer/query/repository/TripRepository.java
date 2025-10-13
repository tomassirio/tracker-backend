package com.tomassirio.wanderer.query.repository;

import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID> {

    List<Trip> findByVisibility(TripVisibility visibility);

    // Added: find all trips that belong to a specific owner (by owner id)
    List<Trip> findByOwnerId(UUID ownerId);
}
