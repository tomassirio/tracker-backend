package com.tomassirio.wanderer.command.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.maps.model.LatLng;
import com.tomassirio.wanderer.command.service.impl.HaversineDistanceStrategy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link HaversineDistanceStrategy} via the {@link DistanceCalculationStrategy}
 * interface.
 */
class GoogleMapsDistanceServiceTest {

    private DistanceCalculationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new HaversineDistanceStrategy();
    }

    @Test
    void calculatePathDistance_whenNullCoordinates_shouldReturnZero() {
        // When
        double distance = strategy.calculatePathDistance(null);

        // Then
        assertThat(distance).isEqualTo(0.0);
    }

    @Test
    void calculatePathDistance_whenEmptyList_shouldReturnZero() {
        // Given
        List<LatLng> coordinates = Collections.emptyList();

        // When
        double distance = strategy.calculatePathDistance(coordinates);

        // Then
        assertThat(distance).isEqualTo(0.0);
    }

    @Test
    void calculatePathDistance_whenSingleCoordinate_shouldReturnZero() {
        // Given
        List<LatLng> coordinates = Collections.singletonList(new LatLng(40.7128, -74.0060));

        // When
        double distance = strategy.calculatePathDistance(coordinates);

        // Then
        assertThat(distance).isEqualTo(0.0);
    }

    @Test
    void calculatePathDistance_whenTwoCoordinates_shouldCalculateDistance() {
        // Given - New York to Los Angeles (approximately 3944 km)
        List<LatLng> coordinates =
                Arrays.asList(
                        new LatLng(40.7128, -74.0060), // New York
                        new LatLng(34.0522, -118.2437) // Los Angeles
                        );

        // When
        double distance = strategy.calculatePathDistance(coordinates);

        // Then - Using Haversine, should be approximately 3944 km
        assertThat(distance).isGreaterThan(3900).isLessThan(4000);
    }

    @Test
    void calculatePathDistance_whenMultipleCoordinates_shouldCalculateTotalDistance() {
        // Given - A path from Santiago de Compostela to Finisterre (approximately 90 km by road)
        List<LatLng> coordinates =
                Arrays.asList(
                        new LatLng(42.8805, -8.5457), // Santiago de Compostela
                        new LatLng(42.8782, -8.7361), // Negreira (waypoint)
                        new LatLng(42.9000, -9.2615) // Finisterre
                        );

        // When
        double distance = strategy.calculatePathDistance(coordinates);

        // Then - Haversine gives straight-line distance (~58 km)
        assertThat(distance).isGreaterThan(50).isLessThan(70);
    }

    @Test
    void calculatePathDistance_whenShortDistance_shouldBeAccurate() {
        // Given - Two points approximately 1.1 km apart (0.01 degrees latitude)
        List<LatLng> coordinates =
                Arrays.asList(
                        new LatLng(40.7128, -74.0060), // Point 1
                        new LatLng(40.7228, -74.0060) // Point 2 (0.01 degrees north)
                        );

        // When
        double distance = strategy.calculatePathDistance(coordinates);

        // Then - Should be approximately 1.11 km (0.01 degrees latitude ≈ 1.11 km)
        assertThat(distance).isGreaterThan(1.0).isLessThan(1.2);
    }

    @Test
    void calculatePathDistance_whenCaminoRoute_shouldCalculateRealisticDistance() {
        // Given - Camino de Santiago route segments
        List<LatLng> coordinates =
                Arrays.asList(
                        new LatLng(42.6750, -2.8260), // Saint-Jean-Pied-de-Port (start)
                        new LatLng(42.8164, -1.6440), // Pamplona
                        new LatLng(42.4668, -2.4450), // Logroño
                        new LatLng(42.3400, -3.7000), // Burgos
                        new LatLng(42.6000, -5.5700), // León
                        new LatLng(42.8805, -8.5457) // Santiago de Compostela (end)
                        );

        // When
        double distance = strategy.calculatePathDistance(coordinates);

        // Then - Camino Francés is approximately 800 km
        // Haversine gives straight-line distance, so it will be less
        assertThat(distance).isGreaterThan(500).isLessThan(900);
    }

    @Test
    void calculatePathDistance_whenSameCoordinate_shouldReturnZero() {
        // Given - Same coordinate twice
        List<LatLng> coordinates =
                Arrays.asList(new LatLng(40.7128, -74.0060), new LatLng(40.7128, -74.0060));

        // When
        double distance = strategy.calculatePathDistance(coordinates);

        // Then
        assertThat(distance).isEqualTo(0.0);
    }

    @Test
    void calculatePathDistance_whenLargeListOfCoordinates_shouldHandleEfficiently() {
        // Given - 100 coordinates forming a path
        List<LatLng> coordinates = new ArrayList<>();
        double lat = 40.0;
        double lng = -74.0;
        for (int i = 0; i < 100; i++) {
            coordinates.add(new LatLng(lat + (i * 0.01), lng));
        }

        // When
        double distance = strategy.calculatePathDistance(coordinates);

        // Then - Should calculate distance without errors

        assertThat(distance).isGreaterThan(0);
    }
}
