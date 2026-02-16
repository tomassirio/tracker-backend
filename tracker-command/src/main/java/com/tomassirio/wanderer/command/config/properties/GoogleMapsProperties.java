package com.tomassirio.wanderer.command.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for Google Maps API.
 *
 * <p>Binds to properties with the prefix "google.maps".
 */
@ConfigurationProperties(prefix = "google.maps")
@Data
@Validated
public class GoogleMapsProperties {

    /** Google Maps API key for accessing Distance Matrix API and other services. */
    private String apiKey;

    /**
     * Whether to use Google Maps API for distance calculation. Defaults to true if API key is set.
     */
    private boolean enabled = true;
}
