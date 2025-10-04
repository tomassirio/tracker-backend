package com.tomassirio.wanderer.command.repository;

import com.tomassirio.wanderer.commons.domain.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID> {

    /**
     * Check if a trip exists by its ID.
     */
    boolean existsById(UUID id);
}
