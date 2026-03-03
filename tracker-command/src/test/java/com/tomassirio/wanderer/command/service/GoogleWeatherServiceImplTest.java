package com.tomassirio.wanderer.command.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.service.impl.GoogleWeatherServiceImpl;
import com.tomassirio.wanderer.commons.domain.GeoLocation;
import com.tomassirio.wanderer.commons.domain.WeatherCondition;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class GoogleWeatherServiceImplTest {

    @Mock private RestClient restClient;
    @Mock private RestClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock private RestClient.RequestBodySpec requestBodySpec;
    @Mock private RestClient.ResponseSpec responseSpec;

    private GoogleWeatherServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GoogleWeatherServiceImpl("test-api-key", restClient);
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
    @SuppressWarnings("unchecked")
    void lookupCurrentWeather_withValidResponse_shouldReturnResult() {
        GeoLocation loc = GeoLocation.builder().lat(42.88).lon(-8.54).build();
        Map<String, Object> body =
                Map.of(
                        "temperature", Map.of("degrees", 18.5, "unit", "CELSIUS"),
                        "weatherCondition", Map.of("type", "PARTLY_CLOUDY"));
        stubRestClientChain(body);

        WeatherService.WeatherResult result = service.lookupCurrentWeather(loc);
        assertThat(result).isNotNull();
        assertThat(result.temperatureCelsius()).isEqualTo(18.5);
        assertThat(result.condition()).isEqualTo(WeatherCondition.PARTLY_CLOUDY);
    }

    @Test
    @SuppressWarnings("unchecked")
    void lookupCurrentWeather_whenNullResponse_shouldReturnNull() {
        GeoLocation loc = GeoLocation.builder().lat(42.88).lon(-8.54).build();
        stubRestClientChain(null);
        assertThat(service.lookupCurrentWeather(loc)).isNull();
    }

    @Test
    void lookupCurrentWeather_whenApiThrows_shouldReturnNull() {
        GeoLocation loc = GeoLocation.builder().lat(42.88).lon(-8.54).build();
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class)))
                .thenThrow(new RuntimeException("timeout"));
        assertThat(service.lookupCurrentWeather(loc)).isNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void lookupCurrentWeather_whenDescriptionMissing_shouldFallbackToType() {
        GeoLocation loc = GeoLocation.builder().lat(42.88).lon(-8.54).build();
        Map<String, Object> body =
                Map.of(
                        "temperature", Map.of("degrees", 5.0),
                        "weatherCondition", Map.of("type", "HEAVY_RAIN"));
        stubRestClientChain(body);

        WeatherService.WeatherResult result = service.lookupCurrentWeather(loc);
        assertThat(result).isNotNull();
        assertThat(result.temperatureCelsius()).isEqualTo(5.0);
        assertThat(result.condition()).isEqualTo(WeatherCondition.HEAVY_RAIN);
    }

    @Test
    @SuppressWarnings("unchecked")
    void lookupCurrentWeather_whenUnknownType_shouldReturnUnknown() {
        GeoLocation loc = GeoLocation.builder().lat(42.88).lon(-8.54).build();
        Map<String, Object> body =
                Map.of(
                        "temperature", Map.of("degrees", 10.0),
                        "weatherCondition", Map.of("type", "SOME_FUTURE_TYPE"));
        stubRestClientChain(body);

        WeatherService.WeatherResult result = service.lookupCurrentWeather(loc);
        assertThat(result).isNotNull();
        assertThat(result.temperatureCelsius()).isEqualTo(10.0);
        assertThat(result.condition()).isEqualTo(WeatherCondition.UNKNOWN);
    }

    @SuppressWarnings("unchecked")
    private void stubRestClientChain(Map<String, Object> responseBody) {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.header(any(String.class), any(String[].class)))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(eq(Map.class))).thenReturn(responseBody);
    }
}
