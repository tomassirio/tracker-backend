package com.tomassirio.wanderer.command.service;

import com.tomassirio.wanderer.commons.domain.GeoLocation;
import com.tomassirio.wanderer.commons.domain.WeatherCondition;

/**
 * Service for looking up current weather conditions at a given location.
 *
 * <p>Implementations may call external APIs (e.g. Google Weather API) or provide offline fallbacks.
 */
public interface WeatherService {

    /**
     * Looks up the current weather at the given location.
     *
     * @param location the geographic coordinates to look up weather for
     * @return a {@link WeatherResult} with temperature and condition, or {@code null} if the lookup
     *     fails
     */
    WeatherResult lookupCurrentWeather(GeoLocation location);

    /**
     * Holds the temperature (in Celsius) and general condition extracted from a weather API
     * response.
     */
    record WeatherResult(Double temperatureCelsius, WeatherCondition condition) {}
}
