package com.tomassirio.wanderer.command.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.repository.TripUpdateRepository;
import com.tomassirio.wanderer.command.service.impl.TripUpdateGeocodingServiceImpl;
import com.tomassirio.wanderer.command.utils.TestEntityFactory;
import com.tomassirio.wanderer.commons.domain.GeoLocation;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripUpdate;
import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TripUpdateGeocodingServiceImplTest {

    @Mock private TripRepository tripRepository;

    @Mock private TripUpdateRepository tripUpdateRepository;

    @Mock private GeocodingService geocodingService;

    @InjectMocks private TripUpdateGeocodingServiceImpl tripUpdateGeocodingService;

    @Captor private ArgumentCaptor<List<TripUpdate>> updatesCaptor;

    @Test
    void recomputeGeocoding_whenTripNotFound_shouldThrowEntityNotFoundException() {
        // Given
        UUID tripId = UUID.randomUUID();
        when(tripRepository.findById(tripId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tripUpdateGeocodingService.recomputeGeocoding(tripId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trip not found");

        verify(tripUpdateRepository, never()).findByTripIdOrderByTimestampAsc(any());
    }

    @Test
    void recomputeGeocoding_whenNoUpdates_shouldSaveEmptyList() {
        // Given
        UUID tripId = UUID.randomUUID();
        Trip trip = TestEntityFactory.createTrip(tripId);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripUpdateRepository.findByTripIdOrderByTimestampAsc(tripId))
                .thenReturn(Collections.emptyList());

        // When
        tripUpdateGeocodingService.recomputeGeocoding(tripId);

        // Then
        verify(tripUpdateRepository).saveAll(updatesCaptor.capture());
        assertThat(updatesCaptor.getValue()).isEmpty();
    }

    @Test
    void recomputeGeocoding_whenGeocodingSucceeds_shouldUpdateCityAndCountry() {
        // Given
        UUID tripId = UUID.randomUUID();
        Trip trip = TestEntityFactory.createTrip(tripId);
        TripUpdate update = TestEntityFactory.createTripUpdate(UUID.randomUUID(), trip);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripUpdateRepository.findByTripIdOrderByTimestampAsc(tripId))
                .thenReturn(List.of(update));
        when(geocodingService.reverseGeocode(update.getLocation()))
                .thenReturn(new GeocodingService.GeocodingResult("León", "Spain"));

        // When
        tripUpdateGeocodingService.recomputeGeocoding(tripId);

        // Then
        verify(tripUpdateRepository).saveAll(updatesCaptor.capture());
        List<TripUpdate> saved = updatesCaptor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getCity()).isEqualTo("León");
        assertThat(saved.get(0).getCountry()).isEqualTo("Spain");
    }

    @Test
    void recomputeGeocoding_whenGeocodingReturnsNull_shouldClearCityAndCountry() {
        // Given
        UUID tripId = UUID.randomUUID();
        Trip trip = TestEntityFactory.createTrip(tripId);
        TripUpdate update = TestEntityFactory.createTripUpdate(UUID.randomUUID(), trip);
        update.setCity("Old City");
        update.setCountry("Old Country");

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripUpdateRepository.findByTripIdOrderByTimestampAsc(tripId))
                .thenReturn(List.of(update));
        when(geocodingService.reverseGeocode(update.getLocation())).thenReturn(null);

        // When
        tripUpdateGeocodingService.recomputeGeocoding(tripId);

        // Then
        verify(tripUpdateRepository).saveAll(updatesCaptor.capture());
        List<TripUpdate> saved = updatesCaptor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getCity()).isNull();
        assertThat(saved.get(0).getCountry()).isNull();
    }

    @Test
    void recomputeGeocoding_withMultipleUpdates_shouldGeocodeEachOne() {
        // Given
        UUID tripId = UUID.randomUUID();
        Trip trip = TestEntityFactory.createTrip(tripId);
        TripUpdate update1 = TestEntityFactory.createTripUpdate(UUID.randomUUID(), trip);
        TripUpdate update2 = TestEntityFactory.createTripUpdate(UUID.randomUUID(), trip);

        // Give each update a distinct location so Mockito can distinguish the stubs
        GeoLocation loc1 = GeoLocation.builder().lat(42.8125).lon(-1.6458).build();
        GeoLocation loc2 = GeoLocation.builder().lat(42.3440).lon(-3.6969).build();
        update1.setLocation(loc1);
        update2.setLocation(loc2);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripUpdateRepository.findByTripIdOrderByTimestampAsc(tripId))
                .thenReturn(List.of(update1, update2));
        when(geocodingService.reverseGeocode(loc1))
                .thenReturn(new GeocodingService.GeocodingResult("Pamplona", "Spain"));
        when(geocodingService.reverseGeocode(loc2))
                .thenReturn(new GeocodingService.GeocodingResult("Burgos", "Spain"));

        // When
        tripUpdateGeocodingService.recomputeGeocoding(tripId);

        // Then
        verify(tripUpdateRepository).saveAll(updatesCaptor.capture());
        List<TripUpdate> saved = updatesCaptor.getValue();
        assertThat(saved).hasSize(2);
        assertThat(saved.get(0).getCity()).isEqualTo("Pamplona");
        assertThat(saved.get(1).getCity()).isEqualTo("Burgos");
    }

    @Test
    void recomputeGeocoding_shouldNotTouchOtherFields() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID updateId = UUID.randomUUID();
        Trip trip = TestEntityFactory.createTrip(tripId);
        TripUpdate update = TestEntityFactory.createTripUpdate(updateId, trip);
        String originalMessage = update.getMessage();
        Integer originalBattery = update.getBattery();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripUpdateRepository.findByTripIdOrderByTimestampAsc(tripId))
                .thenReturn(List.of(update));
        when(geocodingService.reverseGeocode(update.getLocation()))
                .thenReturn(new GeocodingService.GeocodingResult("Santiago", "Spain"));

        // When
        tripUpdateGeocodingService.recomputeGeocoding(tripId);

        // Then
        verify(tripUpdateRepository).saveAll(updatesCaptor.capture());
        TripUpdate saved = updatesCaptor.getValue().get(0);
        assertThat(saved.getId()).isEqualTo(updateId);
        assertThat(saved.getMessage()).isEqualTo(originalMessage);
        assertThat(saved.getBattery()).isEqualTo(originalBattery);
        assertThat(saved.getCity()).isEqualTo("Santiago");
        assertThat(saved.getCountry()).isEqualTo("Spain");
    }
}
