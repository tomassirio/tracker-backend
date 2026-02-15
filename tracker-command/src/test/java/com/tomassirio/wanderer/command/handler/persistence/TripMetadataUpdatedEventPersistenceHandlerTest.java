package com.tomassirio.wanderer.command.handler.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.TripMetadataUpdatedEvent;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TripMetadataUpdatedEventPersistenceHandlerTest {

    @Mock private TripRepository tripRepository;

    @Mock private TripEmbeddedObjectsInitializer embeddedObjectsInitializer;

    @InjectMocks private TripMetadataUpdatedEventPersistenceHandler handler;

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

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        // When
        handler.handle(event);

        // Then
        verify(embeddedObjectsInitializer).ensureTripSettings(trip, TripVisibility.PRIVATE);
        verify(embeddedObjectsInitializer).ensureTripDetails(trip);

        ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);
        verify(tripRepository).save(tripCaptor.capture());

        Trip savedTrip = tripCaptor.getValue();
        assertThat(savedTrip.getName()).isEqualTo("Updated Camino");
        assertThat(savedTrip.getTripSettings().getVisibility()).isEqualTo(TripVisibility.PRIVATE);
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

        when(tripRepository.findById(tripId)).thenReturn(Optional.empty());

        // When
        handler.handle(event);

        // Then
        verify(tripRepository).findById(tripId);
        verify(tripRepository, org.mockito.Mockito.never()).save(any(Trip.class));
    }
}
