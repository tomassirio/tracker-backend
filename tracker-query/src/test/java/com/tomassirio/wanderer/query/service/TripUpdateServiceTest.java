package com.tomassirio.wanderer.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripUpdate;
import com.tomassirio.wanderer.commons.dto.TripUpdateDTO;
import com.tomassirio.wanderer.query.repository.TripUpdateRepository;
import com.tomassirio.wanderer.query.service.impl.TripUpdateServiceImpl;
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
class TripUpdateServiceTest {

    @Mock private TripUpdateRepository tripUpdateRepository;

    @InjectMocks private TripUpdateServiceImpl tripUpdateService;

    @Test
    void getTripUpdate_whenTripUpdateExists_shouldReturnTripUpdateDTO() {
        // Given
        UUID tripUpdateId = UUID.randomUUID();
        UUID tripId = UUID.randomUUID();
        Trip trip = TestEntityFactory.createTrip(tripId, "Test Trip");
        TripUpdate tripUpdate = TestEntityFactory.createTripUpdate(tripUpdateId, trip);

        when(tripUpdateRepository.findById(tripUpdateId)).thenReturn(Optional.of(tripUpdate));

        // When
        TripUpdateDTO result = tripUpdateService.getTripUpdate(tripUpdateId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(tripUpdateId.toString());
        assertThat(result.tripId()).isEqualTo(tripId.toString());
        assertThat(result.location()).isNotNull();
        assertThat(result.location().getLat()).isEqualTo(TestEntityFactory.LATITUDE);
        assertThat(result.location().getLon()).isEqualTo(TestEntityFactory.LONGITUDE);
        assertThat(result.battery()).isEqualTo(85);
        assertThat(result.message()).isEqualTo("Test update");
        assertThat(result.timestamp()).isNotNull();

        verify(tripUpdateRepository).findById(tripUpdateId);
    }

    @Test
    void getTripUpdate_whenTripUpdateDoesNotExist_shouldThrowEntityNotFoundException() {
        // Given
        UUID nonExistentTripUpdateId = UUID.randomUUID();
        when(tripUpdateRepository.findById(nonExistentTripUpdateId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tripUpdateService.getTripUpdate(nonExistentTripUpdateId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trip update not found");

        verify(tripUpdateRepository).findById(nonExistentTripUpdateId);
    }

    @Test
    void getTripUpdate_shouldMapLocationCorrectly() {
        // Given
        UUID tripUpdateId = UUID.randomUUID();
        UUID tripId = UUID.randomUUID();
        Trip trip = TestEntityFactory.createTrip(tripId);
        TripUpdate tripUpdate = TestEntityFactory.createTripUpdate(tripUpdateId, trip);

        when(tripUpdateRepository.findById(tripUpdateId)).thenReturn(Optional.of(tripUpdate));

        // When
        TripUpdateDTO result = tripUpdateService.getTripUpdate(tripUpdateId);

        // Then
        assertThat(result.location()).isNotNull();
        assertThat(result.location().getLat()).isEqualTo(TestEntityFactory.LATITUDE);
        assertThat(result.location().getLon()).isEqualTo(TestEntityFactory.LONGITUDE);
    }

    @Test
    void getTripUpdatesForTrip_whenTripUpdatesExist_shouldReturnListOfTripUpdateDTOs() {
        // Given
        UUID tripId = UUID.randomUUID();
        Trip trip = TestEntityFactory.createTrip(tripId, "Test Trip");

        UUID updateId1 = UUID.randomUUID();
        UUID updateId2 = UUID.randomUUID();
        UUID updateId3 = UUID.randomUUID();

        TripUpdate update1 = TestEntityFactory.createTripUpdate(updateId1, trip);
        TripUpdate update2 = TestEntityFactory.createTripUpdate(updateId2, trip);
        TripUpdate update3 = TestEntityFactory.createTripUpdate(updateId3, trip);

        List<TripUpdate> tripUpdates = List.of(update1, update2, update3);

        when(tripUpdateRepository.findByTripIdOrderByTimestampDesc(tripId)).thenReturn(tripUpdates);

        // When
        List<TripUpdateDTO> result = tripUpdateService.getTripUpdatesForTrip(tripId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result.get(0).id()).isEqualTo(updateId1.toString());
        assertThat(result.get(1).id()).isEqualTo(updateId2.toString());
        assertThat(result.get(2).id()).isEqualTo(updateId3.toString());
        assertThat(result.get(0).tripId()).isEqualTo(tripId.toString());
        assertThat(result.get(1).tripId()).isEqualTo(tripId.toString());
        assertThat(result.get(2).tripId()).isEqualTo(tripId.toString());

        verify(tripUpdateRepository).findByTripIdOrderByTimestampDesc(tripId);
    }

    @Test
    void getTripUpdatesForTrip_whenNoTripUpdatesExist_shouldReturnEmptyList() {
        // Given
        UUID tripId = UUID.randomUUID();
        when(tripUpdateRepository.findByTripIdOrderByTimestampDesc(tripId))
                .thenReturn(Collections.emptyList());

        // When
        List<TripUpdateDTO> result = tripUpdateService.getTripUpdatesForTrip(tripId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(tripUpdateRepository).findByTripIdOrderByTimestampDesc(tripId);
    }

    @Test
    void getTripUpdatesForTrip_shouldReturnUpdatesOrderedByTimestampDescending() {
        // Given
        UUID tripId = UUID.randomUUID();
        Trip trip = TestEntityFactory.createTrip(tripId);

        TripUpdate update1 = TestEntityFactory.createTripUpdate(UUID.randomUUID(), trip);
        TripUpdate update2 = TestEntityFactory.createTripUpdate(UUID.randomUUID(), trip);

        // Repository should return in descending order
        List<TripUpdate> orderedUpdates = List.of(update2, update1);

        when(tripUpdateRepository.findByTripIdOrderByTimestampDesc(tripId))
                .thenReturn(orderedUpdates);

        // When
        List<TripUpdateDTO> result = tripUpdateService.getTripUpdatesForTrip(tripId);

        // Then
        assertThat(result).hasSize(2);
        // Verify the order is maintained
        assertThat(result.get(0).id()).isEqualTo(update2.getId().toString());
        assertThat(result.get(1).id()).isEqualTo(update1.getId().toString());

        verify(tripUpdateRepository).findByTripIdOrderByTimestampDesc(tripId);
    }

    @Test
    void getTripUpdatesForTrip_shouldMapAllFieldsCorrectly() {
        // Given
        UUID tripId = UUID.randomUUID();
        Trip trip = TestEntityFactory.createTrip(tripId);
        TripUpdate tripUpdate = TestEntityFactory.createTripUpdate(UUID.randomUUID(), trip);

        when(tripUpdateRepository.findByTripIdOrderByTimestampDesc(tripId))
                .thenReturn(List.of(tripUpdate));

        // When
        List<TripUpdateDTO> result = tripUpdateService.getTripUpdatesForTrip(tripId);

        // Then
        assertThat(result).hasSize(1);
        TripUpdateDTO dto = result.get(0);
        assertThat(dto.id()).isNotNull();
        assertThat(dto.tripId()).isEqualTo(tripId.toString());
        assertThat(dto.location()).isNotNull();
        assertThat(dto.battery()).isEqualTo(85);
        assertThat(dto.message()).isEqualTo("Test update");
        assertThat(dto.timestamp()).isNotNull();
    }
}
