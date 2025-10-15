package com.tomassirio.wanderer.command.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.dto.TripPlanCreationRequest;
import com.tomassirio.wanderer.command.dto.TripPlanUpdateRequest;
import com.tomassirio.wanderer.command.repository.TripPlanRepository;
import com.tomassirio.wanderer.command.service.TripPlanMetadataProcessor;
import com.tomassirio.wanderer.command.service.validator.OwnershipValidator;
import com.tomassirio.wanderer.command.service.validator.TripPlanValidator;
import com.tomassirio.wanderer.commons.domain.GeoLocation;
import com.tomassirio.wanderer.commons.domain.TripPlan;
import com.tomassirio.wanderer.commons.domain.TripPlanType;
import com.tomassirio.wanderer.commons.dto.TripPlanDTO;
import com.tomassirio.wanderer.commons.mapper.TripPlanMapper;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class TripPlanServiceImplTest {

    @Mock private TripPlanRepository tripPlanRepository;

    @Mock private TripPlanMetadataProcessor metadataProcessor;

    @Spy private TripPlanMapper tripPlanMapper = TripPlanMapper.INSTANCE;

    @Mock private OwnershipValidator ownershipValidator;

    @Mock private TripPlanValidator tripPlanValidator;

    @InjectMocks private TripPlanServiceImpl tripPlanService;

    @Captor private ArgumentCaptor<TripPlan> tripPlanCaptor;

    private UUID userId;
    private UUID planId;
    private LocalDate startDate;
    private LocalDate endDate;
    private GeoLocation startLocation;
    private GeoLocation endLocation;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        planId = UUID.randomUUID();
        startDate = LocalDate.now().plusDays(1);
        endDate = LocalDate.now().plusDays(7);
        startLocation = GeoLocation.builder().lat(40.7128).lon(-74.0060).build();
        endLocation = GeoLocation.builder().lat(34.0522).lon(-118.2437).build();
    }

    // CREATE TRIP PLAN TESTS

    @Test
    void createTripPlan_whenValidSimpleRequest_shouldCreateAndReturnPlan() {
        // Given
        TripPlanCreationRequest request =
                new TripPlanCreationRequest(
                        "Europe Summer Trip",
                        startDate,
                        endDate,
                        startLocation,
                        endLocation,
                        List.of(),
                        TripPlanType.SIMPLE);

        TripPlan savedPlan =
                TripPlan.builder()
                        .id(planId)
                        .name(request.name())
                        .planType(request.planType())
                        .userId(userId)
                        .createdTimestamp(Instant.now())
                        .startDate(request.startDate())
                        .endDate(request.endDate())
                        .startLocation(request.startLocation())
                        .endLocation(request.endLocation())
                        .metadata(new HashMap<>())
                        .build();

        when(tripPlanRepository.save(any(TripPlan.class))).thenReturn(savedPlan);

        // When
        TripPlanDTO result = tripPlanService.createTripPlan(userId, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(planId.toString());
        assertThat(result.name()).isEqualTo("Europe Summer Trip");
        assertThat(result.planType()).isEqualTo(TripPlanType.SIMPLE);
        assertThat(result.userId()).isEqualTo(userId.toString());
        assertThat(result.startDate()).isEqualTo(startDate);
        assertThat(result.endDate()).isEqualTo(endDate);
        assertThat(result.startLocation()).isEqualTo(startLocation);
        assertThat(result.endLocation()).isEqualTo(endLocation);

        verify(tripPlanRepository).save(tripPlanCaptor.capture());
        TripPlan capturedPlan = tripPlanCaptor.getValue();
        assertThat(capturedPlan.getName()).isEqualTo("Europe Summer Trip");
        assertThat(capturedPlan.getUserId()).isEqualTo(userId);
        assertThat(capturedPlan.getPlanType()).isEqualTo(TripPlanType.SIMPLE);
        assertThat(capturedPlan.getMetadata()).isNotNull();

        verify(metadataProcessor).applyMetadata(any(TripPlan.class), any());
    }

    @Test
    void createTripPlan_whenMultiDayRequest_shouldCreateMultiDayPlan() {
        // Given
        TripPlanCreationRequest request =
                new TripPlanCreationRequest(
                        "Multi-Day Adventure",
                        startDate,
                        endDate,
                        startLocation,
                        endLocation,
                        List.of(),
                        TripPlanType.MULTI_DAY);

        TripPlan savedPlan =
                TripPlan.builder()
                        .id(planId)
                        .name(request.name())
                        .planType(TripPlanType.MULTI_DAY)
                        .userId(userId)
                        .createdTimestamp(Instant.now())
                        .startDate(request.startDate())
                        .endDate(request.endDate())
                        .startLocation(request.startLocation())
                        .endLocation(request.endLocation())
                        .metadata(new HashMap<>())
                        .build();

        when(tripPlanRepository.save(any(TripPlan.class))).thenReturn(savedPlan);

        // When
        TripPlanDTO result = tripPlanService.createTripPlan(userId, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.planType()).isEqualTo(TripPlanType.MULTI_DAY);
        assertThat(result.name()).isEqualTo("Multi-Day Adventure");

        verify(tripPlanRepository).save(tripPlanCaptor.capture());
        assertThat(tripPlanCaptor.getValue().getPlanType()).isEqualTo(TripPlanType.MULTI_DAY);
    }

    @Test
    void createTripPlan_whenInvalidDates_shouldThrowException() {
        // Given
        TripPlanCreationRequest request =
                new TripPlanCreationRequest(
                        "Invalid Plan",
                        endDate,
                        startDate,
                        startLocation,
                        endLocation,
                        List.of(),
                        TripPlanType.SIMPLE);

        doThrow(new IllegalArgumentException("End date must be after start date"))
                .when(tripPlanValidator)
                .validateDates(endDate, startDate);

        // When & Then
        assertThatThrownBy(() -> tripPlanService.createTripPlan(userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("End date must be after start date");

        verify(tripPlanRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void createTripPlan_shouldApplyMetadataCorrectly() {
        // Given
        TripPlanCreationRequest request =
                new TripPlanCreationRequest(
                        "Metadata Test",
                        startDate,
                        endDate,
                        startLocation,
                        endLocation,
                        List.of(),
                        TripPlanType.MULTI_DAY);

        TripPlan savedPlan =
                TripPlan.builder()
                        .id(planId)
                        .name(request.name())
                        .planType(TripPlanType.MULTI_DAY)
                        .userId(userId)
                        .createdTimestamp(Instant.now())
                        .startDate(request.startDate())
                        .endDate(request.endDate())
                        .startLocation(request.startLocation())
                        .endLocation(request.endLocation())
                        .metadata(new HashMap<>())
                        .build();

        when(tripPlanRepository.save(any(TripPlan.class))).thenReturn(savedPlan);

        // When
        TripPlanDTO result = tripPlanService.createTripPlan(userId, request);

        // Then
        verify(tripPlanRepository).save(tripPlanCaptor.capture());
        TripPlan capturedPlan = tripPlanCaptor.getValue();
        assertThat(capturedPlan.getMetadata()).isNotNull();
        assertThat(capturedPlan.getMetadata()).isEmpty();

        verify(metadataProcessor).applyMetadata(any(TripPlan.class), any());
    }

    // UPDATE TRIP PLAN TESTS

    @Test
    void updateTripPlan_whenValidRequest_shouldUpdateAndReturnPlan() {
        // Given
        TripPlanUpdateRequest request =
                new TripPlanUpdateRequest(
                        "Updated Plan Name",
                        startDate.plusDays(1),
                        endDate.plusDays(1),
                        startLocation,
                        endLocation,
                        List.of());

        TripPlan existingPlan =
                TripPlan.builder()
                        .id(planId)
                        .name("Original Name")
                        .planType(TripPlanType.SIMPLE)
                        .userId(userId)
                        .createdTimestamp(Instant.now())
                        .startDate(startDate)
                        .endDate(endDate)
                        .startLocation(startLocation)
                        .endLocation(endLocation)
                        .metadata(new HashMap<>())
                        .build();

        TripPlan updatedPlan =
                TripPlan.builder()
                        .id(planId)
                        .name(request.name())
                        .planType(TripPlanType.SIMPLE)
                        .userId(userId)
                        .createdTimestamp(existingPlan.getCreatedTimestamp())
                        .startDate(request.startDate())
                        .endDate(request.endDate())
                        .startLocation(request.startLocation())
                        .endLocation(request.endLocation())
                        .metadata(new HashMap<>())
                        .build();

        when(tripPlanRepository.findById(planId)).thenReturn(Optional.of(existingPlan));
        when(tripPlanRepository.save(any(TripPlan.class))).thenReturn(updatedPlan);

        // When
        TripPlanDTO result = tripPlanService.updateTripPlan(userId, planId, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(planId.toString());
        assertThat(result.name()).isEqualTo("Updated Plan Name");
        assertThat(result.startDate()).isEqualTo(startDate.plusDays(1));
        assertThat(result.endDate()).isEqualTo(endDate.plusDays(1));

        verify(ownershipValidator)
                .validateOwnership(eq(existingPlan), eq(userId), any(), any(), eq("trip plan"));
        verify(metadataProcessor).applyMetadata(any(TripPlan.class), any());
        verify(tripPlanRepository).save(existingPlan);
    }

    @Test
    void updateTripPlan_whenPlanNotFound_shouldThrowEntityNotFoundException() {
        // Given
        TripPlanUpdateRequest request =
                new TripPlanUpdateRequest(
                        "Updated Plan", startDate, endDate, startLocation, endLocation, List.of());

        when(tripPlanRepository.findById(planId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tripPlanService.updateTripPlan(userId, planId, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trip plan not found");

        verify(tripPlanRepository).findById(planId);
        verify(ownershipValidator, org.mockito.Mockito.never())
                .validateOwnership(any(), any(), any(), any(), any());
        verify(tripPlanRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void updateTripPlan_whenUserDoesNotOwnPlan_shouldThrowAccessDeniedException() {
        // Given
        UUID differentUserId = UUID.randomUUID();
        TripPlanUpdateRequest request =
                new TripPlanUpdateRequest(
                        "Updated Plan", startDate, endDate, startLocation, endLocation, List.of());

        TripPlan existingPlan =
                TripPlan.builder()
                        .id(planId)
                        .name("Original Name")
                        .planType(TripPlanType.SIMPLE)
                        .userId(differentUserId)
                        .createdTimestamp(Instant.now())
                        .startDate(startDate)
                        .endDate(endDate)
                        .startLocation(startLocation)
                        .endLocation(endLocation)
                        .metadata(new HashMap<>())
                        .build();

        when(tripPlanRepository.findById(planId)).thenReturn(Optional.of(existingPlan));
        doThrow(new AccessDeniedException("User does not have permission to modify trip plan"))
                .when(ownershipValidator)
                .validateOwnership(any(), any(), any(), any(), any());

        // When & Then
        assertThatThrownBy(() -> tripPlanService.updateTripPlan(userId, planId, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("User does not have permission");

        verify(tripPlanRepository).findById(planId);
        verify(ownershipValidator)
                .validateOwnership(any(), eq(userId), any(), any(), eq("trip plan"));
        verify(tripPlanRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void updateTripPlan_shouldUpdateAllFields() {
        // Given
        GeoLocation newStartLocation = GeoLocation.builder().lat(51.5074).lon(-0.1278).build();
        GeoLocation newEndLocation = GeoLocation.builder().lat(48.8566).lon(2.3522).build();
        LocalDate newStartDate = startDate.plusDays(5);
        LocalDate newEndDate = endDate.plusDays(5);

        TripPlanUpdateRequest request =
                new TripPlanUpdateRequest(
                        "Completely Updated Plan",
                        newStartDate,
                        newEndDate,
                        newStartLocation,
                        newEndLocation,
                        List.of());

        TripPlan existingPlan =
                TripPlan.builder()
                        .id(planId)
                        .name("Original Name")
                        .planType(TripPlanType.SIMPLE)
                        .userId(userId)
                        .createdTimestamp(Instant.now())
                        .startDate(startDate)
                        .endDate(endDate)
                        .startLocation(startLocation)
                        .endLocation(endLocation)
                        .metadata(new HashMap<>())
                        .build();

        when(tripPlanRepository.findById(planId)).thenReturn(Optional.of(existingPlan));
        when(tripPlanRepository.save(any(TripPlan.class))).thenReturn(existingPlan);

        // When
        tripPlanService.updateTripPlan(userId, planId, request);

        // Then
        assertThat(existingPlan.getName()).isEqualTo("Completely Updated Plan");
        assertThat(existingPlan.getStartDate()).isEqualTo(newStartDate);
        assertThat(existingPlan.getEndDate()).isEqualTo(newEndDate);
        assertThat(existingPlan.getStartLocation()).isEqualTo(newStartLocation);
        assertThat(existingPlan.getEndLocation()).isEqualTo(newEndLocation);

        verify(tripPlanRepository).save(existingPlan);
    }

    @Test
    void updateTripPlan_shouldReapplyMetadata() {
        // Given
        TripPlanUpdateRequest request =
                new TripPlanUpdateRequest(
                        "Updated Plan", startDate, endDate, startLocation, endLocation, List.of());

        TripPlan existingPlan =
                TripPlan.builder()
                        .id(planId)
                        .name("Original Name")
                        .planType(TripPlanType.MULTI_DAY)
                        .userId(userId)
                        .createdTimestamp(Instant.now())
                        .startDate(startDate)
                        .endDate(endDate)
                        .startLocation(startLocation)
                        .endLocation(endLocation)
                        .metadata(new HashMap<>())
                        .build();

        when(tripPlanRepository.findById(planId)).thenReturn(Optional.of(existingPlan));
        when(tripPlanRepository.save(any(TripPlan.class))).thenReturn(existingPlan);

        // When
        tripPlanService.updateTripPlan(userId, planId, request);

        // Then
        verify(metadataProcessor).applyMetadata(existingPlan, existingPlan.getMetadata());
    }

    // DELETE TRIP PLAN TESTS

    @Test
    void deleteTripPlan_whenPlanExists_shouldDeletePlan() {
        // Given
        TripPlan existingPlan =
                TripPlan.builder()
                        .id(planId)
                        .name("Plan to Delete")
                        .planType(TripPlanType.SIMPLE)
                        .userId(userId)
                        .createdTimestamp(Instant.now())
                        .startDate(startDate)
                        .endDate(endDate)
                        .startLocation(startLocation)
                        .endLocation(endLocation)
                        .metadata(new HashMap<>())
                        .build();

        when(tripPlanRepository.findById(planId)).thenReturn(Optional.of(existingPlan));

        // When
        tripPlanService.deleteTripPlan(userId, planId);

        // Then
        verify(tripPlanRepository).findById(planId);
        verify(ownershipValidator)
                .validateOwnership(eq(existingPlan), eq(userId), any(), any(), eq("trip plan"));
        verify(tripPlanRepository).deleteById(planId);
    }

    @Test
    void deleteTripPlan_whenPlanNotFound_shouldThrowEntityNotFoundException() {
        // Given
        when(tripPlanRepository.findById(planId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tripPlanService.deleteTripPlan(userId, planId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trip plan not found");

        verify(tripPlanRepository).findById(planId);
        verify(ownershipValidator, org.mockito.Mockito.never())
                .validateOwnership(any(), any(), any(), any(), any());
        verify(tripPlanRepository, org.mockito.Mockito.never()).deleteById(any());
    }

    @Test
    void deleteTripPlan_whenUserDoesNotOwnPlan_shouldThrowAccessDeniedException() {
        // Given
        UUID differentUserId = UUID.randomUUID();
        TripPlan existingPlan =
                TripPlan.builder()
                        .id(planId)
                        .name("Plan to Delete")
                        .planType(TripPlanType.SIMPLE)
                        .userId(differentUserId)
                        .createdTimestamp(Instant.now())
                        .startDate(startDate)
                        .endDate(endDate)
                        .startLocation(startLocation)
                        .endLocation(endLocation)
                        .metadata(new HashMap<>())
                        .build();

        when(tripPlanRepository.findById(planId)).thenReturn(Optional.of(existingPlan));
        doThrow(new AccessDeniedException("User does not have permission to delete trip plan"))
                .when(ownershipValidator)
                .validateOwnership(any(), any(), any(), any(), any());

        // When & Then
        assertThatThrownBy(() -> tripPlanService.deleteTripPlan(userId, planId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("User does not have permission");

        verify(tripPlanRepository).findById(planId);
        verify(ownershipValidator)
                .validateOwnership(any(), eq(userId), any(), any(), eq("trip plan"));
        verify(tripPlanRepository, org.mockito.Mockito.never()).deleteById(any());
    }
}
