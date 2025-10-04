package com.tomassirio.wanderer.command.service;

import com.tomassirio.wanderer.command.dto.LocationUpdateRequest;
import com.tomassirio.wanderer.commons.domain.Location;
import java.util.UUID;

/**
 * Service interface for managing location tracking operations in the command side of the CQRS
 * architecture.
 *
 * <p>This service handles GPS location updates from tracking devices such as OwnTracks. Each
 * location update is associated with a specific trip and includes coordinates, timestamp, and
 * device metadata.
 *
 * @author tomassirio
 * @since 0.0.2
 */
public interface LocationService {

    /**
     * Creates a new location entry for the specified trip.
     *
     * <p>This method processes location updates from tracking devices and persists them to the
     * database. If no timestamp is provided in the request, the current system time is used. The
     * location is validated to ensure it belongs to an existing trip.
     *
     * <p>Location data includes:
     *
     * <ul>
     *   <li>GPS coordinates (latitude, longitude, altitude)
     *   <li>Timestamp of when the location was recorded
     *   <li>Accuracy information from the GPS device
     *   <li>Device battery level
     *   <li>Source identifier (e.g., device name or tracking app)
     * </ul>
     *
     * @param tripId the UUID of the trip this location belongs to
     * @param request the location update request containing GPS coordinates and metadata
     * @return the created {@link Location} entity with generated ID
     * @throws IllegalArgumentException if the trip doesn't exist or request data is invalid
     */
    Location createLocationUpdate(UUID tripId, LocationUpdateRequest request);
}
