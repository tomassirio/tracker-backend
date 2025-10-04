package com.tomassirio.wanderer.command.repository;

import com.tomassirio.wanderer.commons.domain.Trip;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID> {

    /** Check if a trip exists by its ID. */
    boolean existsById(UUID id);
}
