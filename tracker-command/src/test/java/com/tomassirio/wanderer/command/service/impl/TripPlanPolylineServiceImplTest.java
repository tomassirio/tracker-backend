package com.tomassirio.wanderer.command.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.maps.model.LatLng;
import com.tomassirio.wanderer.command.repository.TripPlanRepository;
import com.tomassirio.wanderer.command.service.RouteService;
import com.tomassirio.wanderer.command.service.helper.PolylineComputer;
import com.tomassirio.wanderer.commons.domain.GeoLocation;
import com.tomassirio.wanderer.commons.domain.TripPlan;
import com.tomassirio.wanderer.commons.domain.TripPlanType;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TripPlanPolylineServiceImplTest {

    @Mock private TripPlanRepository tripPlanRepository;

    @Mock private RouteService routeService;

    private TripPlanPolylineServiceImpl service;

    @BeforeEach
    void setUp() {
        PolylineComputer polylineComputer = new PolylineComputer(routeService);
        service = new TripPlanPolylineServiceImpl(tripPlanRepository, polylineComputer);
    }

    @Test
    void computePolyline_whenPlanHasStartWaypointsEnd_shouldComputeAndSave() {
        // Given
        UUID planId = UUID.randomUUID();
        GeoLocation start = GeoLocation.builder().lat(42.88).lon(-8.54).build();
        GeoLocation waypoint = GeoLocation.builder().lat(42.50).lon(-8.10).build();
        GeoLocation end = GeoLocation.builder().lat(42.10).lon(-7.70).build();

        TripPlan tripPlan = buildTripPlan(planId, start, end, List.of(waypoint));
        when(tripPlanRepository.findById(planId)).thenReturn(Optional.of(tripPlan));

        List<LatLng> routePoints =
                List.of(
                        new LatLng(42.88, -8.54),
                        new LatLng(42.50, -8.10),
                        new LatLng(42.10, -7.70));
        when(routeService.getFullRoutePoints(anyList())).thenReturn(routePoints);

        // When
        service.computePolyline(planId);

        // Then
        ArgumentCaptor<TripPlan> captor = ArgumentCaptor.forClass(TripPlan.class);
        verify(tripPlanRepository).save(captor.capture());

        TripPlan saved = captor.getValue();
        assertThat(saved.getEncodedPolyline()).isNotNull().isNotEmpty();
        assertThat(saved.getPolylineUpdatedAt()).isNotNull();
    }

    @Test
    void computePolyline_whenPlanHasOnlyStartAndEnd_shouldComputePolyline() {
        // Given
        UUID planId = UUID.randomUUID();
        GeoLocation start = GeoLocation.builder().lat(42.88).lon(-8.54).build();
        GeoLocation end = GeoLocation.builder().lat(42.10).lon(-7.70).build();

        TripPlan tripPlan = buildTripPlan(planId, start, end, List.of());
        when(tripPlanRepository.findById(planId)).thenReturn(Optional.of(tripPlan));

        List<LatLng> routePoints = List.of(new LatLng(42.88, -8.54), new LatLng(42.10, -7.70));
        when(routeService.getFullRoutePoints(anyList())).thenReturn(routePoints);

        // When
        service.computePolyline(planId);

        // Then
        ArgumentCaptor<TripPlan> captor = ArgumentCaptor.forClass(TripPlan.class);
        verify(tripPlanRepository).save(captor.capture());

        TripPlan saved = captor.getValue();
        assertThat(saved.getEncodedPolyline()).isNotNull();
        assertThat(saved.getPolylineUpdatedAt()).isNotNull();
    }

    @Test
    void computePolyline_whenPlanHasOnlyStartLocation_shouldClearPolyline() {
        // Given
        UUID planId = UUID.randomUUID();
        GeoLocation start = GeoLocation.builder().lat(42.88).lon(-8.54).build();

        TripPlan tripPlan =
                TripPlan.builder()
                        .id(planId)
                        .name("Test")
                        .planType(TripPlanType.SIMPLE)
                        .userId(UUID.randomUUID())
                        .createdTimestamp(Instant.now())
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(7))
                        .startLocation(start)
                        .endLocation(null)
                        .waypoints(List.of())
                        .build();

        when(tripPlanRepository.findById(planId)).thenReturn(Optional.of(tripPlan));

        // When
        service.computePolyline(planId);

        // Then
        ArgumentCaptor<TripPlan> captor = ArgumentCaptor.forClass(TripPlan.class);
        verify(tripPlanRepository).save(captor.capture());

        TripPlan saved = captor.getValue();
        assertThat(saved.getEncodedPolyline()).isNull();
        assertThat(saved.getPolylineUpdatedAt()).isNull();
        verify(routeService, never()).getFullRoutePoints(any());
    }

    @Test
    void computePolyline_whenPlanNotFound_shouldThrowEntityNotFoundException() {
        // Given
        UUID planId = UUID.randomUUID();
        when(tripPlanRepository.findById(planId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.computePolyline(planId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(planId.toString());
    }

    private TripPlan buildTripPlan(
            UUID planId, GeoLocation start, GeoLocation end, List<GeoLocation> waypoints) {
        return TripPlan.builder()
                .id(planId)
                .name("Camino Plan")
                .planType(TripPlanType.MULTI_DAY)
                .userId(UUID.randomUUID())
                .createdTimestamp(Instant.now())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .startLocation(start)
                .endLocation(end)
                .waypoints(waypoints)
                .build();
    }
}
