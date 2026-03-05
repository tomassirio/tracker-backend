package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.command.service.TripPlanMetadataProcessor;
import com.tomassirio.wanderer.commons.domain.TripPlan;
import com.tomassirio.wanderer.commons.domain.TripPlanType;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Default implementation of TripPlanMetadataProcessor. Handles validation and processing of
 * plan-specific metadata.
 *
 * @since 0.2.0
 */
@Service
@Slf4j
public class TripPlanMetadataProcessorImpl implements TripPlanMetadataProcessor {

    private static final String DISTANCE_PER_DAY_KEY = "distancePerDay";

    @Override
    public Map<String, Object> processMetadata(
            TripPlanType planType, Map<String, Object> metadata) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }

        return switch (planType) {
            case MULTI_DAY -> processMultiDayMetadata(metadata);
            case SIMPLE -> processSimpleMetadata(metadata);
        };
    }

    @Override
    public void applyMetadata(TripPlan tripPlan, Map<String, Object> metadata) {
        if (tripPlan == null) {
            return;
        }

        Map<String, Object> processedMetadata = processMetadata(tripPlan.getPlanType(), metadata);
        tripPlan.setMetadata(processedMetadata);
    }

    private Map<String, Object> processMultiDayMetadata(Map<String, Object> metadata) {
        Map<String, Object> processed = new HashMap<>(metadata);

        // Validate distance_per_day if present
        if (processed.containsKey(DISTANCE_PER_DAY_KEY)) {
            Object distanceValue = processed.get(DISTANCE_PER_DAY_KEY);
            if (distanceValue != null) {
                try {
                    double distance = convertToDouble(distanceValue);
                    if (distance <= 0) {
                        throw new IllegalArgumentException(
                                "Distance per day must be positive, got: " + distance);
                    }
                    processed.put(DISTANCE_PER_DAY_KEY, distance);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Invalid distance per day value: " + distanceValue, e);
                }
            }
        }

        return processed;
    }

    private Map<String, Object> processSimpleMetadata(Map<String, Object> metadata) {
        // Simple plans don't have specific metadata requirements
        // Just return a copy of the metadata
        return metadata != null ? new HashMap<>(metadata) : new HashMap<>();
    }

    private double convertToDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.parseDouble(value.toString());
    }
}
