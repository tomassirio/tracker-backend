package com.tomassirio.wanderer.commons.domain;

/**
 * Weather conditions mapped from the Google Weather API {@code weatherCondition.type} values.
 *
 * @see <a href="https://developers.google.com/maps/documentation/weather/current-conditions">Google
 *     Weather API</a>
 */
public enum WeatherCondition {
    // Clear / Cloudy
    CLEAR,
    MOSTLY_CLEAR,
    PARTLY_CLOUDY,
    MOSTLY_CLOUDY,
    CLOUDY,

    // Wind
    WINDY,
    WIND_AND_RAIN,

    // Rain showers
    LIGHT_RAIN_SHOWERS,
    CHANCE_OF_SHOWERS,
    SCATTERED_SHOWERS,
    RAIN_SHOWERS,
    HEAVY_RAIN_SHOWERS,

    // Rain
    LIGHT_TO_MODERATE_RAIN,
    MODERATE_TO_HEAVY_RAIN,
    RAIN,
    LIGHT_RAIN,
    HEAVY_RAIN,
    RAIN_PERIODICALLY_HEAVY,

    // Snow showers
    LIGHT_SNOW_SHOWERS,
    CHANCE_OF_SNOW_SHOWERS,
    SCATTERED_SNOW_SHOWERS,
    SNOW_SHOWERS,
    HEAVY_SNOW_SHOWERS,

    // Snow
    LIGHT_TO_MODERATE_SNOW,
    MODERATE_TO_HEAVY_SNOW,
    SNOW,
    LIGHT_SNOW,
    HEAVY_SNOW,
    SNOWSTORM,
    SNOW_PERIODICALLY_HEAVY,
    HEAVY_SNOW_STORM,
    BLOWING_SNOW,
    RAIN_AND_SNOW,

    // Hail
    HAIL,
    HAIL_SHOWERS,

    // Thunderstorms
    THUNDERSTORM,
    THUNDERSHOWER,
    LIGHT_THUNDERSTORM_RAIN,
    SCATTERED_THUNDERSTORMS,
    HEAVY_THUNDERSTORM,

    // Fallback
    UNKNOWN
}
