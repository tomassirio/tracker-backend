package com.tomassirio.wanderer.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import com.tomassirio.wanderer.commons.domain.User;
import com.tomassirio.wanderer.commons.dto.TripDTO;
import com.tomassirio.wanderer.query.repository.TripRepository;
import com.tomassirio.wanderer.query.service.impl.TripServiceImpl;
import com.tomassirio.wanderer.query.utils.TestEntityFactory;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
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
    void getTrip_whenTripExists_shouldReturnTripDTO() {
        // Given
        UUID tripId = UUID.randomUUID();
        Trip trip = TestEntityFactory.createTrip(tripId, "Test Trip");

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        // When
        TripDTO result = tripService.getTrip(tripId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(tripId);
        assertThat(result.name()).isEqualTo("Test Trip");
        assertThat(result.totalDistance()).isEqualTo(1500.0);
        assertThat(result.visibility()).isEqualTo(TripVisibility.PUBLIC);

        verify(tripRepository).findById(tripId);
    }

    @Test
    void getTrip_whenTripDoesNotExist_shouldThrowEntityNotFoundException() {
        // Given
        UUID nonExistentTripId = UUID.randomUUID();
        when(tripRepository.findById(nonExistentTripId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tripService.getTrip(nonExistentTripId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trip not found");

        verify(tripRepository).findById(nonExistentTripId);
    }

    @Test
    void getAllTrips_whenTripsExist_shouldReturnListOfTripDTOs() {
        // Given
        Trip trip1 =
                TestEntityFactory.createTrip(
                        UUID.randomUUID(),
                        "Trip 1",
                        LocalDate.now().minusDays(10),
                        LocalDate.now().minusDays(5),
                        500.0,
                        TripVisibility.PUBLIC);

        Trip trip2 =
                TestEntityFactory.createTrip(
                        UUID.randomUUID(),
                        "Trip 2",
                        LocalDate.now().minusDays(3),
                        LocalDate.now().plusDays(3),
                        800.0,
                        TripVisibility.PRIVATE);

        List<Trip> trips = List.of(trip1, trip2);
        when(tripRepository.findAll()).thenReturn(trips);

        // When
        List<TripDTO> result = tripService.getAllTrips();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().name()).isEqualTo("Trip 1");
        assertThat(result.get(0).totalDistance()).isEqualTo(500.0);
        assertThat(result.get(0).visibility()).isEqualTo(TripVisibility.PUBLIC);
        assertThat(result.get(1).name()).isEqualTo("Trip 2");
        assertThat(result.get(1).totalDistance()).isEqualTo(800.0);
        assertThat(result.get(1).visibility()).isEqualTo(TripVisibility.PRIVATE);

        verify(tripRepository).findAll();
    }

    @Test
    void getAllTrips_whenNoTripsExist_shouldReturnEmptyList() {
        // Given
        when(tripRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<TripDTO> result = tripService.getAllTrips();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(tripRepository).findAll();
    }

    @Test
    void getAllTrips_shouldMapAllFieldsCorrectly() {
        // Given
        UUID tripId = UUID.randomUUID();
        LocalDate startDate = LocalDate.of(2025, 6, 1);
        LocalDate endDate = LocalDate.of(2025, 6, 15);

        Trip trip =
                TestEntityFactory.createTrip(
                        tripId,
                        "Summer Road Trip",
                        startDate,
                        endDate,
                        2500.5,
                        TripVisibility.PROTECTED);

        when(tripRepository.findAll()).thenReturn(List.of(trip));

        // When
        List<TripDTO> result = tripService.getAllTrips();

        // Then
        assertThat(result).hasSize(1);
        TripDTO tripDTO = result.getFirst();
        assertThat(tripDTO.id()).isEqualTo(tripId);
        assertThat(tripDTO.name()).isEqualTo("Summer Road Trip");
        assertThat(tripDTO.startDate()).isEqualTo(startDate);
        assertThat(tripDTO.endDate()).isEqualTo(endDate);
        assertThat(tripDTO.totalDistance()).isEqualTo(2500.5);
        assertThat(tripDTO.visibility()).isEqualTo(TripVisibility.PROTECTED);

        verify(tripRepository).findAll();
    }

    @Test
    void getTripsForUser_whenTripsExist_shouldReturnTripDTOs() {
        // Given
        UUID userId = TestEntityFactory.USER_ID;
        UUID tripId = UUID.randomUUID();
        Trip trip = TestEntityFactory.createTrip(tripId, "Owned Trip");
        trip.setOwner(User.builder().id(userId).build());

        when(tripRepository.findByOwnerId(userId)).thenReturn(List.of(trip));

        // When
        List<TripDTO> result = tripService.getTripsForUser(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        TripDTO dto = result.getFirst();
        assertThat(dto.id()).isEqualTo(tripId);
        assertThat(dto.ownerId()).isEqualTo(userId);

        verify(tripRepository).findByOwnerId(userId);
    }

    @Test
    void getTripsForUser_whenNoTripsExist_shouldReturnEmptyList() {
        // Given
        UUID userId = TestEntityFactory.USER_ID;
        when(tripRepository.findByOwnerId(userId)).thenReturn(Collections.emptyList());

        // When
        List<TripDTO> result = tripService.getTripsForUser(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(tripRepository).findByOwnerId(userId);
    }
}
