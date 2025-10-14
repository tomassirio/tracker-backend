package com.tomassirio.wanderer.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripStatus;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import com.tomassirio.wanderer.commons.dto.TripDTO;
import com.tomassirio.wanderer.query.repository.TripRepository;
import com.tomassirio.wanderer.query.service.impl.TripServiceImpl;
import com.tomassirio.wanderer.query.utils.TestEntityFactory;
import jakarta.persistence.EntityNotFoundException;
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
        assertThat(result.userId()).isEqualTo(TestEntityFactory.USER_ID);
        assertThat(result.visibility()).isEqualTo(TripVisibility.PUBLIC);
        assertThat(result.tripStatus()).isEqualTo(TripStatus.CREATED);
        assertThat(result.enabled()).isTrue();

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
                TestEntityFactory.createTrip(UUID.randomUUID(), "Trip 1", TripVisibility.PUBLIC);
        Trip trip2 =
                TestEntityFactory.createTrip(UUID.randomUUID(), "Trip 2", TripVisibility.PRIVATE);

        List<Trip> trips = List.of(trip1, trip2);
        when(tripRepository.findAll()).thenReturn(trips);

        // When
        List<TripDTO> result = tripService.getAllTrips();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().name()).isEqualTo("Trip 1");
        assertThat(result.get(0).visibility()).isEqualTo(TripVisibility.PUBLIC);
        assertThat(result.get(0).tripStatus()).isEqualTo(TripStatus.CREATED);
        assertThat(result.get(1).name()).isEqualTo("Trip 2");
        assertThat(result.get(1).visibility()).isEqualTo(TripVisibility.PRIVATE);
        assertThat(result.get(1).tripStatus()).isEqualTo(TripStatus.CREATED);

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

        Trip trip =
                TestEntityFactory.createTrip(tripId, "Summer Road Trip", TripVisibility.PROTECTED);

        when(tripRepository.findAll()).thenReturn(List.of(trip));

        // When
        List<TripDTO> result = tripService.getAllTrips();

        // Then
        assertThat(result).hasSize(1);
        TripDTO tripDTO = result.getFirst();
        assertThat(tripDTO.id()).isEqualTo(tripId);
        assertThat(tripDTO.name()).isEqualTo("Summer Road Trip");
        assertThat(tripDTO.userId()).isEqualTo(TestEntityFactory.USER_ID);
        assertThat(tripDTO.visibility()).isEqualTo(TripVisibility.PROTECTED);
        assertThat(tripDTO.tripStatus()).isEqualTo(TripStatus.CREATED);
        assertThat(tripDTO.enabled()).isTrue();
        assertThat(tripDTO.creationTimestamp()).isNotNull();

        verify(tripRepository).findAll();
    }

    @Test
    void getTripsForUser_whenTripsExist_shouldReturnTripDTOs() {
        // Given
        UUID userId = TestEntityFactory.USER_ID;
        UUID tripId = UUID.randomUUID();
        Trip trip = TestEntityFactory.createTrip(tripId, "Owned Trip");

        when(tripRepository.findByUserId(userId)).thenReturn(List.of(trip));

        // When
        List<TripDTO> result = tripService.getTripsForUser(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        TripDTO dto = result.getFirst();
        assertThat(dto.id()).isEqualTo(tripId);
        assertThat(dto.userId()).isEqualTo(userId);
        assertThat(dto.name()).isEqualTo("Owned Trip");

        verify(tripRepository).findByUserId(userId);
    }

    @Test
    void getTripsForUser_whenNoTripsExist_shouldReturnEmptyList() {
        // Given
        UUID userId = TestEntityFactory.USER_ID;
        when(tripRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        // When
        List<TripDTO> result = tripService.getTripsForUser(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(tripRepository).findByUserId(userId);
    }
}
