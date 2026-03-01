package com.tomassirio.wanderer.command.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.maps.model.LatLng;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.repository.TripUpdateRepository;
import com.tomassirio.wanderer.command.service.GoogleRoutesService;
import com.tomassirio.wanderer.commons.domain.GeoLocation;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripUpdate;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolylineServiceImplTest {

    @Mock private TripRepository tripRepository;

    @Mock private TripUpdateRepository tripUpdateRepository;

    @Mock private GoogleRoutesService googleRoutesService;

    @InjectMocks private PolylineServiceImpl polylineService;

    @Test
    void appendSegment_whenTripNotFound_shouldThrowEntityNotFoundException() {
        // Given
        UUID tripId = UUID.randomUUID();
        when(tripRepository.findById(tripId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> polylineService.appendSegment(tripId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trip not found");
    }

    @Test
    void appendSegment_whenFewerThanTwoUpdates_shouldClearPolyline() {
        // Given
        UUID tripId = UUID.randomUUID();
        Trip trip =
                Trip.builder()
                        .id(tripId)
                        .name("Test Trip")
                        .encodedPolyline("existingPolyline")
                        .polylineUpdatedAt(Instant.now())
                        .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripUpdateRepository.findByTripIdOrderByTimestampAsc(tripId)).thenReturn(List.of());

        // When
        polylineService.appendSegment(tripId);

        // Then
        ArgumentCaptor<Trip> captor = ArgumentCaptor.forClass(Trip.class);
        verify(tripRepository).save(captor.capture());

        Trip saved = captor.getValue();
        assertThat(saved.getEncodedPolyline()).isNull();
        assertThat(saved.getPolylineUpdatedAt()).isNull();
    }

    @Test
    void appendSegment_whenOneUpdate_shouldClearPolyline() {
        // Given
        UUID tripId = UUID.randomUUID();
        Trip trip = Trip.builder().id(tripId).name("Test Trip").build();

        TripUpdate update =
                TripUpdate.builder()
                        .id(UUID.randomUUID())
                        .trip(trip)
                        .location(GeoLocation.builder().lat(42.0).lon(-8.0).build())
                        .timestamp(Instant.now())
                        .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripUpdateRepository.findByTripIdOrderByTimestampAsc(tripId))
                .thenReturn(List.of(update));

        // When
        polylineService.appendSegment(tripId);

        // Then
        ArgumentCaptor<Trip> captor = ArgumentCaptor.forClass(Trip.class);
        verify(tripRepository).save(captor.capture());
        assertThat(captor.getValue().getEncodedPolyline()).isNull();
    }

    @Test
    void appendSegment_whenExistingPolyline_shouldAppendNewSegment() {
        // Given
        UUID tripId = UUID.randomUUID();
        Trip trip =
                Trip.builder()
                        .id(tripId)
                        .name("Test Trip")
                        .encodedPolyline("existingEncoded")
                        .polylineUpdatedAt(Instant.now().minusSeconds(3600))
                        .build();

        GeoLocation loc1 = GeoLocation.builder().lat(42.0).lon(-8.0).build();
        GeoLocation loc2 = GeoLocation.builder().lat(42.5).lon(-8.2).build();
        GeoLocation loc3 = GeoLocation.builder().lat(43.0).lon(-8.5).build();

        TripUpdate update1 = createTripUpdate(trip, loc1, Instant.now().minusSeconds(7200));
        TripUpdate update2 = createTripUpdate(trip, loc2, Instant.now().minusSeconds(3600));
        TripUpdate update3 = createTripUpdate(trip, loc3, Instant.now());

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripUpdateRepository.findByTripIdOrderByTimestampAsc(tripId))
                .thenReturn(List.of(update1, update2, update3));

        // Existing polyline decodes to 2 points
        List<LatLng> existingPoints = List.of(new LatLng(42.0, -8.0), new LatLng(42.5, -8.2));
        when(googleRoutesService.decodePolyline("existingEncoded")).thenReturn(existingPoints);

        // New segment from loc2 -> loc3
        List<LatLng> newSegmentPoints =
                List.of(new LatLng(42.5, -8.2), new LatLng(42.7, -8.3), new LatLng(43.0, -8.5));
        when(googleRoutesService.getRoutePoints(loc2, loc3)).thenReturn(newSegmentPoints);

        // Combined encoding
        when(googleRoutesService.encodePolyline(any())).thenReturn("updatedEncoded");

        // When
        polylineService.appendSegment(tripId);

        // Then
        ArgumentCaptor<Trip> captor = ArgumentCaptor.forClass(Trip.class);
        verify(tripRepository).save(captor.capture());

        Trip saved = captor.getValue();
        assertThat(saved.getEncodedPolyline()).isEqualTo("updatedEncoded");
        assertThat(saved.getPolylineUpdatedAt()).isNotNull();

        // Verify the encode was called with existing + new segment (minus duplicate first point)
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<LatLng>> pointsCaptor = ArgumentCaptor.forClass(List.class);
        verify(googleRoutesService).encodePolyline(pointsCaptor.capture());

        List<LatLng> encodedPoints = pointsCaptor.getValue();
        assertThat(encodedPoints).hasSize(4); // 2 existing + 2 new (skip duplicate)
    }

    @Test
    void appendSegment_whenNoExistingPolyline_shouldFullRecompute() {
        // Given
        UUID tripId = UUID.randomUUID();
        Trip trip = Trip.builder().id(tripId).name("Test Trip").build();

        GeoLocation loc1 = GeoLocation.builder().lat(42.0).lon(-8.0).build();
        GeoLocation loc2 = GeoLocation.builder().lat(42.5).lon(-8.2).build();

        TripUpdate update1 = createTripUpdate(trip, loc1, Instant.now().minusSeconds(3600));
        TripUpdate update2 = createTripUpdate(trip, loc2, Instant.now());

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripUpdateRepository.findByTripIdOrderByTimestampAsc(tripId))
                .thenReturn(List.of(update1, update2));

        List<LatLng> fullRoute = List.of(new LatLng(42.0, -8.0), new LatLng(42.5, -8.2));
        when(googleRoutesService.getFullRoutePoints(List.of(loc1, loc2))).thenReturn(fullRoute);
        when(googleRoutesService.encodePolyline(fullRoute)).thenReturn("fullEncoded");

        // When
        polylineService.appendSegment(tripId);

        // Then
        ArgumentCaptor<Trip> captor = ArgumentCaptor.forClass(Trip.class);
        verify(tripRepository).save(captor.capture());

        Trip saved = captor.getValue();
        assertThat(saved.getEncodedPolyline()).isEqualTo("fullEncoded");
        assertThat(saved.getPolylineUpdatedAt()).isNotNull();
    }

    @Test
    void recomputePolyline_whenTripNotFound_shouldThrowEntityNotFoundException() {
        // Given
        UUID tripId = UUID.randomUUID();
        when(tripRepository.findById(tripId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> polylineService.recomputePolyline(tripId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trip not found");
    }

    @Test
    void recomputePolyline_whenFewerThanTwoUpdates_shouldClearPolyline() {
        // Given
        UUID tripId = UUID.randomUUID();
        Trip trip =
                Trip.builder().id(tripId).name("Test Trip").encodedPolyline("oldPolyline").build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripUpdateRepository.findByTripIdOrderByTimestampAsc(tripId)).thenReturn(List.of());

        // When
        polylineService.recomputePolyline(tripId);

        // Then
        ArgumentCaptor<Trip> captor = ArgumentCaptor.forClass(Trip.class);
        verify(tripRepository).save(captor.capture());

        Trip saved = captor.getValue();
        assertThat(saved.getEncodedPolyline()).isNull();
        assertThat(saved.getPolylineUpdatedAt()).isNull();
    }

    @Test
    void recomputePolyline_whenMultipleUpdates_shouldComputeFullPolyline() {
        // Given
        UUID tripId = UUID.randomUUID();
        Trip trip = Trip.builder().id(tripId).name("Test Trip").build();

        GeoLocation loc1 = GeoLocation.builder().lat(42.0).lon(-8.0).build();
        GeoLocation loc2 = GeoLocation.builder().lat(42.5).lon(-8.2).build();
        GeoLocation loc3 = GeoLocation.builder().lat(43.0).lon(-8.5).build();

        TripUpdate update1 = createTripUpdate(trip, loc1, Instant.now().minusSeconds(7200));
        TripUpdate update2 = createTripUpdate(trip, loc2, Instant.now().minusSeconds(3600));
        TripUpdate update3 = createTripUpdate(trip, loc3, Instant.now());

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripUpdateRepository.findByTripIdOrderByTimestampAsc(tripId))
                .thenReturn(List.of(update1, update2, update3));

        List<LatLng> fullRoutePoints =
                List.of(new LatLng(42.0, -8.0), new LatLng(42.5, -8.2), new LatLng(43.0, -8.5));
        when(googleRoutesService.getFullRoutePoints(List.of(loc1, loc2, loc3)))
                .thenReturn(fullRoutePoints);
        when(googleRoutesService.encodePolyline(fullRoutePoints)).thenReturn("recomputedEncoded");

        // When
        polylineService.recomputePolyline(tripId);

        // Then
        ArgumentCaptor<Trip> captor = ArgumentCaptor.forClass(Trip.class);
        verify(tripRepository).save(captor.capture());

        Trip saved = captor.getValue();
        assertThat(saved.getEncodedPolyline()).isEqualTo("recomputedEncoded");
        assertThat(saved.getPolylineUpdatedAt()).isNotNull();
    }

    @Test
    void appendSegment_whenEmptyStringPolyline_shouldFullRecompute() {
        // Given
        UUID tripId = UUID.randomUUID();
        Trip trip = Trip.builder().id(tripId).name("Test Trip").encodedPolyline("").build();

        GeoLocation loc1 = GeoLocation.builder().lat(42.0).lon(-8.0).build();
        GeoLocation loc2 = GeoLocation.builder().lat(42.5).lon(-8.2).build();

        TripUpdate update1 = createTripUpdate(trip, loc1, Instant.now().minusSeconds(3600));
        TripUpdate update2 = createTripUpdate(trip, loc2, Instant.now());

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripUpdateRepository.findByTripIdOrderByTimestampAsc(tripId))
                .thenReturn(List.of(update1, update2));

        List<LatLng> fullRoute = List.of(new LatLng(42.0, -8.0), new LatLng(42.5, -8.2));
        when(googleRoutesService.getFullRoutePoints(List.of(loc1, loc2))).thenReturn(fullRoute);
        when(googleRoutesService.encodePolyline(fullRoute)).thenReturn("fullEncoded");

        // When
        polylineService.appendSegment(tripId);

        // Then
        verify(googleRoutesService).getFullRoutePoints(anyList());
        verify(googleRoutesService, never()).decodePolyline(any());

        ArgumentCaptor<Trip> captor = ArgumentCaptor.forClass(Trip.class);
        verify(tripRepository).save(captor.capture());
        assertThat(captor.getValue().getEncodedPolyline()).isEqualTo("fullEncoded");
    }

    private TripUpdate createTripUpdate(Trip trip, GeoLocation location, Instant timestamp) {
        return TripUpdate.builder()
                .id(UUID.randomUUID())
                .trip(trip)
                .location(location)
                .timestamp(timestamp)
                .build();
    }
}
