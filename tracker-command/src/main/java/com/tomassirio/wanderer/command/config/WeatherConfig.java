package com.tomassirio.wanderer.command.config;

import com.tomassirio.wanderer.command.config.properties.GoogleMapsProperties;
import com.tomassirio.wanderer.command.config.properties.GoogleWeatherProperties;
import com.tomassirio.wanderer.command.service.WeatherService;
import com.tomassirio.wanderer.command.service.impl.GoogleWeatherServiceImpl;
import com.tomassirio.wanderer.command.service.impl.NoOpWeatherServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuration class that selects the appropriate {@link WeatherService} implementation.
 *
 * <p>The Google Weather API shares the same GCP API key as the Google Maps APIs, so the key is read
 * from {@link GoogleMapsProperties}. A separate {@link GoogleWeatherProperties#isEnabled() enabled}
 * toggle allows disabling weather lookups independently.
 *
 * <p>If the API key is not configured or weather is disabled, a no-op implementation is used.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(GoogleWeatherProperties.class)
public class WeatherConfig {

    /**
     * Creates a {@link WeatherService} bean.
     *
     * <p>Reuses the Google Maps API key for Weather API calls. If the key is not set or weather is
     * disabled, returns a no-op implementation.
     *
     * @param weatherProperties the Google Weather configuration properties
     * @param mapsProperties the Google Maps configuration properties (provides the shared API key)
     * @return the selected weather service
     */
    @Bean
    public WeatherService weatherService(
            GoogleWeatherProperties weatherProperties, GoogleMapsProperties mapsProperties) {

        String apiKey = mapsProperties.getApiKey();

        if (apiKey != null && !apiKey.isBlank() && weatherProperties.isEnabled()) {
            log.info("Using Google Weather API for weather lookups");
            RestClient restClient = RestClient.create();
            return new GoogleWeatherServiceImpl(apiKey, restClient);
        }

        log.info("Weather lookups disabled — no API key configured or feature disabled");
        return new NoOpWeatherServiceImpl();
    }
}
