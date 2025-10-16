package com.tomassirio.wanderer.query.repository;

import com.tomassirio.wanderer.commons.domain.TripPlan;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripPlanRepository extends JpaRepository<TripPlan, UUID> {

    List<TripPlan> findByUserId(UUID userId);
}
