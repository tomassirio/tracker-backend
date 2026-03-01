package com.tomassirio.wanderer.command.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.maps.model.LatLng;
import com.tomassirio.wanderer.command.config.properties.GoogleMapsProperties;
import com.tomassirio.wanderer.commons.domain.GeoLocation;
import java.util.List;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GoogleRoutesServiceTest {

    private GoogleRoutesService googleRoutesService;

    @BeforeEach
    void setUp() {
        GoogleMapsProperties properties = new GoogleMapsProperties();
        properties.setApiKey("");
        properties.setEnabled(false);
        googleRoutesService = new GoogleRoutesService(properties);
        googleRoutesService.init();
    }

    @Test
    void getRoutePoints_whenApiNotConfigured_shouldReturnStraightLine() {
        // Given
        GeoLocation origin = GeoLocation.builder().lat(42.0).lon(-8.0).build();
        GeoLocation destination = GeoLocation.builder().lat(43.0).lon(-8.5).build();

        // When
        List<LatLng> points = googleRoutesService.getRoutePoints(origin, destination);

        // Then
        assertThat(points).hasSize(2);
        assertThat(points.get(0).lat).isEqualTo(42.0);
        assertThat(points.get(0).lng).isEqualTo(-8.0);
        assertThat(points.get(1).lat).isEqualTo(43.0);
        assertThat(points.get(1).lng).isEqualTo(-8.5);
    }

    @Test
    void getFullRoutePoints_whenNullLocations_shouldReturnEmptyList() {
        // When
        List<LatLng> points = googleRoutesService.getFullRoutePoints(null);

        // Then
        assertThat(points).isEmpty();
    }

    @Test
    void getFullRoutePoints_whenSingleLocation_shouldReturnEmptyList() {
        // Given
        List<GeoLocation> locations = List.of(GeoLocation.builder().lat(42.0).lon(-8.0).build());

        // When
        List<LatLng> points = googleRoutesService.getFullRoutePoints(locations);

        // Then
        assertThat(points).isEmpty();
    }

    @Test
    void getFullRoutePoints_whenTwoLocations_shouldReturnStraightLine() {
        // Given
        GeoLocation loc1 = GeoLocation.builder().lat(42.0).lon(-8.0).build();
        GeoLocation loc2 = GeoLocation.builder().lat(43.0).lon(-8.5).build();

        // When
        List<LatLng> points = googleRoutesService.getFullRoutePoints(List.of(loc1, loc2));

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
        List<LatLng> points = googleRoutesService.getFullRoutePoints(List.of(loc1, loc2, loc3));

        // Then - 2 from first segment + 1 new from second (duplicate start removed)
        assertThat(points).hasSize(3);
        assertThat(points.get(0).lat).isEqualTo(42.0);
        assertThat(points.get(1).lat).isEqualTo(42.5);
        assertThat(points.get(2).lat).isEqualTo(43.0);
    }

    @Test
    void encodePolyline_whenNullPoints_shouldReturnNull() {
        // When & Then
        assertThat(googleRoutesService.encodePolyline(null)).isNull();
    }

    @Test
    void encodePolyline_whenEmptyPoints_shouldReturnNull() {
        // When & Then
        assertThat(googleRoutesService.encodePolyline(List.of())).isNull();
    }

    @Test
    void encodePolyline_whenValidPoints_shouldReturnEncodedString() {
        // Given
        List<LatLng> points = List.of(new LatLng(42.0, -8.0), new LatLng(43.0, -8.5));

        // When
        String result = googleRoutesService.encodePolyline(points);

        // Then
        assertThat(result).isNotNull().isNotEmpty();
    }

    @Test
    void decodePolyline_whenNullInput_shouldReturnEmptyList() {
        // When & Then
        assertThat(googleRoutesService.decodePolyline(null)).isEmpty();
    }

    @Test
    void decodePolyline_whenEmptyInput_shouldReturnEmptyList() {
        // When & Then
        assertThat(googleRoutesService.decodePolyline("")).isEmpty();
    }

    @Test
    void encodeAndDecode_shouldBeReversible() {
        // Given
        List<LatLng> originalPoints =
                List.of(new LatLng(42.0, -8.0), new LatLng(42.5, -8.2), new LatLng(43.0, -8.5));

        // When
        String encoded = googleRoutesService.encodePolyline(originalPoints);
        List<LatLng> decoded = googleRoutesService.decodePolyline(encoded);

        // Then
        assertThat(decoded).hasSameSizeAs(originalPoints);
        for (int i = 0; i < originalPoints.size(); i++) {
            assertThat(decoded.get(i).lat)
                    .isCloseTo(originalPoints.get(i).lat, Offset.offset(1e-5));
            assertThat(decoded.get(i).lng)
                    .isCloseTo(originalPoints.get(i).lng, Offset.offset(1e-5));
        }
    }
}
