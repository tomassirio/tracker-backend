package com.tomassirio.wanderer.command.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.maps.model.LatLng;
import com.tomassirio.wanderer.command.service.impl.StraightLineRouteStrategy;
import com.tomassirio.wanderer.commons.domain.GeoLocation;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for {@link StraightLineRouteStrategy} via the {@link RouteService} interface. */
class GoogleRoutesServiceTest {

    private RouteService routeService;

    @BeforeEach
    void setUp() {
        routeService = new StraightLineRouteStrategy();
    }

    @Test
    void getRoutePoints_shouldReturnStraightLine() {
        // Given
        GeoLocation origin = GeoLocation.builder().lat(42.0).lon(-8.0).build();
        GeoLocation destination = GeoLocation.builder().lat(43.0).lon(-8.5).build();

        // When
        List<LatLng> points = routeService.getRoutePoints(origin, destination);

        // Then
        assertThat(points).hasSize(2);
        assertThat(points.get(0).lat).isEqualTo(42.0);
        assertThat(points.get(0).lng).isEqualTo(-8.0);
        assertThat(points.get(1).lat).isEqualTo(43.0);
        assertThat(points.get(1).lng).isEqualTo(-8.5);
    }

    @Test
    void getFullRoutePoints_whenNullLocations_shouldReturnEmptyList() {
        assertThat(routeService.getFullRoutePoints(null)).isEmpty();
    }

    @Test
    void getFullRoutePoints_whenSingleLocation_shouldReturnEmptyList() {
        // Given
        List<GeoLocation> locations = List.of(GeoLocation.builder().lat(42.0).lon(-8.0).build());

        // When & Then
        assertThat(routeService.getFullRoutePoints(locations)).isEmpty();
    }

    @Test
    void getFullRoutePoints_whenTwoLocations_shouldReturnStraightLine() {
        // Given
        GeoLocation loc1 = GeoLocation.builder().lat(42.0).lon(-8.0).build();
        GeoLocation loc2 = GeoLocation.builder().lat(43.0).lon(-8.5).build();

        // When
        List<LatLng> points = routeService.getFullRoutePoints(List.of(loc1, loc2));

        // Then
        assertThat(points).hasSize(2);
        assertThat(points.get(0).lat).isEqualTo(42.0);
        assertThat(points.get(1).lat).isEqualTo(43.0);
    }

    @Test
    void getFullRoutePoints_whenThreeLocations_shouldCombineSegmentsWithoutDuplicates() {
        // Given
        GeoLocation loc1 = GeoLocation.builder().lat(42.0).lon(-8.0).build();
        GeoLocation loc2 = GeoLocation.builder().lat(42.5).lon(-8.2).build();
        GeoLocation loc3 = GeoLocation.builder().lat(43.0).lon(-8.5).build();

        // When
        List<LatLng> points = routeService.getFullRoutePoints(List.of(loc1, loc2, loc3));

        // Then - 2 from first segment + 1 new from second (duplicate start removed)
        assertThat(points).hasSize(3);
        assertThat(points.get(0).lat).isEqualTo(42.0);
        assertThat(points.get(1).lat).isEqualTo(42.5);
        assertThat(points.get(2).lat).isEqualTo(43.0);
    }
}
