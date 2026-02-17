package com.tomassirio.wanderer.command.service;

import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.DistanceMatrixRow;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;
import com.tomassirio.wanderer.command.config.properties.GoogleMapsProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

/**
 * Service for calculating distances using Google Maps Distance Matrix API.
 *
 * <p>Falls back to Haversine formula if API is not configured or fails.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(GoogleMapsProperties.class)
public class GoogleMapsDistanceService {

    private final GoogleMapsProperties googleMapsProperties;
    private GeoApiContext geoApiContext;

    @PostConstruct
    public void init() {
        if (googleMapsProperties.getApiKey() != null
                && !googleMapsProperties.getApiKey().isEmpty()
                && googleMapsProperties.isEnabled()) {
            geoApiContext =
                    new GeoApiContext.Builder().apiKey(googleMapsProperties.getApiKey()).build();
            log.info("Google Maps API initialized successfully");
        } else {
            log.warn(
                    "Google Maps API key not configured. Distance calculations will use Haversine"
                            + " formula");
        }
    }

    @PreDestroy
    public void cleanup() {
        if (geoApiContext != null) {
            geoApiContext.shutdown();
        }
    }

    /**
     * Calculates the total distance along a path of coordinates using Google Maps Distance Matrix
     * API.
     *
     * <p>If Google Maps API is not available, falls back to Haversine formula.
     *
     * @param coordinates list of lat/lng pairs representing the path
     * @return total distance in kilometers
     */
    public double calculatePathDistance(List<LatLng> coordinates) {
        if (coordinates == null || coordinates.size() < 2) {
            return 0.0;
        }

        // If Google Maps API is available, use it
        if (geoApiContext != null) {
            try {
                return calculateDistanceViaGoogleMaps(coordinates);
            } catch (Exception e) {
                log.warn(
                        "Failed to calculate distance via Google Maps API, falling back to"
                                + " Haversine: {}",
                        e.getMessage());
            }
        }

        // Fallback to Haversine formula
        return calculateDistanceViaHaversine(coordinates);
    }

    /**
     * Calculates distance using Google Maps Distance Matrix API.
     *
     * <p>Uses walking mode to get realistic path distances.
     */
    private double calculateDistanceViaGoogleMaps(List<LatLng> coordinates) throws Exception {
        double totalDistance = 0.0;

        // Process in batches to avoid API limits (max 25 origins x 25 destinations per request)
        for (int i = 0; i < coordinates.size() - 1; i++) {
            LatLng origin = coordinates.get(i);
            LatLng destination = coordinates.get(i + 1);

            DistanceMatrix matrix =
                    DistanceMatrixApi.newRequest(geoApiContext)
                            .origins(origin)
                            .destinations(destination)
                            .mode(TravelMode.WALKING)
                            .await();

            if (matrix != null && matrix.rows != null && matrix.rows.length > 0) {
                DistanceMatrixRow row = matrix.rows[0];
                if (row.elements != null && row.elements.length > 0) {
                    DistanceMatrixElement element = row.elements[0];
                    if (element.distance != null) {
                        // Distance is returned in meters, convert to kilometers
                        totalDistance += element.distance.inMeters / 1000.0;
                    }
                }
            }
        }

        log.debug("Calculated distance via Google Maps API: {} km", totalDistance);
        return totalDistance;
    }

    /**
     * Calculates distance using Haversine formula (great-circle distance).
     *
     * <p>This is a fallback method when Google Maps API is unavailable.
     */
    private double calculateDistanceViaHaversine(List<LatLng> coordinates) {
        double totalDistance = 0.0;

        for (int i = 0; i < coordinates.size() - 1; i++) {
            LatLng current = coordinates.get(i);
            LatLng next = coordinates.get(i + 1);

            totalDistance += haversineDistance(current.lat, current.lng, next.lat, next.lng);
        }

        log.debug("Calculated distance via Haversine formula: {} km", totalDistance);
        return totalDistance;
    }

    /**
     * Calculates distance between two coordinates using Haversine formula.
     *
     * @param lat1 latitude of first point
     * @param lon1 longitude of first point
     * @param lat2 latitude of second point
     * @param lon2 longitude of second point
     * @return distance in kilometers
     */
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // Earth's radius in kilometers

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2)
                        + Math.cos(Math.toRadians(lat1))
                                * Math.cos(Math.toRadians(lat2))
                                * Math.sin(dLon / 2)
                                * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}
