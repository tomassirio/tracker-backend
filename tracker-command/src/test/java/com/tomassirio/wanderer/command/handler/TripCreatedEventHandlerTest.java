package com.tomassirio.wanderer.command.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.TripCreatedEvent;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.service.helper.TripEmbeddedObjectsInitializer;
import com.tomassirio.wanderer.commons.domain.GeoLocation;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripDetails;
import com.tomassirio.wanderer.commons.domain.TripSettings;
import com.tomassirio.wanderer.commons.domain.TripStatus;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TripCreatedEventHandlerTest {

    @Mock private TripEmbeddedObjectsInitializer embeddedObjectsInitializer;
    @Mock private TripRepository tripRepository;

    @InjectMocks private TripCreatedEventHandler handler;

    @Test
    void handle_whenEventWithMinimalData_shouldPersistTrip() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Instant creationTimestamp = Instant.now();
        TripCreatedEvent event =
                TripCreatedEvent.builder()
                        .tripId(tripId)
                        .tripName("Camino de Santiago")
                        .ownerId(ownerId)
                        .visibility("PUBLIC")
                        .creationTimestamp(creationTimestamp)
                        .build();

        TripSettings tripSettings =
                TripSettings.builder()
                        .tripStatus(TripStatus.CREATED)
                        .visibility(TripVisibility.PUBLIC)
                        .build();
        TripDetails tripDetails = TripDetails.builder().build();

        when(embeddedObjectsInitializer.createTripSettings(TripVisibility.PUBLIC))
                .thenReturn(tripSettings);
        when(embeddedObjectsInitializer.createTripDetails()).thenReturn(tripDetails);

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);
        verify(tripRepository).save(tripCaptor.capture());

        Trip savedTrip = tripCaptor.getValue();
        assertThat(savedTrip.getId()).isEqualTo(tripId);
        assertThat(savedTrip.getName()).isEqualTo("Camino de Santiago");
        assertThat(savedTrip.getUserId()).isEqualTo(ownerId);
        assertThat(savedTrip.getCreationTimestamp()).isEqualTo(creationTimestamp);
        assertThat(savedTrip.getTripSettings()).isEqualTo(tripSettings);
        assertThat(savedTrip.getTripDetails()).isEqualTo(tripDetails);
        assertThat(savedTrip.getEnabled()).isTrue();
    }

    @Test
    void handle_whenEventWithTripPlan_shouldPersistTripWithPlanId() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID tripPlanId = UUID.randomUUID();
        Instant creationTimestamp = Instant.now();
        TripCreatedEvent event =
                TripCreatedEvent.builder()
                        .tripId(tripId)
                        .tripName("Camino de Santiago")
                        .ownerId(ownerId)
                        .visibility("PRIVATE")
                        .tripPlanId(tripPlanId)
                        .creationTimestamp(creationTimestamp)
                        .build();

        TripSettings tripSettings =
                TripSettings.builder()
                        .tripStatus(TripStatus.CREATED)
                        .visibility(TripVisibility.PRIVATE)
                        .build();
        TripDetails tripDetails = TripDetails.builder().build();

        when(embeddedObjectsInitializer.createTripSettings(TripVisibility.PRIVATE))
                .thenReturn(tripSettings);
        when(embeddedObjectsInitializer.createTripDetails()).thenReturn(tripDetails);

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);
        verify(tripRepository).save(tripCaptor.capture());

        Trip savedTrip = tripCaptor.getValue();
        assertThat(savedTrip.getTripPlanId()).isEqualTo(tripPlanId);
    }

    @Test
    void handle_whenEventWithLocationData_shouldCreateTripDetailsFromEvent() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Instant creationTimestamp = Instant.now();
        Instant startTimestamp = Instant.now();
        Instant endTimestamp = startTimestamp.plusSeconds(86400);
        GeoLocation startLocation = new GeoLocation(42.8805, -8.5457);
        GeoLocation endLocation = new GeoLocation(42.8782, -8.5448);
        List<GeoLocation> waypoints = Collections.singletonList(new GeoLocation(42.88, -8.55));

        TripCreatedEvent event =
                TripCreatedEvent.builder()
                        .tripId(tripId)
                        .tripName("Camino de Santiago")
                        .ownerId(ownerId)
                        .visibility("PUBLIC")
                        .startLocation(startLocation)
                        .endLocation(endLocation)
                        .waypoints(waypoints)
                        .startTimestamp(startTimestamp)
                        .endTimestamp(endTimestamp)
                        .creationTimestamp(creationTimestamp)
                        .build();

        TripSettings tripSettings =
                TripSettings.builder()
                        .tripStatus(TripStatus.CREATED)
                        .visibility(TripVisibility.PUBLIC)
                        .build();
        TripDetails tripDetails =
                TripDetails.builder()
                        .startLocation(startLocation)
                        .endLocation(endLocation)
                        .waypoints(waypoints)
                        .startTimestamp(startTimestamp)
                        .endTimestamp(endTimestamp)
                        .build();

        when(embeddedObjectsInitializer.createTripSettings(TripVisibility.PUBLIC))
                .thenReturn(tripSettings);
        when(embeddedObjectsInitializer.createTripDetailsFromEvent(
                        startLocation, endLocation, waypoints, startTimestamp, endTimestamp))
                .thenReturn(tripDetails);

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);
        verify(tripRepository).save(tripCaptor.capture());

        Trip savedTrip = tripCaptor.getValue();
        assertThat(savedTrip.getTripDetails()).isEqualTo(tripDetails);
        verify(embeddedObjectsInitializer)
                .createTripDetailsFromEvent(
                        startLocation, endLocation, waypoints, startTimestamp, endTimestamp);
    }
}
