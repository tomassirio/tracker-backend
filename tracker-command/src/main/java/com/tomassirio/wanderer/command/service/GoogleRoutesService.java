package com.tomassirio.wanderer.command.service;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;
import com.tomassirio.wanderer.command.config.properties.GoogleMapsProperties;
import com.tomassirio.wanderer.commons.domain.GeoLocation;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

/**
 * Service for fetching road-snapped walking routes from Google Directions API.
 *
 * <p>Returns encoded polyline strings representing the walking path between two locations. Falls
 * back to a straight-line encoded polyline if the API is not configured or fails.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(GoogleMapsProperties.class)
public class GoogleRoutesService {

    private final GoogleMapsProperties googleMapsProperties;
    private GeoApiContext geoApiContext;

    @PostConstruct
    public void init() {
        if (googleMapsProperties.getApiKey() != null
                && !googleMapsProperties.getApiKey().isEmpty()
                && googleMapsProperties.isEnabled()) {
            geoApiContext =
                    new GeoApiContext.Builder().apiKey(googleMapsProperties.getApiKey()).build();
            log.info("Google Routes Service initialized with API key");
        } else {
            log.warn(
                    "Google Maps API key not configured. Polyline computation will use"
                            + " straight-line fallback");
        }
    }

    @PreDestroy
    public void cleanup() {
        if (geoApiContext != null) {
            geoApiContext.shutdown();
        }
    }

    /**
     * Gets the road-snapped route points for a walking route between two locations.
     *
     * @param origin the starting location
     * @param destination the ending location
     * @return list of decoded LatLng points along the route
     */
    public List<LatLng> getRoutePoints(GeoLocation origin, GeoLocation destination) {
        if (geoApiContext != null) {
            try {
                return fetchRouteFromGoogle(origin, destination);
            } catch (Exception e) {
                log.warn(
                        "Failed to fetch route from Google Directions API, falling back to"
                                + " straight-line: {}",
                        e.getMessage());
            }
        }
        return straightLineFallback(origin, destination);
    }

    /**
     * Gets the road-snapped route points for a full path through all provided locations.
     *
     * @param locations ordered list of locations (at least 2)
     * @return list of decoded LatLng points along the full route
     */
    public List<LatLng> getFullRoutePoints(List<GeoLocation> locations) {
        if (locations == null || locations.size() < 2) {
            return List.of();
        }
        return fetchSegmentBySegment(locations);
    }

    /**
     * Encodes a list of LatLng points into a Google Encoded Polyline string.
     *
     * @param points the list of points to encode
     * @return the encoded polyline string, or null if points is null or empty
     */
    public String encodePolyline(List<LatLng> points) {
        if (points == null || points.isEmpty()) {
            return null;
        }
        return new EncodedPolyline(points).getEncodedPath();
    }

    /**
     * Decodes a Google Encoded Polyline string into a list of LatLng points.
     *
     * @param encodedPolyline the encoded polyline string
     * @return list of decoded LatLng points, or empty list if input is null or empty
     */
    public List<LatLng> decodePolyline(String encodedPolyline) {
        if (encodedPolyline == null || encodedPolyline.isEmpty()) {
            return List.of();
        }
        return new EncodedPolyline(encodedPolyline).decodePath();
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
        return straightLineFallback(origin, destination);
    }

    private List<LatLng> fetchSegmentBySegment(List<GeoLocation> locations) {
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

    private List<LatLng> straightLineFallback(GeoLocation origin, GeoLocation destination) {
        return List.of(
                new LatLng(origin.getLat(), origin.getLon()),
                new LatLng(destination.getLat(), destination.getLon()));
    }
}
