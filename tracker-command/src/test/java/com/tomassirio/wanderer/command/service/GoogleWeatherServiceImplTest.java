package com.tomassirio.wanderer.command.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.client.GoogleWeatherClient;
import com.tomassirio.wanderer.command.service.impl.GoogleWeatherServiceImpl;
import com.tomassirio.wanderer.commons.domain.GeoLocation;
import com.tomassirio.wanderer.commons.domain.WeatherCondition;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GoogleWeatherServiceImplTest {

    @Mock private GoogleWeatherClient weatherClient;

    private GoogleWeatherServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GoogleWeatherServiceImpl(weatherClient);
    }

    @Test
    void lookupCurrentWeather_whenLocationIsNull_shouldReturnNull() {
        assertThat(service.lookupCurrentWeather(null)).isNull();
    }

    @Test
    void lookupCurrentWeather_whenLatIsNull_shouldReturnNull() {
        GeoLocation loc = GeoLocation.builder().lat(null).lon(2.35).build();
        assertThat(service.lookupCurrentWeather(loc)).isNull();
    }

    @Test
    void lookupCurrentWeather_whenLonIsNull_shouldReturnNull() {
        GeoLocation loc = GeoLocation.builder().lat(48.85).lon(null).build();
        assertThat(service.lookupCurrentWeather(loc)).isNull();
    }

    @Test
    void lookupCurrentWeather_withValidResponse_shouldReturnResult() {
        GeoLocation loc = GeoLocation.builder().lat(42.88).lon(-8.54).build();
        Map<String, Object> body =
                Map.of(
                        "temperature", Map.of("degrees", 19.4, "unit", "CELSIUS"),
                        "weatherCondition", Map.of("type", "CLEAR"));
        when(weatherClient.getCurrentConditions(42.88, -8.54)).thenReturn(body);

        WeatherService.WeatherResult result = service.lookupCurrentWeather(loc);
        assertThat(result).isNotNull();
        assertThat(result.temperatureCelsius()).isEqualTo(19.4);
        assertThat(result.condition()).isEqualTo(WeatherCondition.CLEAR);
    }

    @Test
    void lookupCurrentWeather_whenNullResponse_shouldReturnNull() {
        GeoLocation loc = GeoLocation.builder().lat(42.88).lon(-8.54).build();
        when(weatherClient.getCurrentConditions(42.88, -8.54)).thenReturn(null);
        assertThat(service.lookupCurrentWeather(loc)).isNull();
    }

    @Test
    void lookupCurrentWeather_whenApiThrows_shouldReturnNull() {
        GeoLocation loc = GeoLocation.builder().lat(42.88).lon(-8.54).build();
        when(weatherClient.getCurrentConditions(42.88, -8.54))
                .thenThrow(new RuntimeException("timeout"));
        when(weatherClient.sanitize(anyString())).thenReturn("timeout");
        assertThat(service.lookupCurrentWeather(loc)).isNull();
    }

    @Test
    void lookupCurrentWeather_withHeavyRain_shouldMapCorrectly() {
        GeoLocation loc = GeoLocation.builder().lat(42.88).lon(-8.54).build();
        Map<String, Object> body =
                Map.of(
                        "temperature", Map.of("degrees", 5.0),
                        "weatherCondition", Map.of("type", "HEAVY_RAIN"));
        when(weatherClient.getCurrentConditions(42.88, -8.54)).thenReturn(body);

        WeatherService.WeatherResult result = service.lookupCurrentWeather(loc);
        assertThat(result).isNotNull();
        assertThat(result.temperatureCelsius()).isEqualTo(5.0);
        assertThat(result.condition()).isEqualTo(WeatherCondition.HEAVY_RAIN);
    }

    @Test
    void lookupCurrentWeather_whenUnknownType_shouldReturnUnknown() {
        GeoLocation loc = GeoLocation.builder().lat(42.88).lon(-8.54).build();
        Map<String, Object> body =
                Map.of(
                        "temperature", Map.of("degrees", 10.0),
                        "weatherCondition", Map.of("type", "SOME_FUTURE_TYPE"));
        when(weatherClient.getCurrentConditions(42.88, -8.54)).thenReturn(body);

        WeatherService.WeatherResult result = service.lookupCurrentWeather(loc);
        assertThat(result).isNotNull();
        assertThat(result.temperatureCelsius()).isEqualTo(10.0);
        assertThat(result.condition()).isEqualTo(WeatherCondition.UNKNOWN);
    }

    @Test
    void lookupCurrentWeather_whenOnlyTemperature_shouldReturnWithNullCondition() {
        GeoLocation loc = GeoLocation.builder().lat(42.88).lon(-8.54).build();
        Map<String, Object> body = Map.of("temperature", Map.of("degrees", 22.0));
        when(weatherClient.getCurrentConditions(42.88, -8.54)).thenReturn(body);

        WeatherService.WeatherResult result = service.lookupCurrentWeather(loc);
        assertThat(result).isNotNull();
        assertThat(result.temperatureCelsius()).isEqualTo(22.0);
        assertThat(result.condition()).isNull();
    }
}
