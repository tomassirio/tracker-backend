package com.tomassirio.wanderer.command.service;

import com.google.maps.model.LatLng;
import com.tomassirio.wanderer.commons.domain.GeoLocation;
import java.util.List;

/**
 * Strategy interface for fetching route points between locations.
 *
 * <p>Implementations may use external APIs (e.g., Google Directions API) or offline fallbacks
 * (e.g., straight-line points). The active implementation is selected at startup based on
 * configuration — see {@link com.tomassirio.wanderer.command.config.GeoApiContextConfig}.
 *
 * @since 0.8.0
 */
public interface RouteService {

    /**
     * Gets route points for a walking route between two locations.
     *
     * @param origin the starting location
     * @param destination the ending location
     * @return list of LatLng points along the route
     */
    List<LatLng> getRoutePoints(GeoLocation origin, GeoLocation destination);

    /**
     * Gets route points for a full path through all provided locations.
     *
     * @param locations ordered list of locations (at least 2)
     * @return list of LatLng points along the full route, or empty list if fewer than 2 locations
     */
    List<LatLng> getFullRoutePoints(List<GeoLocation> locations);
}
