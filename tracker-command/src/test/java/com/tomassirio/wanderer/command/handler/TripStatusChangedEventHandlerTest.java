package com.tomassirio.wanderer.command.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.TripStatusChangedEvent;
import com.tomassirio.wanderer.command.repository.ActiveTripRepository;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.service.helper.TripEmbeddedObjectsInitializer;
import com.tomassirio.wanderer.command.service.helper.TripStatusTransitionHandler;
import com.tomassirio.wanderer.commons.domain.ActiveTrip;
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
class TripStatusChangedEventHandlerTest {

    @Mock private TripRepository tripRepository;

    @Mock private ActiveTripRepository activeTripRepository;

    @Mock private TripEmbeddedObjectsInitializer embeddedObjectsInitializer;

    @Mock private TripStatusTransitionHandler statusTransitionHandler;

    @InjectMocks private TripStatusChangedEventHandler handler;

    @Test
    void handle_whenTripExists_shouldUpdateTripStatus() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TripStatusChangedEvent event =
                TripStatusChangedEvent.builder()
                        .tripId(tripId)
                        .previousStatus("CREATED")
                        .newStatus("IN_PROGRESS")
                        .build();

        TripSettings tripSettings =
                TripSettings.builder()
                        .tripStatus(TripStatus.CREATED)
                        .visibility(TripVisibility.PUBLIC)
                        .build();
        Trip trip = Trip.builder().id(tripId).userId(userId).tripSettings(tripSettings).build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(activeTripRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        handler.handle(event);

        // Then
        verify(embeddedObjectsInitializer)
                .ensureTripSettingsAndGetPreviousStatus(trip, TripStatus.IN_PROGRESS);
        verify(embeddedObjectsInitializer).ensureTripDetails(trip);
        verify(statusTransitionHandler)
                .handleStatusTransition(trip, TripStatus.CREATED, TripStatus.IN_PROGRESS);
        verify(activeTripRepository).save(any(ActiveTrip.class));

        // Entity is managed, no need to verify save
        assertThat(trip.getTripSettings().getTripStatus()).isEqualTo(TripStatus.IN_PROGRESS);
    }

    @Test
    void handle_whenPreviousStatusIsNull_shouldHandleStatusTransitionWithNull() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TripStatusChangedEvent event =
                TripStatusChangedEvent.builder()
                        .tripId(tripId)
                        .previousStatus(null)
                        .newStatus("CREATED")
                        .build();

        TripSettings tripSettings =
                TripSettings.builder()
                        .tripStatus(TripStatus.CREATED)
                        .visibility(TripVisibility.PUBLIC)
                        .build();
        Trip trip = Trip.builder().id(tripId).userId(userId).tripSettings(tripSettings).build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(activeTripRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        handler.handle(event);

        // Then
        verify(statusTransitionHandler).handleStatusTransition(trip, null, TripStatus.CREATED);
    }

    @Test
    void handle_whenTripNotFound_shouldNotUpdateOrSave() {
        // Given
        UUID tripId = UUID.randomUUID();
        TripStatusChangedEvent event =
                TripStatusChangedEvent.builder()
                        .tripId(tripId)
                        .previousStatus("CREATED")
                        .newStatus("IN_PROGRESS")
                        .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.empty());

        // When
        handler.handle(event);

        // Then
        verify(tripRepository).findById(tripId);
        // Handler should not call any methods on embeddedObjectsInitializer when trip is not found
        verifyNoInteractions(embeddedObjectsInitializer);
        verifyNoInteractions(activeTripRepository);
    }

    @Test
    void handle_whenStatusChangedToInProgress_shouldCreateActiveTrip() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TripStatusChangedEvent event =
                TripStatusChangedEvent.builder()
                        .tripId(tripId)
                        .previousStatus("CREATED")
                        .newStatus("IN_PROGRESS")
                        .build();

        TripSettings tripSettings =
                TripSettings.builder()
                        .tripStatus(TripStatus.CREATED)
                        .visibility(TripVisibility.PUBLIC)
                        .build();
        Trip trip = Trip.builder().id(tripId).userId(userId).tripSettings(tripSettings).build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(activeTripRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        handler.handle(event);

        // Then
        verify(activeTripRepository).save(any(ActiveTrip.class));
    }

    @Test
    void handle_whenStatusChangedFromInProgressToPaused_shouldRemoveActiveTrip() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TripStatusChangedEvent event =
                TripStatusChangedEvent.builder()
                        .tripId(tripId)
                        .previousStatus("IN_PROGRESS")
                        .newStatus("PAUSED")
                        .build();

        TripSettings tripSettings =
                TripSettings.builder()
                        .tripStatus(TripStatus.IN_PROGRESS)
                        .visibility(TripVisibility.PUBLIC)
                        .build();
        Trip trip = Trip.builder().id(tripId).userId(userId).tripSettings(tripSettings).build();
        ActiveTrip activeTrip = ActiveTrip.builder().userId(userId).tripId(tripId).build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(activeTripRepository.findById(userId)).thenReturn(Optional.of(activeTrip));

        // When
        handler.handle(event);

        // Then
        verify(activeTripRepository).delete(activeTrip);
    }

    @Test
    void handle_whenStatusChangedFromInProgressToFinished_shouldRemoveActiveTrip() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TripStatusChangedEvent event =
                TripStatusChangedEvent.builder()
                        .tripId(tripId)
                        .previousStatus("IN_PROGRESS")
                        .newStatus("FINISHED")
                        .build();

        TripSettings tripSettings =
                TripSettings.builder()
                        .tripStatus(TripStatus.IN_PROGRESS)
                        .visibility(TripVisibility.PUBLIC)
                        .build();
        Trip trip = Trip.builder().id(tripId).userId(userId).tripSettings(tripSettings).build();
        ActiveTrip activeTrip = ActiveTrip.builder().userId(userId).tripId(tripId).build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(activeTripRepository.findById(userId)).thenReturn(Optional.of(activeTrip));

        // When
        handler.handle(event);

        // Then
        verify(activeTripRepository).delete(activeTrip);
    }
}
