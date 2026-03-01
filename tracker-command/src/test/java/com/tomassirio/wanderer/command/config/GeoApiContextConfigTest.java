package com.tomassirio.wanderer.command.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.maps.GeoApiContext;
import com.tomassirio.wanderer.command.config.properties.GoogleMapsProperties;
import com.tomassirio.wanderer.command.service.DistanceCalculationStrategy;
import com.tomassirio.wanderer.command.service.RouteService;
import com.tomassirio.wanderer.command.service.impl.GoogleDirectionsRouteStrategy;
import com.tomassirio.wanderer.command.service.impl.GoogleMapsDistanceStrategy;
import com.tomassirio.wanderer.command.service.impl.HaversineDistanceStrategy;
import com.tomassirio.wanderer.command.service.impl.StraightLineRouteStrategy;
import org.junit.jupiter.api.Test;

class GeoApiContextConfigTest {

    private final GeoApiContextConfig config = new GeoApiContextConfig();

    @Test
    void geoApiContext_whenApiKeyConfiguredAndEnabled_shouldReturnContext() {
        // Given
        GoogleMapsProperties properties = new GoogleMapsProperties();
        properties.setApiKey("test-api-key");
        properties.setEnabled(true);

        // When
        GeoApiContext context = config.geoApiContext(properties);

        // Then
        assertThat(context).isNotNull();

        // Cleanup
        config.cleanup();
    }

    @Test
    void geoApiContext_whenApiKeyIsNull_shouldReturnNull() {
        // Given
        GoogleMapsProperties properties = new GoogleMapsProperties();
        properties.setApiKey(null);
        properties.setEnabled(true);

        // When
        GeoApiContext context = config.geoApiContext(properties);

        // Then
        assertThat(context).isNull();
    }

    @Test
    void geoApiContext_whenApiKeyIsEmpty_shouldReturnNull() {
        // Given
        GoogleMapsProperties properties = new GoogleMapsProperties();
        properties.setApiKey("");
        properties.setEnabled(true);

        // When
        GeoApiContext context = config.geoApiContext(properties);

        // Then
        assertThat(context).isNull();
    }

    @Test
    void geoApiContext_whenDisabled_shouldReturnNull() {
        // Given
        GoogleMapsProperties properties = new GoogleMapsProperties();
        properties.setApiKey("test-api-key");
        properties.setEnabled(false);

        // When
        GeoApiContext context = config.geoApiContext(properties);

        // Then
        assertThat(context).isNull();
    }

    @Test
    void cleanup_whenNoContextCreated_shouldNotThrow() {
        // When & Then — should not throw
        config.cleanup();
    }

    @Test
    void distanceCalculationStrategy_whenGeoApiContextAvailable_shouldReturnGoogleMapsStrategy() {
        // Given
        GoogleMapsProperties properties = new GoogleMapsProperties();
        properties.setApiKey("test-api-key");
        properties.setEnabled(true);
        GeoApiContext context = config.geoApiContext(properties);

        // When
        DistanceCalculationStrategy strategy = config.distanceCalculationStrategy(context);

        // Then
        assertThat(strategy).isInstanceOf(GoogleMapsDistanceStrategy.class);

        // Cleanup
        config.cleanup();
    }

    @Test
    void distanceCalculationStrategy_whenGeoApiContextIsNull_shouldReturnHaversineStrategy() {
        // When
        DistanceCalculationStrategy strategy = config.distanceCalculationStrategy(null);

        // Then
        assertThat(strategy).isInstanceOf(HaversineDistanceStrategy.class);
    }

    @Test
    void routeService_whenGeoApiContextAvailable_shouldReturnGoogleDirectionsStrategy() {
        // Given
        GoogleMapsProperties properties = new GoogleMapsProperties();
        properties.setApiKey("test-api-key");
        properties.setEnabled(true);
        GeoApiContext context = config.geoApiContext(properties);

        // When
        RouteService route = config.routeService(context);

        // Then
        assertThat(route).isInstanceOf(GoogleDirectionsRouteStrategy.class);

        // Cleanup
        config.cleanup();
    }

    @Test
    void routeService_whenGeoApiContextIsNull_shouldReturnStraightLineStrategy() {
        // When
        RouteService route = config.routeService(null);

        // Then
        assertThat(route).isInstanceOf(StraightLineRouteStrategy.class);
    }
}
