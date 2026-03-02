package com.tomassirio.wanderer.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.commons.domain.GeoLocation;
import com.tomassirio.wanderer.commons.domain.TripPlan;
import com.tomassirio.wanderer.commons.domain.TripPlanType;
import com.tomassirio.wanderer.commons.dto.TripPlanDTO;
import com.tomassirio.wanderer.query.repository.TripPlanRepository;
import com.tomassirio.wanderer.query.service.impl.TripPlanServiceImpl;
import com.tomassirio.wanderer.query.utils.TestEntityFactory;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TripPlanServiceTest {

    @Mock private TripPlanRepository tripPlanRepository;

    @InjectMocks private TripPlanServiceImpl tripPlanService;

    @Test
    void getTripPlan_whenTripPlanExists_shouldReturnTripPlanDTO() {
        // Given
        UUID planId = UUID.randomUUID();
        TripPlan tripPlan = createTripPlan(planId, "Summer Vacation", TripPlanType.MULTI_DAY);

        when(tripPlanRepository.findById(planId)).thenReturn(Optional.of(tripPlan));

        // When
        TripPlanDTO result = tripPlanService.getTripPlan(planId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(planId.toString());
        assertThat(result.name()).isEqualTo("Summer Vacation");
        assertThat(result.userId()).isEqualTo(TestEntityFactory.USER_ID.toString());
        assertThat(result.planType()).isEqualTo(TripPlanType.MULTI_DAY);
        assertThat(result.startDate()).isNotNull();
        assertThat(result.endDate()).isNotNull();
        assertThat(result.createdTimestamp()).isNotNull();

        verify(tripPlanRepository).findById(planId);
    }

    @Test
    void getTripPlan_whenTripPlanDoesNotExist_shouldThrowEntityNotFoundException() {
        // Given
        UUID nonExistentPlanId = UUID.randomUUID();
        when(tripPlanRepository.findById(nonExistentPlanId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tripPlanService.getTripPlan(nonExistentPlanId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trip plan not found");

        verify(tripPlanRepository).findById(nonExistentPlanId);
    }

    @Test
    void getTripPlan_withLocationsAndWaypoints_shouldMapCorrectly() {
        // Given
        UUID planId = UUID.randomUUID();
        GeoLocation startLocation = new GeoLocation(40.7128, -74.0060);
        GeoLocation endLocation = new GeoLocation(34.0522, -118.2437);
        GeoLocation waypoint = new GeoLocation(41.8781, -87.6298);

        TripPlan tripPlan =
                TripPlan.builder()
                        .id(planId)
                        .userId(TestEntityFactory.USER_ID)
                        .name("Cross Country Trip")
                        .planType(TripPlanType.MULTI_DAY)
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(10))
                        .startLocation(startLocation)
                        .endLocation(endLocation)
                        .waypoints(List.of(waypoint))
                        .createdTimestamp(Instant.now())
                        .build();

        when(tripPlanRepository.findById(planId)).thenReturn(Optional.of(tripPlan));

        // When
        TripPlanDTO result = tripPlanService.getTripPlan(planId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.startLocation()).isNotNull();
        assertThat(result.startLocation().getLat()).isEqualTo(40.7128);
        assertThat(result.startLocation().getLon()).isEqualTo(-74.0060);
        assertThat(result.endLocation()).isNotNull();
        assertThat(result.endLocation().getLat()).isEqualTo(34.0522);
        assertThat(result.waypoints()).hasSize(1);
        assertThat(result.waypoints().get(0).getLat()).isEqualTo(41.8781);

        verify(tripPlanRepository).findById(planId);
    }

    @Test
    void getTripPlan_withSimplePlanType_shouldReturnSimplePlan() {
        // Given
        UUID planId = UUID.randomUUID();
        TripPlan tripPlan = createTripPlan(planId, "Day Trip", TripPlanType.SIMPLE);

        when(tripPlanRepository.findById(planId)).thenReturn(Optional.of(tripPlan));

        // When
        TripPlanDTO result = tripPlanService.getTripPlan(planId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.planType()).isEqualTo(TripPlanType.SIMPLE);

        verify(tripPlanRepository).findById(planId);
    }

    @Test
    void getTripPlansForUser_whenTripPlansExist_shouldReturnListOfTripPlanDTOs() {
        // Given
        UUID userId = TestEntityFactory.USER_ID;
        TripPlan plan1 = createTripPlan(UUID.randomUUID(), "Plan 1", TripPlanType.MULTI_DAY);
        TripPlan plan2 = createTripPlan(UUID.randomUUID(), "Plan 2", TripPlanType.SIMPLE);

        when(tripPlanRepository.findByUserId(userId)).thenReturn(List.of(plan1, plan2));

        // When
        List<TripPlanDTO> result = tripPlanService.getTripPlansForUser(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Plan 1");
        assertThat(result.get(0).planType()).isEqualTo(TripPlanType.MULTI_DAY);
        assertThat(result.get(0).userId()).isEqualTo(userId.toString());
        assertThat(result.get(1).name()).isEqualTo("Plan 2");
        assertThat(result.get(1).planType()).isEqualTo(TripPlanType.SIMPLE);
        assertThat(result.get(1).userId()).isEqualTo(userId.toString());

        verify(tripPlanRepository).findByUserId(userId);
    }

    @Test
    void getTripPlansForUser_whenNoTripPlansExist_shouldReturnEmptyList() {
        // Given
        UUID userId = TestEntityFactory.USER_ID;
        when(tripPlanRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        // When
        List<TripPlanDTO> result = tripPlanService.getTripPlansForUser(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(tripPlanRepository).findByUserId(userId);
    }

    @Test
    void getTripPlansForUser_withMultiplePlans_shouldReturnAllPlans() {
        // Given
        UUID userId = TestEntityFactory.USER_ID;
        TripPlan plan1 = createTripPlan(UUID.randomUUID(), "Plan A", TripPlanType.MULTI_DAY);
        TripPlan plan2 = createTripPlan(UUID.randomUUID(), "Plan B", TripPlanType.SIMPLE);
        TripPlan plan3 = createTripPlan(UUID.randomUUID(), "Plan C", TripPlanType.MULTI_DAY);

        when(tripPlanRepository.findByUserId(userId)).thenReturn(List.of(plan1, plan2, plan3));

        // When
        List<TripPlanDTO> result = tripPlanService.getTripPlansForUser(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).allMatch(dto -> dto.userId().equals(userId.toString()));
        assertThat(result.get(0).name()).isEqualTo("Plan A");
        assertThat(result.get(1).name()).isEqualTo("Plan B");
        assertThat(result.get(2).name()).isEqualTo("Plan C");

        verify(tripPlanRepository).findByUserId(userId);
    }

    @Test
    void getTripPlansForUser_shouldMapAllFieldsCorrectly() {
        // Given
        UUID userId = TestEntityFactory.USER_ID;
        UUID planId = UUID.randomUUID();
        LocalDate startDate = LocalDate.of(2025, 6, 15);
        LocalDate endDate = LocalDate.of(2025, 6, 25);

        TripPlan tripPlan =
                TripPlan.builder()
                        .id(planId)
                        .userId(userId)
                        .name("Detailed Plan")
                        .planType(TripPlanType.MULTI_DAY)
                        .startDate(startDate)
                        .endDate(endDate)
                        .startLocation(TestEntityFactory.createGeoLocation())
                        .endLocation(TestEntityFactory.createGeoLocation())
                        .waypoints(List.of(TestEntityFactory.createGeoLocation()))
                        .createdTimestamp(Instant.now())
                        .build();

        when(tripPlanRepository.findByUserId(userId)).thenReturn(List.of(tripPlan));

        // When
        List<TripPlanDTO> result = tripPlanService.getTripPlansForUser(userId);

        // Then
        assertThat(result).hasSize(1);
        TripPlanDTO dto = result.get(0);
        assertThat(dto.id()).isEqualTo(planId.toString());
        assertThat(dto.userId()).isEqualTo(userId.toString());
        assertThat(dto.name()).isEqualTo("Detailed Plan");
        assertThat(dto.planType()).isEqualTo(TripPlanType.MULTI_DAY);
        assertThat(dto.startDate()).isEqualTo(startDate);
        assertThat(dto.endDate()).isEqualTo(endDate);
        assertThat(dto.startLocation()).isNotNull();
        assertThat(dto.endLocation()).isNotNull();
        assertThat(dto.waypoints()).hasSize(1);
        assertThat(dto.createdTimestamp()).isNotNull();

        verify(tripPlanRepository).findByUserId(userId);
    }

    @Test
    void getTripPlan_withPolylineData_shouldMapPolylineFieldsCorrectly() {
        // Given
        UUID planId = UUID.randomUUID();
        Instant polylineUpdatedAt = Instant.now();
        String encodedPolyline = "a~l~Fjk~uOwHJy@P??fHzR";

        TripPlan tripPlan =
                TripPlan.builder()
                        .id(planId)
                        .userId(TestEntityFactory.USER_ID)
                        .name("Plan With Polyline")
                        .planType(TripPlanType.MULTI_DAY)
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(7))
                        .startLocation(TestEntityFactory.createGeoLocation())
                        .endLocation(TestEntityFactory.createGeoLocation())
                        .waypoints(List.of())
                        .encodedPolyline(encodedPolyline)
                        .polylineUpdatedAt(polylineUpdatedAt)
                        .createdTimestamp(Instant.now())
                        .build();

        when(tripPlanRepository.findById(planId)).thenReturn(Optional.of(tripPlan));

        // When
        TripPlanDTO result = tripPlanService.getTripPlan(planId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.encodedPolyline()).isEqualTo(encodedPolyline);
        assertThat(result.polylineUpdatedAt()).isEqualTo(polylineUpdatedAt);

        verify(tripPlanRepository).findById(planId);
    }

    @Test
    void getTripPlan_withoutPolylineData_shouldReturnNullPolylineFields() {
        // Given
        UUID planId = UUID.randomUUID();

        TripPlan tripPlan =
                TripPlan.builder()
                        .id(planId)
                        .userId(TestEntityFactory.USER_ID)
                        .name("Plan Without Polyline")
                        .planType(TripPlanType.SIMPLE)
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(3))
                        .startLocation(TestEntityFactory.createGeoLocation())
                        .endLocation(TestEntityFactory.createGeoLocation())
                        .waypoints(List.of())
                        .createdTimestamp(Instant.now())
                        .build();

        when(tripPlanRepository.findById(planId)).thenReturn(Optional.of(tripPlan));

        // When
        TripPlanDTO result = tripPlanService.getTripPlan(planId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.encodedPolyline()).isNull();
        assertThat(result.polylineUpdatedAt()).isNull();

        verify(tripPlanRepository).findById(planId);
    }

    private TripPlan createTripPlan(UUID planId, String name, TripPlanType planType) {
        return TripPlan.builder()
                .id(planId)
                .userId(TestEntityFactory.USER_ID)
                .name(name)
                .planType(planType)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))
                .startLocation(TestEntityFactory.createGeoLocation())
                .endLocation(TestEntityFactory.createGeoLocation())
                .waypoints(List.of())
                .createdTimestamp(Instant.now())
                .build();
    }
}
