package com.tomassirio.wanderer.command.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.dto.LocationUpdateRequest;
import com.tomassirio.wanderer.command.repository.LocationRepository;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.service.impl.LocationServiceImpl;
import com.tomassirio.wanderer.commons.domain.Location;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.utils.BaseTestEntityFactory;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock private LocationRepository locationRepository;

    @Mock private TripRepository tripRepository;

    @InjectMocks private LocationServiceImpl locationService;

    @Test
    void createLocationUpdate_whenTripExists_shouldCreateAndSaveLocation() {
        // Given
        UUID tripId = UUID.randomUUID();
        Trip trip = BaseTestEntityFactory.createTrip(tripId);
        LocationUpdateRequest request =
                new LocationUpdateRequest(
                        BaseTestEntityFactory.LATITUDE,
                        BaseTestEntityFactory.LONGITUDE,
                        null,
                        100.0,
                        10.0,
                        80,
                        "test-device");

        when(tripRepository.existsById(tripId)).thenReturn(true);
        when(tripRepository.getReferenceById(tripId)).thenReturn(trip);
        when(locationRepository.save(any(Location.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Location createdLocation = locationService.createLocationUpdate(tripId, request);

        // Then
        assertThat(createdLocation).isNotNull();
        assertThat(createdLocation.getTrip().getId()).isEqualTo(tripId);
        assertThat(createdLocation.getLatitude()).isEqualTo(request.latitude());
        assertThat(createdLocation.getLongitude()).isEqualTo(request.longitude());
        assertThat(createdLocation.getAltitude()).isEqualTo(request.altitude());
        assertThat(createdLocation.getTimestamp())
                .isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));

        verify(locationRepository).save(any(Location.class));
    }

    @Test
    void createLocationUpdate_whenTripDoesNotExist_shouldThrowException() {
        // Given
        UUID nonExistentTripId = UUID.randomUUID();
        LocationUpdateRequest request =
                new LocationUpdateRequest(
                        BaseTestEntityFactory.LATITUDE,
                        BaseTestEntityFactory.LONGITUDE,
                        null,
                        null,
                        null,
                        null,
                        "test");

        when(tripRepository.existsById(nonExistentTripId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> locationService.createLocationUpdate(nonExistentTripId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trip not found with ID: " + nonExistentTripId);

        verify(locationRepository, never()).save(any(Location.class));
    }

    @Test
    void createLocationUpdate_withSpecificTimestamp_shouldUseProvidedTimestamp() {
        // Given
        UUID tripId = UUID.randomUUID();
        Trip trip = BaseTestEntityFactory.createTrip(tripId);
        String timestampString = "2025-10-04T10:00:00Z";
        Instant expectedTimestamp = Instant.parse(timestampString);
        LocationUpdateRequest request =
                new LocationUpdateRequest(
                        BaseTestEntityFactory.LATITUDE,
                        BaseTestEntityFactory.LONGITUDE,
                        timestampString,
                        null,
                        null,
                        null,
                        "test");

        when(tripRepository.existsById(tripId)).thenReturn(true);
        when(tripRepository.getReferenceById(tripId)).thenReturn(trip);
        when(locationRepository.save(any(Location.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Location createdLocation = locationService.createLocationUpdate(tripId, request);

        // Then
        assertThat(createdLocation.getTimestamp()).isEqualTo(expectedTimestamp);
        verify(locationRepository).save(any(Location.class));
    }
}
