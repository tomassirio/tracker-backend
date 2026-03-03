package com.tomassirio.wanderer.command.service;

import java.util.UUID;

/**
 * Service for computing and updating encoded polylines on trip plans.
 *
 * <p>Trip plan polylines are computed from the planned route: start location → waypoints → end
 * location. Unlike trip polylines (which grow incrementally), trip plan polylines are always fully
 * recomputed since the route is defined upfront.
 *
 * @since 0.8.0
 */
public interface TripPlanPolylineService {

    /**
     * Computes (or recomputes) the encoded polyline for a trip plan from its start location,
     * waypoints, and end location.
     *
     * @param tripPlanId the UUID of the trip plan
     */
    void computePolyline(UUID tripPlanId);
}
