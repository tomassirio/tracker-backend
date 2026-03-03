package com.tomassirio.wanderer.command.config;

import com.tomassirio.wanderer.command.client.GoogleWeatherClient;
import com.tomassirio.wanderer.command.config.properties.GoogleWeatherProperties;
import com.tomassirio.wanderer.command.service.WeatherService;
import com.tomassirio.wanderer.command.service.impl.GoogleWeatherServiceImpl;
import com.tomassirio.wanderer.command.service.impl.NoOpWeatherServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class that selects the appropriate {@link WeatherService} implementation.
 *
 * <p>The Google Weather API shares the same GCP API key as the Google Maps APIs, injected via
 * {@code @Value} into the {@link GoogleWeatherClient}. A separate {@link
 * GoogleWeatherProperties#isEnabled() enabled} toggle allows disabling weather lookups
 * independently.
 *
 * <p>If the API key is not configured or weather is disabled, a no-op implementation is used.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(GoogleWeatherProperties.class)
public class WeatherConfig {

    @Bean
    public WeatherService weatherService(
            GoogleWeatherProperties weatherProperties, GoogleWeatherClient weatherClient) {

        if (weatherClient.isConfigured() && weatherProperties.isEnabled()) {
            log.info("Using Google Weather API for weather lookups");
            return new GoogleWeatherServiceImpl(weatherClient);
        }

        log.info("Weather lookups disabled — no API key configured or feature disabled");
        return new NoOpWeatherServiceImpl();
    }
}
