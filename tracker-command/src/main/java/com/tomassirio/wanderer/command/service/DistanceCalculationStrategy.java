package com.tomassirio.wanderer.command.service;

import com.google.maps.model.LatLng;
import java.util.List;

/**
 * Strategy interface for calculating the total distance along a path of coordinates.
 *
 * <p>Implementations may use external APIs (e.g., Google Maps Distance Matrix) or offline
 * algorithms (e.g., Haversine formula). The active implementation is selected at startup based on
 * configuration — see {@link com.tomassirio.wanderer.command.config.GeoApiContextConfig}.
 *
 * @since 0.8.0
 */
public interface DistanceCalculationStrategy {

    /**
     * Calculates the total distance along a path of coordinates.
     *
     * @param coordinates ordered list of lat/lng pairs representing the path (at least 2)
     * @return total distance in kilometers, or 0.0 if the list has fewer than 2 points
     */
    double calculatePathDistance(List<LatLng> coordinates);
}
