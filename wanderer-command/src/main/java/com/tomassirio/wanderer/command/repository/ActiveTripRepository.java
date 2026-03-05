package com.tomassirio.wanderer.command.repository;

import com.tomassirio.wanderer.commons.domain.ActiveTrip;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing ActiveTrip entities.
 *
 * <p>This repository enforces the business rule that a user can have only one trip in progress at a
 * time by maintaining active trip records.
 *
 * @author tomassirio
 * @since 0.4.0
 */
@Repository
public interface ActiveTripRepository extends JpaRepository<ActiveTrip, UUID> {}
