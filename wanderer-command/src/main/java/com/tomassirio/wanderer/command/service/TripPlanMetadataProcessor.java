package com.tomassirio.wanderer.command.service;

import com.tomassirio.wanderer.commons.domain.TripPlan;
import com.tomassirio.wanderer.commons.domain.TripPlanType;
import java.util.Map;

/**
 * Service for processing and validating trip plan metadata based on plan type.
 *
 * @since 0.2.0
 */
public interface TripPlanMetadataProcessor {

    /**
     * Processes and validates metadata for a specific plan type.
     *
     * @param planType the type of plan
     * @param metadata the metadata to process
     * @return processed and validated metadata
     * @throws IllegalArgumentException if metadata is invalid for the plan type
     */
    Map<String, Object> processMetadata(TripPlanType planType, Map<String, Object> metadata);

    /**
     * Applies metadata to a trip plan entity.
     *
     * @param tripPlan the trip plan to update
     * @param metadata the metadata to apply
     */
    void applyMetadata(TripPlan tripPlan, Map<String, Object> metadata);
}
