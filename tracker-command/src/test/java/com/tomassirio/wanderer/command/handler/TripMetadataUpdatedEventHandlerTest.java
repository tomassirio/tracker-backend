package com.tomassirio.wanderer.command.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.TripMetadataUpdatedEvent;
import com.tomassirio.wanderer.command.service.helper.TripEmbeddedObjectsInitializer;
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
class TripMetadataUpdatedEventHandlerTest {

    @Mock private EntityManager entityManager;
    @Mock private TripEmbeddedObjectsInitializer embeddedObjectsInitializer;

    @InjectMocks private TripMetadataUpdatedEventHandler handler;

    @Test
    void handle_whenTripExists_shouldUpdateTripMetadata() {
        // Given
        UUID tripId = UUID.randomUUID();
        TripMetadataUpdatedEvent event =
                TripMetadataUpdatedEvent.builder()
                        .tripId(tripId)
                        .tripName("Updated Camino")
                        .visibility("PRIVATE")
                        .build();

        TripSettings tripSettings =
                TripSettings.builder()
                        .tripStatus(TripStatus.CREATED)
                        .visibility(TripVisibility.PUBLIC)
                        .build();
        Trip trip = Trip.builder().id(tripId).name("Old Name").tripSettings(tripSettings).build();

        when(entityManager.find(Trip.class, tripId)).thenReturn(trip);

        // When
        handler.handle(event);

        // Then
        verify(embeddedObjectsInitializer).ensureTripSettings(trip, TripVisibility.PRIVATE);
        verify(embeddedObjectsInitializer).ensureTripDetails(trip);

        // Entity is managed, no need to verify save
        assertThat(trip.getName()).isEqualTo("Updated Camino");
        assertThat(trip.getTripSettings().getVisibility()).isEqualTo(TripVisibility.PRIVATE);
    }

    @Test
    void handle_whenTripNotFound_shouldNotUpdateOrSave() {
        // Given
        UUID tripId = UUID.randomUUID();
        TripMetadataUpdatedEvent event =
                TripMetadataUpdatedEvent.builder()
                        .tripId(tripId)
                        .tripName("Updated Camino")
                        .visibility("PUBLIC")
                        .build();

        when(entityManager.find(Trip.class, tripId)).thenReturn(null);

        // When
        handler.handle(event);

        // Then
        verify(entityManager).find(Trip.class, tripId);
        // Handler should not call any methods on embeddedObjectsInitializer when trip is null
    }
}
