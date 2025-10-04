package com.tomassirio.wanderer.command.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.dto.LocationRequest;
import com.tomassirio.wanderer.command.dto.TripCreationRequest;
import com.tomassirio.wanderer.command.dto.TripUpdateRequest;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.service.impl.TripServiceImpl;
import com.tomassirio.wanderer.command.utils.TestEntityFactory;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import com.tomassirio.wanderer.commons.dto.TripDTO;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock private TripRepository tripRepository;

    @InjectMocks private TripServiceImpl tripService;

    @Test
    void createTrip_whenValidRequest_shouldCreateAndSaveTrip() {
        // Given
        LocationRequest startLocation =
                TestEntityFactory.createLocationRequest(39.7392, -104.9903, 1609.3);
        LocationRequest endLocation =
                TestEntityFactory.createLocationRequest(37.7749, -122.4194, 16.0);
        TripCreationRequest request =
                TestEntityFactory.createTripCreationRequest(
                        "Summer Road Trip", startLocation, endLocation);

        when(tripRepository.save(any(Trip.class)))
                .thenAnswer(
                        invocation -> {
                            Trip trip = invocation.getArgument(0);
                            trip.setId(UUID.randomUUID());
                            return trip;
                        });

        // When
        TripDTO createdTrip = tripService.createTrip(request);

        // Then
        assertThat(createdTrip).isNotNull();
        assertThat(createdTrip.id()).isNotNull();
        assertThat(createdTrip.name()).isEqualTo("Summer Road Trip");
        assertThat(createdTrip.visibility()).isEqualTo(TripVisibility.PUBLIC);
        assertThat(createdTrip.startingLocation()).isNotNull();
        assertThat(createdTrip.startingLocation().latitude()).isEqualTo(39.7392);
        assertThat(createdTrip.startingLocation().longitude()).isEqualTo(-104.9903);
        assertThat(createdTrip.endingLocation()).isNotNull();
        assertThat(createdTrip.endingLocation().latitude()).isEqualTo(37.7749);
        assertThat(createdTrip.endingLocation().longitude()).isEqualTo(-122.4194);

        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void createTrip_whenLocationRequestsHaveNoAltitude_shouldCreateTripSuccessfully() {
        // Given
        LocationRequest startLocation =
                TestEntityFactory.createLocationRequest(39.7392, -104.9903, null);
        LocationRequest endLocation =
                TestEntityFactory.createLocationRequest(37.7749, -122.4194, null);
        TripCreationRequest request =
                TestEntityFactory.createTripCreationRequest(
                        "Road Trip Without Altitude", startLocation, endLocation);

        when(tripRepository.save(any(Trip.class)))
                .thenAnswer(
                        invocation -> {
                            Trip trip = invocation.getArgument(0);
                            trip.setId(UUID.randomUUID());
                            return trip;
                        });

        // When
        TripDTO createdTrip = tripService.createTrip(request);

        // Then
        assertThat(createdTrip).isNotNull();
        assertThat(createdTrip.id()).isNotNull();
        assertThat(createdTrip.name()).isEqualTo("Road Trip Without Altitude");
        assertThat(createdTrip.startingLocation()).isNotNull();
        assertThat(createdTrip.startingLocation().altitude()).isNull();
        assertThat(createdTrip.endingLocation()).isNotNull();
        assertThat(createdTrip.endingLocation().altitude()).isNull();

        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void createTrip_whenOnlyStartingLocationProvided_shouldCreateTripWithStartingLocationOnly() {
        // Given
        LocationRequest startLocation = TestEntityFactory.createLocationRequest();
        TripCreationRequest request =
                TestEntityFactory.createTripCreationRequest("One Way Trip", startLocation, null);

        when(tripRepository.save(any(Trip.class)))
                .thenAnswer(
                        invocation -> {
                            Trip trip = invocation.getArgument(0);
                            trip.setId(UUID.randomUUID());
                            return trip;
                        });

        // When
        TripDTO createdTrip = tripService.createTrip(request);

        // Then
        assertThat(createdTrip).isNotNull();
        assertThat(createdTrip.startingLocation()).isNotNull();
        assertThat(createdTrip.endingLocation()).isNull();

        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void updateTrip_whenTripExists_shouldUpdateAndSaveTrip() {
        // Given
        UUID tripId = UUID.randomUUID();
        Trip existingTrip =
                Trip.builder()
                        .id(tripId)
                        .name("Old Trip Name")
                        .startDate(LocalDate.now().minusDays(20))
                        .endDate(LocalDate.now().minusDays(10))
                        .totalDistance(500.0)
                        .visibility(TripVisibility.PRIVATE)
                        .build();

        LocationRequest newStartLocation =
                TestEntityFactory.createLocationRequest(40.7128, -74.0060, 10.0);
        LocationRequest newEndLocation =
                TestEntityFactory.createLocationRequest(34.0522, -118.2437, 71.0);
        TripUpdateRequest request =
                TestEntityFactory.createTripUpdateRequest(
                        "Updated Trip Name",
                        newStartLocation,
                        newEndLocation,
                        2000.0,
                        TripVisibility.PRIVATE);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(existingTrip));
        when(tripRepository.save(any(Trip.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TripDTO updatedTrip = tripService.updateTrip(tripId, request);

        // Then
        assertThat(updatedTrip).isNotNull();
        assertThat(updatedTrip.id()).isEqualTo(tripId);
        assertThat(updatedTrip.name()).isEqualTo("Updated Trip Name");
        assertThat(updatedTrip.totalDistance()).isEqualTo(2000.0);
        assertThat(updatedTrip.visibility()).isEqualTo(TripVisibility.PRIVATE);
        assertThat(updatedTrip.startingLocation()).isNotNull();
        assertThat(updatedTrip.startingLocation().latitude()).isEqualTo(40.7128);
        assertThat(updatedTrip.endingLocation()).isNotNull();
        assertThat(updatedTrip.endingLocation().latitude()).isEqualTo(34.0522);

        verify(tripRepository).findById(tripId);
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void updateTrip_whenTripDoesNotExist_shouldThrowException() {
        // Given
        UUID nonExistentTripId = UUID.randomUUID();
        LocationRequest startLocation = TestEntityFactory.createLocationRequest();
        TripUpdateRequest request =
                TestEntityFactory.createTripUpdateRequest("Updated Trip", startLocation, null);

        when(tripRepository.findById(nonExistentTripId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tripService.updateTrip(nonExistentTripId, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trip not found");

        verify(tripRepository).findById(nonExistentTripId);
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void updateTrip_whenUpdatingLocations_shouldReplaceExistingLocations() {
        // Given
        UUID tripId = UUID.randomUUID();
        Trip existingTrip =
                Trip.builder()
                        .id(tripId)
                        .name("Trip Name")
                        .startDate(LocalDate.now().minusDays(5))
                        .endDate(LocalDate.now().plusDays(5))
                        .visibility(TripVisibility.PUBLIC)
                        .build();

        LocationRequest newStartLocation =
                TestEntityFactory.createLocationRequest(50.0, -100.0, 500.0);
        LocationRequest newEndLocation =
                TestEntityFactory.createLocationRequest(45.0, -95.0, 450.0);
        TripUpdateRequest request =
                TestEntityFactory.createTripUpdateRequest(
                        "Trip Name", newStartLocation, newEndLocation);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(existingTrip));
        when(tripRepository.save(any(Trip.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TripDTO updatedTrip = tripService.updateTrip(tripId, request);

        // Then
        assertThat(updatedTrip.startingLocation()).isNotNull();
        assertThat(updatedTrip.startingLocation().latitude()).isEqualTo(50.0);
        assertThat(updatedTrip.startingLocation().altitude()).isEqualTo(500.0);
        assertThat(updatedTrip.endingLocation()).isNotNull();
        assertThat(updatedTrip.endingLocation().latitude()).isEqualTo(45.0);
        assertThat(updatedTrip.endingLocation().altitude()).isEqualTo(450.0);

        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void deleteTrip_whenCalled_shouldCallRepositoryDelete() {
        // Given
        UUID tripId = UUID.randomUUID();

        // When
        tripService.deleteTrip(tripId);

        // Then
        verify(tripRepository).deleteById(tripId);
    }

    @Test
    void createTrip_whenNoEndingLocation_shouldCreateTripWithoutEndingLocation() {
        // Given
        LocationRequest startLocation = TestEntityFactory.createLocationRequest();
        TripCreationRequest request =
                TestEntityFactory.createTripCreationRequest("Trip to Nowhere", startLocation, null);

        when(tripRepository.save(any(Trip.class)))
                .thenAnswer(
                        invocation -> {
                            Trip trip = invocation.getArgument(0);
                            trip.setId(UUID.randomUUID());
                            return trip;
                        });

        // When
        TripDTO createdTrip = tripService.createTrip(request);

        // Then
        assertThat(createdTrip).isNotNull();
        assertThat(createdTrip.name()).isEqualTo("Trip to Nowhere");
        assertThat(createdTrip.startingLocation()).isNotNull();
        assertThat(createdTrip.endingLocation()).isNull();

        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void createTrip_shouldSetSourceAsTrip_ENDPOINT() {
        // Given
        LocationRequest startLocation = TestEntityFactory.createLocationRequest();
        TripCreationRequest request =
                TestEntityFactory.createTripCreationRequest("Test Trip", startLocation, null);

        when(tripRepository.save(any(Trip.class)))
                .thenAnswer(
                        invocation -> {
                            Trip trip = invocation.getArgument(0);
                            trip.setId(UUID.randomUUID());
                            return trip;
                        });

        // When
        TripDTO createdTrip = tripService.createTrip(request);

        // Then
        assertThat(createdTrip.startingLocation()).isNotNull();
        assertThat(createdTrip.startingLocation().source()).isEqualTo("TRIP_STARTPOINT");

        verify(tripRepository).save(any(Trip.class));
    }
}
