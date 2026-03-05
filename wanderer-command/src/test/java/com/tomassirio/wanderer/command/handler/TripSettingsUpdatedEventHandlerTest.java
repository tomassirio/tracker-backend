package com.tomassirio.wanderer.command.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.TripSettingsUpdatedEvent;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.service.helper.TripEmbeddedObjectsInitializer;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripSettings;
import com.tomassirio.wanderer.commons.domain.TripStatus;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TripSettingsUpdatedEventHandlerTest {

    @Mock private TripRepository tripRepository;
    @Mock private TripEmbeddedObjectsInitializer embeddedObjectsInitializer;

    @InjectMocks private TripSettingsUpdatedEventHandler handler;

    @Test
    void handle_whenTripExists_shouldUpdateBothSettings() {
        // Given
        UUID tripId = UUID.randomUUID();
        TripSettingsUpdatedEvent event =
                TripSettingsUpdatedEvent.builder()
                        .tripId(tripId)
                        .updateRefresh(120)
                        .automaticUpdates(true)
                        .build();

        TripSettings tripSettings =
                TripSettings.builder()
                        .tripStatus(TripStatus.CREATED)
                        .visibility(TripVisibility.PUBLIC)
                        .updateRefresh(60)
                        .automaticUpdates(false)
                        .build();
        Trip trip = Trip.builder().id(tripId).tripSettings(tripSettings).build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        // When
        handler.handle(event);

        // Then
        verify(embeddedObjectsInitializer).ensureTripSettings(trip, null);

        // Entity is managed, no need to verify save
        assertThat(trip.getTripSettings().getUpdateRefresh()).isEqualTo(120);
        assertThat(trip.getTripSettings().getAutomaticUpdates()).isTrue();
    }

    @Test
    void handle_whenOnlyUpdateRefresh_shouldUpdateOnlyThatField() {
        // Given
        UUID tripId = UUID.randomUUID();
        TripSettingsUpdatedEvent event =
                TripSettingsUpdatedEvent.builder()
                        .tripId(tripId)
                        .updateRefresh(180)
                        .automaticUpdates(null)
                        .build();

        TripSettings tripSettings =
                TripSettings.builder()
                        .tripStatus(TripStatus.CREATED)
                        .visibility(TripVisibility.PUBLIC)
                        .updateRefresh(60)
                        .automaticUpdates(true)
                        .build();
        Trip trip = Trip.builder().id(tripId).tripSettings(tripSettings).build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        // When
        handler.handle(event);

        // Then
        verify(embeddedObjectsInitializer).ensureTripSettings(trip, null);
        assertThat(trip.getTripSettings().getUpdateRefresh()).isEqualTo(180);
        assertThat(trip.getTripSettings().getAutomaticUpdates()).isTrue(); // Unchanged
    }

    @Test
    void handle_whenOnlyAutomaticUpdates_shouldUpdateOnlyThatField() {
        // Given
        UUID tripId = UUID.randomUUID();
        TripSettingsUpdatedEvent event =
                TripSettingsUpdatedEvent.builder()
                        .tripId(tripId)
                        .updateRefresh(null)
                        .automaticUpdates(false)
                        .build();

        TripSettings tripSettings =
                TripSettings.builder()
                        .tripStatus(TripStatus.CREATED)
                        .visibility(TripVisibility.PUBLIC)
                        .updateRefresh(120)
                        .automaticUpdates(true)
                        .build();
        Trip trip = Trip.builder().id(tripId).tripSettings(tripSettings).build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        // When
        handler.handle(event);

        // Then
        verify(embeddedObjectsInitializer).ensureTripSettings(trip, null);
        assertThat(trip.getTripSettings().getUpdateRefresh()).isEqualTo(120); // Unchanged
        assertThat(trip.getTripSettings().getAutomaticUpdates()).isFalse();
    }

    @Test
    void handle_whenTripNotFound_shouldNotUpdateOrSave() {
        // Given
        UUID tripId = UUID.randomUUID();
        TripSettingsUpdatedEvent event =
                TripSettingsUpdatedEvent.builder()
                        .tripId(tripId)
                        .updateRefresh(120)
                        .automaticUpdates(true)
                        .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.empty());

        // When
        handler.handle(event);

        // Then
        verify(tripRepository).findById(tripId);
        // Handler should not call any methods on embeddedObjectsInitializer when trip is not found
        verifyNoInteractions(embeddedObjectsInitializer);
    }
}
