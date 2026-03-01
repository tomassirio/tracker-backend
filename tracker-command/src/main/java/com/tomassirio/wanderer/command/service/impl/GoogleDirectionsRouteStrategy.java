package com.tomassirio.wanderer.command.service.impl;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;
import com.tomassirio.wanderer.command.service.RouteService;
import com.tomassirio.wanderer.commons.domain.GeoLocation;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Route strategy that uses the Google Directions API (walking mode) for road-snapped routes.
 *
 * <p>Falls back to {@link StraightLineRouteStrategy} if an API call fails at runtime.
 */
@Slf4j
@RequiredArgsConstructor
public class GoogleDirectionsRouteStrategy implements RouteService {

    private final GeoApiContext geoApiContext;
    private final StraightLineRouteStrategy fallback;

    @Override
    public List<LatLng> getRoutePoints(GeoLocation origin, GeoLocation destination) {
        try {
            return fetchRouteFromGoogle(origin, destination);
        } catch (Exception e) {
            log.warn(
                    "Failed to fetch route from Google Directions API, falling back to"
                            + " straight-line: {}",
                    e.getMessage());
            return fallback.getRoutePoints(origin, destination);
        }
    }

    @Override
    public List<LatLng> getFullRoutePoints(List<GeoLocation> locations) {
        if (locations == null || locations.size() < 2) {
            return List.of();
        }

        ArrayList<LatLng> allPoints = new ArrayList<>();

        for (int i = 0; i < locations.size() - 1; i++) {
            List<LatLng> segmentPoints = getRoutePoints(locations.get(i), locations.get(i + 1));
            if (allPoints.isEmpty()) {
                allPoints.addAll(segmentPoints);
            } else if (!segmentPoints.isEmpty()) {
                // Skip first point of new segment to avoid duplicate with last point
                allPoints.addAll(segmentPoints.subList(1, segmentPoints.size()));
            }
        }

        return allPoints;
    }

    private List<LatLng> fetchRouteFromGoogle(GeoLocation origin, GeoLocation destination)
            throws Exception {
        LatLng originLatLng = new LatLng(origin.getLat(), origin.getLon());
        LatLng destLatLng = new LatLng(destination.getLat(), destination.getLon());

        DirectionsResult result =
                DirectionsApi.newRequest(geoApiContext)
                        .origin(originLatLng)
                        .destination(destLatLng)
                        .mode(TravelMode.WALKING)
                        .await();

        if (result.routes != null && result.routes.length > 0) {
            DirectionsRoute route = result.routes[0];
            if (route.overviewPolyline != null) {
                return route.overviewPolyline.decodePath();
            }
        }

        log.warn("No routes returned from Google API, using straight-line fallback");
        return fallback.getRoutePoints(origin, destination);
    }
}
