package com.tomassirio.wanderer.command.service;

import java.util.UUID;

/**
 * Service for computing and updating encoded polylines on trips.
 *
 * <p>Supports incremental segment appending (when a new trip update is added) and full
 * recomputation (when a trip update is deleted or the polyline needs to be regenerated).
 *
 * @since 0.8.0
 */
public interface PolylineService {

    /**
     * Incrementally appends a new route segment to an existing trip's polyline.
     *
     * <p>Fetches the walking route between the previous last location and the new location, then
     * appends the new segment's points to the existing encoded polyline. If no existing polyline
     * exists, computes the full polyline from all trip updates.
     *
     * @param tripId the UUID of the trip to update
     */
    void appendSegment(UUID tripId);

    /**
     * Fully recomputes the encoded polyline for a trip from all its trip updates.
     *
     * <p>This should be called when a trip update is deleted or when the polyline needs to be
     * regenerated from scratch.
     *
     * @param tripId the UUID of the trip to recompute the polyline for
     */
    void recomputePolyline(UUID tripId);
}
