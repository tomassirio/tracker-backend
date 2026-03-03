package com.tomassirio.wanderer.command.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.tomassirio.wanderer.command.service.impl.NoOpWeatherServiceImpl;
import com.tomassirio.wanderer.commons.domain.GeoLocation;
import org.junit.jupiter.api.Test;

class NoOpWeatherServiceImplTest {

    private final WeatherService service = new NoOpWeatherServiceImpl();

    @Test
    void lookupCurrentWeather_shouldAlwaysReturnNull() {
        GeoLocation location = GeoLocation.builder().lat(42.88).lon(-8.54).build();

        WeatherService.WeatherResult result = service.lookupCurrentWeather(location);

        assertThat(result).isNull();
    }

    @Test
    void lookupCurrentWeather_whenLocationIsNull_shouldReturnNull() {
        WeatherService.WeatherResult result = service.lookupCurrentWeather(null);

        assertThat(result).isNull();
    }
}
