package com.tomassirio.wanderer.command.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for the Google Weather API.
 *
 * <p>Binds to properties with the prefix "google.weather". The API key is shared with the Google
 * Maps configuration ({@code google.maps.api-key}) since both APIs use the same GCP project key.
 */
@ConfigurationProperties(prefix = "google.weather")
@Data
@Validated
public class GoogleWeatherProperties {

    /** Whether to use the Google Weather API for weather lookups. Defaults to true. */
    private boolean enabled = true;
}
