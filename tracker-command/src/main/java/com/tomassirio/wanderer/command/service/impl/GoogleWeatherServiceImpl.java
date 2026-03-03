package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.command.service.WeatherService;
import com.tomassirio.wanderer.commons.domain.GeoLocation;
import com.tomassirio.wanderer.commons.domain.WeatherCondition;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;

/**
 * Weather service implementation that calls the Google Weather API (v1) to retrieve current
 * conditions at a given location.
 *
 * <p>Uses the {@code currentConditions:lookup} endpoint and extracts the temperature (Celsius) and
 * the weather condition type from the response, mapping it to our {@link WeatherCondition} enum.
 *
 * <p>Returns {@code null} on any API or parsing failure so that callers can gracefully degrade.
 *
 * @see <a href="https://developers.google.com/maps/documentation/weather/current-conditions">Google
 *     Weather API</a>
 */
@Slf4j
@RequiredArgsConstructor
public class GoogleWeatherServiceImpl implements WeatherService {

    private static final String WEATHER_API_URL =
            "https://weather.googleapis.com/v1/currentConditions:lookup";

    private final String apiKey;
    private final RestClient restClient;

    @Override
    @SuppressWarnings("unchecked")
    public WeatherResult lookupCurrentWeather(GeoLocation location) {
        if (location == null || location.getLat() == null || location.getLon() == null) {
            return null;
        }

        try {
            Map<String, Object> requestBody =
                    Map.of(
                            "location",
                            Map.of(
                                    "latitude", location.getLat(),
                                    "longitude", location.getLon()));

            Map<String, Object> response =
                    restClient
                            .post()
                            .uri(WEATHER_API_URL + "?key={key}", apiKey)
                            .body(requestBody)
                            .retrieve()
                            .body(Map.class);

            if (response == null) {
                log.debug(
                        "Empty weather response for ({}, {})",
                        location.getLat(),
                        location.getLon());
                return null;
            }

            return extractWeatherResult(response);
        } catch (Exception e) {
            log.warn(
                    "Weather lookup failed for ({}, {}): {}",
                    location.getLat(),
                    location.getLon(),
                    e.getMessage());
            return null;
        }
    }

    private WeatherResult extractWeatherResult(Map<String, Object> response) {
        Double temperatureCelsius = extractTemperature(response);
        WeatherCondition condition = extractCondition(response);

        if (temperatureCelsius != null || condition != null) {
            log.debug(
                    "Weather lookup result: temperature={}°C, condition={}",
                    temperatureCelsius,
                    condition);
            return new WeatherResult(temperatureCelsius, condition);
        }

        return null;
    }

    /**
     * Extracts temperature from the response.
     *
     * <p>Expected structure: {@code { "temperature": { "degrees": 15.5, "unit": "CELSIUS" } }}
     */
    @SuppressWarnings("unchecked")
    private Double extractTemperature(Map<String, Object> response) {
        try {
            Map<String, Object> temperature = (Map<String, Object>) response.get("temperature");
            if (temperature != null && temperature.get("degrees") != null) {
                return ((Number) temperature.get("degrees")).doubleValue();
            }
        } catch (ClassCastException e) {
            log.debug("Failed to extract temperature from response: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Extracts the weather condition type from the response and maps it to our {@link
     * WeatherCondition} enum.
     *
     * <p>Expected structure: {@code { "weatherCondition": { "type": "PARTLY_CLOUDY" } }}
     *
     * <p>Falls back to {@link WeatherCondition#UNKNOWN} if the type is not recognized.
     */
    @SuppressWarnings("unchecked")
    private WeatherCondition extractCondition(Map<String, Object> response) {
        try {
            Map<String, Object> weatherCondition =
                    (Map<String, Object>) response.get("weatherCondition");
            if (weatherCondition == null) {
                return null;
            }

            Object type = weatherCondition.get("type");
            if (type == null) {
                return null;
            }

            return parseConditionType(type.toString());
        } catch (ClassCastException e) {
            log.debug("Failed to extract condition from response: {}", e.getMessage());
        }
        return null;
    }

    private WeatherCondition parseConditionType(String type) {
        try {
            return WeatherCondition.valueOf(type);
        } catch (IllegalArgumentException e) {
            log.debug("Unrecognized weather condition type: {}", type);
            return WeatherCondition.UNKNOWN;
        }
    }
}
