package com.tomassirio.wanderer.command.handler.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.TripUpdatedEvent;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.repository.TripUpdateRepository;
import com.tomassirio.wanderer.commons.domain.GeoLocation;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripUpdate;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TripUpdatedEventPersistenceHandlerTest {

    @Mock private TripUpdateRepository tripUpdateRepository;
    @Mock private TripRepository tripRepository;

    @InjectMocks private TripUpdatedEventPersistenceHandler handler;

    @Test
    void handle_shouldPersistTripUpdate() {
        UUID tripId = UUID.randomUUID();
        UUID tripUpdateId = UUID.randomUUID();
        GeoLocation location = GeoLocation.builder().lat(42.8805).lon(-8.5457).build();
        Instant timestamp = Instant.now();

        Trip trip = Trip.builder().id(tripId).name("Camino").build();

        TripUpdatedEvent event =
                TripUpdatedEvent.builder()
                        .tripUpdateId(tripUpdateId)
                        .tripId(tripId)
                        .location(location)
                        .batteryLevel(85)
                        .message("Arrived at Santiago!")
                        .timestamp(timestamp)
                        .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        handler.handle(event);

        ArgumentCaptor<TripUpdate> captor = ArgumentCaptor.forClass(TripUpdate.class);
        verify(tripUpdateRepository).save(captor.capture());

        TripUpdate saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(tripUpdateId);
        assertThat(saved.getTrip()).isEqualTo(trip);
        assertThat(saved.getLocation()).isEqualTo(location);
        assertThat(saved.getBattery()).isEqualTo(85);
        assertThat(saved.getMessage()).isEqualTo("Arrived at Santiago!");
        assertThat(saved.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    void handle_whenTripNotFound_shouldThrowEntityNotFoundException() {
        UUID tripId = UUID.randomUUID();
        TripUpdatedEvent event =
                TripUpdatedEvent.builder().tripUpdateId(UUID.randomUUID()).tripId(tripId).build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(event))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trip not found");

        verify(tripUpdateRepository, never()).save(any());
    }
}
