package com.tomassirio.wanderer.command.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.TripStatusChangedEvent;
import com.tomassirio.wanderer.command.service.helper.TripEmbeddedObjectsInitializer;
import com.tomassirio.wanderer.command.service.helper.TripStatusTransitionHandler;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripSettings;
import com.tomassirio.wanderer.commons.domain.TripStatus;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TripStatusChangedEventHandlerTest {

    @Mock private EntityManager entityManager;

    @Mock private TripEmbeddedObjectsInitializer embeddedObjectsInitializer;

    @Mock private TripStatusTransitionHandler statusTransitionHandler;

    @InjectMocks private TripStatusChangedEventHandler handler;

    @Test
    void handle_whenTripExists_shouldUpdateTripStatus() {
        // Given
        UUID tripId = UUID.randomUUID();
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
        Trip trip = Trip.builder().id(tripId).tripSettings(tripSettings).build();

        when(entityManager.find(Trip.class, tripId)).thenReturn(trip);

        // When
        handler.handle(event);

        // Then
        verify(embeddedObjectsInitializer)
                .ensureTripSettingsAndGetPreviousStatus(trip, TripStatus.IN_PROGRESS);
        verify(embeddedObjectsInitializer).ensureTripDetails(trip);
        verify(statusTransitionHandler)
                .handleStatusTransition(trip, TripStatus.CREATED, TripStatus.IN_PROGRESS);

        // Entity is managed, no need to verify save
        assertThat(trip.getTripSettings().getTripStatus()).isEqualTo(TripStatus.IN_PROGRESS);
    }

    @Test
    void handle_whenPreviousStatusIsNull_shouldHandleStatusTransitionWithNull() {
        // Given
        UUID tripId = UUID.randomUUID();
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
        Trip trip = Trip.builder().id(tripId).tripSettings(tripSettings).build();

        when(entityManager.find(Trip.class, tripId)).thenReturn(trip);

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

        when(entityManager.find(Trip.class, tripId)).thenReturn(null);

        // When
        handler.handle(event);

        // Then
        verify(entityManager).find(Trip.class, tripId);
        // Handler should not call any methods on embeddedObjectsInitializer when trip is null
    }
}
