package com.tomassirio.wanderer.commons.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Interface for entities that carry an encoded polyline.
 *
 * <p>Implemented by {@link Trip} and {@link TripPlan} to allow shared polyline computation logic.
 *
 * @since 0.8.2
 */
public interface Polylineable {

    UUID getId();

    String getEncodedPolyline();

    void setEncodedPolyline(String encodedPolyline);

    Instant getPolylineUpdatedAt();

    void setPolylineUpdatedAt(Instant polylineUpdatedAt);
}
