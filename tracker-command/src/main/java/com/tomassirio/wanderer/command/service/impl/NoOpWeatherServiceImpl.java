package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.command.service.WeatherService;
import com.tomassirio.wanderer.commons.domain.GeoLocation;

/**
 * No-op weather service that always returns {@code null}.
 *
 * <p>Used when the Google Weather API key is not configured or the feature is disabled.
 */
public class NoOpWeatherServiceImpl implements WeatherService {

    @Override
    public WeatherResult lookupCurrentWeather(GeoLocation location) {
        return null;
    }
}
