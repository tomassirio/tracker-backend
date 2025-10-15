package com.tomassirio.wanderer.command.service.impl;

import static com.tomassirio.wanderer.commons.utils.BaseTestEntityFactory.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.dto.TripCreationRequest;
import com.tomassirio.wanderer.command.dto.TripUpdateRequest;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.repository.UserRepository;
import com.tomassirio.wanderer.command.service.helper.TripEmbeddedObjectsInitializer;
import com.tomassirio.wanderer.command.service.helper.TripStatusTransitionHandler;
import com.tomassirio.wanderer.command.service.validator.OwnershipValidator;
import com.tomassirio.wanderer.command.utils.TestEntityFactory;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripDetails;
import com.tomassirio.wanderer.commons.domain.TripSettings;
import com.tomassirio.wanderer.commons.domain.TripStatus;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import com.tomassirio.wanderer.commons.domain.User;
import com.tomassirio.wanderer.commons.dto.TripDTO;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class TripServiceImplTest {

    @Mock private TripRepository tripRepository;

    @Mock private UserRepository userRepository;

    @Mock private TripEmbeddedObjectsInitializer embeddedObjectsInitializer;

    @Mock private TripStatusTransitionHandler statusTransitionHandler;

    @Mock private OwnershipValidator ownershipValidator;

    @InjectMocks private TripServiceImpl tripService;

    @BeforeEach
    void setUp() {
        lenient()
                .when(userRepository.findById(any(UUID.class)))
                .thenAnswer(
                        invocation -> {
                            UUID id = invocation.getArgument(0);
                            return Optional.of(User.builder().id(id).username("test-user").build());
                        });

        // Configure the embedded objects initializer to create default objects
        lenient()
                .when(embeddedObjectsInitializer.createTripSettings(any(TripVisibility.class)))
                .thenAnswer(
                        invocation -> {
                            TripVisibility visibility = invocation.getArgument(0);
                            return TripSettings.builder()
                                    .tripStatus(TripStatus.CREATED)
                                    .visibility(visibility)
                                    .updateRefresh(null)
                                    .build();
                        });

        lenient()
                .when(embeddedObjectsInitializer.createTripDetails())
                .thenReturn(
                        TripDetails.builder()
                                .startTimestamp(null)
                                .endTimestamp(null)
                                .startLocation(null)
                                .endLocation(null)
                                .build());

        // Configure ensureTripSettings to do nothing (objects already exist in tests)
        lenient()
                .when(
                        embeddedObjectsInitializer.ensureTripSettingsAndGetPreviousStatus(
                                any(Trip.class), any(TripStatus.class)))
                .thenAnswer(
                        invocation -> {
                            Trip trip = invocation.getArgument(0);
                            if (trip.getTripSettings() != null) {
                                return trip.getTripSettings().getTripStatus();
                            }
                            return TripStatus.CREATED;
                        });

        // Configure the status transition handler to actually update timestamps
        lenient()
                .doAnswer(
                        invocation -> {
                            Trip trip = invocation.getArgument(0);
                            TripStatus previousStatus = invocation.getArgument(1);
                            TripStatus newStatus = invocation.getArgument(2);

                            // Replicate the actual logic from TripStatusTransitionHandler
                            if (newStatus == TripStatus.IN_PROGRESS
                                    && previousStatus == TripStatus.CREATED) {
                                trip.getTripDetails().setStartTimestamp(Instant.now());
                            } else if (newStatus == TripStatus.FINISHED) {
                                trip.getTripDetails().setEndTimestamp(Instant.now());
                            }
                            return null;
                        })
                .when(statusTransitionHandler)
                .handleStatusTransition(
                        any(Trip.class), any(TripStatus.class), any(TripStatus.class));

        // Configure the ownership validator to throw exception for non-owners
        lenient()
                .doAnswer(
                        invocation -> {
                            Object entity = invocation.getArgument(0);
                            UUID userId = invocation.getArgument(1);
                            Function<Object, UUID> userIdExtractor = invocation.getArgument(2);
                            Function<Object, UUID> entityIdExtractor = invocation.getArgument(3);
                            String entityType = invocation.getArgument(4);

                            UUID ownerId = userIdExtractor.apply(entity);
                            if (!ownerId.equals(userId)) {
                                UUID entityId = entityIdExtractor.apply(entity);
                                throw new AccessDeniedException(
                                        "User "
                                                + userId
                                                + " does not have permission to modify "
                                                + entityType
                                                + " "
                                                + entityId);
                            }
                            return null;
                        })
                .when(ownershipValidator)
                .validateOwnership(
                        any(),
                        any(UUID.class),
                        any(Function.class),
                        any(Function.class),
                        any(String.class));
    }

    @Test
    void createTrip_whenValidRequest_shouldCreateAndSaveTrip() {
        // Given
        TripCreationRequest request =
                TestEntityFactory.createTripCreationRequest(
                        "Summer Road Trip", TripVisibility.PUBLIC);

        when(tripRepository.save(any(Trip.class)))
                .thenAnswer(
                        invocation -> {
                            Trip trip = invocation.getArgument(0);
                            trip.setId(UUID.randomUUID());
                            return trip;
                        });

        // When
        TripDTO createdTrip = tripService.createTrip(USER_ID, request);

        // Then
        assertThat(createdTrip).isNotNull();
        assertThat(createdTrip.id()).isNotNull();
        assertThat(createdTrip.name()).isEqualTo("Summer Road Trip");
        assertThat(createdTrip.userId()).isNotNull();
        assertThat(createdTrip.tripSettings().tripStatus()).isEqualTo(TripStatus.CREATED);
        assertThat(createdTrip.tripSettings().visibility()).isEqualTo(TripVisibility.PUBLIC);
        assertThat(createdTrip.enabled()).isTrue();
        assertThat(createdTrip.creationTimestamp()).isNotNull();

        verify(tripRepository).save(any(Trip.class));
        verify(userRepository).findById(USER_ID);
    }

    @Test
    void createTrip_whenPrivateVisibility_shouldCreatePrivateTrip() {
        // Given
        TripCreationRequest request =
                TestEntityFactory.createTripCreationRequest("Private Trip", TripVisibility.PRIVATE);

        when(tripRepository.save(any(Trip.class)))
                .thenAnswer(
                        invocation -> {
                            Trip trip = invocation.getArgument(0);
                            trip.setId(UUID.randomUUID());
                            return trip;
                        });

        // When
        TripDTO createdTrip = tripService.createTrip(USER_ID, request);

        // Then
        assertThat(createdTrip).isNotNull();
        assertThat(createdTrip.name()).isEqualTo("Private Trip");
        assertThat(createdTrip.tripSettings().visibility()).isEqualTo(TripVisibility.PRIVATE);
        assertThat(createdTrip.tripSettings().tripStatus()).isEqualTo(TripStatus.CREATED);

        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void createTrip_shouldInitializeWithNullTimestampsAndLocations() {
        // Given
        TripCreationRequest request =
                TestEntityFactory.createTripCreationRequest("New Trip", TripVisibility.PUBLIC);

        when(tripRepository.save(any(Trip.class)))
                .thenAnswer(
                        invocation -> {
                            Trip trip = invocation.getArgument(0);
                            trip.setId(UUID.randomUUID());
                            return trip;
                        });

        // When
        TripDTO createdTrip = tripService.createTrip(USER_ID, request);

        // Then
        assertThat(createdTrip).isNotNull();
        assertThat(createdTrip.tripDetails().startTimestamp()).isNull();
        assertThat(createdTrip.tripDetails().endTimestamp()).isNull();
        assertThat(createdTrip.tripPlanId()).isNull();
        assertThat(createdTrip.tripSettings().updateRefresh()).isNull();

        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void createTrip_whenUserNotFound_shouldThrowException() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();
        TripCreationRequest request =
                TestEntityFactory.createTripCreationRequest("Test Trip", TripVisibility.PUBLIC);

        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tripService.createTrip(nonExistentUserId, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findById(nonExistentUserId);
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void updateTrip_whenTripExists_shouldUpdateAndSaveTrip() {
        // Given
        UUID tripId = UUID.randomUUID();

        TripSettings existingSettings =
                TripSettings.builder()
                        .tripStatus(TripStatus.CREATED)
                        .visibility(TripVisibility.PRIVATE)
                        .updateRefresh(null)
                        .build();

        TripDetails existingDetails =
                TripDetails.builder()
                        .startTimestamp(null)
                        .endTimestamp(null)
                        .startLocation(null)
                        .endLocation(null)
                        .build();

        Trip existingTrip =
                Trip.builder()
                        .id(tripId)
                        .name("Old Trip Name")
                        .userId(USER_ID)
                        .tripSettings(existingSettings)
                        .tripDetails(existingDetails)
                        .creationTimestamp(Instant.now().minusSeconds(3600))
                        .enabled(true)
                        .build();

        TripUpdateRequest request =
                TestEntityFactory.createTripUpdateRequest(
                        "Updated Trip Name", TripVisibility.PUBLIC);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(existingTrip));
        when(tripRepository.save(any(Trip.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TripDTO updatedTrip = tripService.updateTrip(USER_ID, tripId, request);

        // Then
        assertThat(updatedTrip).isNotNull();
        assertThat(updatedTrip.id()).isNotNull();
        assertThat(updatedTrip.name()).isEqualTo("Updated Trip Name");
        assertThat(updatedTrip.tripSettings().visibility()).isEqualTo(TripVisibility.PUBLIC);

        verify(tripRepository).findById(tripId);
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void updateTrip_whenTripDoesNotExist_shouldThrowException() {
        // Given
        UUID nonExistentTripId = UUID.randomUUID();
        TripUpdateRequest request =
                TestEntityFactory.createTripUpdateRequest("Updated Trip", TripVisibility.PUBLIC);

        when(tripRepository.findById(nonExistentTripId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tripService.updateTrip(USER_ID, nonExistentTripId, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trip not found");

        verify(tripRepository).findById(nonExistentTripId);
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void updateTrip_whenChangingVisibility_shouldUpdateVisibility() {
        // Given
        UUID tripId = UUID.randomUUID();

        TripSettings existingSettings =
                TripSettings.builder()
                        .tripStatus(TripStatus.CREATED)
                        .visibility(TripVisibility.PRIVATE)
                        .updateRefresh(null)
                        .build();

        TripDetails existingDetails =
                TripDetails.builder()
                        .startTimestamp(null)
                        .endTimestamp(null)
                        .startLocation(null)
                        .endLocation(null)
                        .build();

        Trip existingTrip =
                Trip.builder()
                        .id(tripId)
                        .name("Trip Name")
                        .userId(USER_ID)
                        .tripSettings(existingSettings)
                        .tripDetails(existingDetails)
                        .creationTimestamp(Instant.now())
                        .enabled(true)
                        .build();

        TripUpdateRequest request =
                TestEntityFactory.createTripUpdateRequest("Trip Name", TripVisibility.PUBLIC);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(existingTrip));
        when(tripRepository.save(any(Trip.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TripDTO updatedTrip = tripService.updateTrip(USER_ID, tripId, request);

        // Then
        assertThat(updatedTrip.tripSettings().visibility()).isEqualTo(TripVisibility.PUBLIC);
        assertThat(updatedTrip.name()).isEqualTo("Trip Name");

        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void updateTrip_whenUserDoesNotOwnTrip_shouldThrowAccessDeniedException() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        Trip existingTrip =
                Trip.builder()
                        .id(tripId)
                        .name("Trip Name")
                        .userId(USER_ID)
                        .tripSettings(
                                TripSettings.builder()
                                        .tripStatus(TripStatus.CREATED)
                                        .visibility(TripVisibility.PUBLIC)
                                        .build())
                        .tripDetails(TripDetails.builder().build())
                        .creationTimestamp(Instant.now())
                        .enabled(true)
                        .build();

        TripUpdateRequest request =
                TestEntityFactory.createTripUpdateRequest("Updated Trip", TripVisibility.PUBLIC);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(existingTrip));

        // When & Then
        assertThatThrownBy(() -> tripService.updateTrip(otherUserId, tripId, request))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                .hasMessageContaining("does not have permission");

        verify(tripRepository).findById(tripId);
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void deleteTrip_whenTripExists_shouldCallRepositoryDelete() {
        // Given
        UUID tripId = UUID.randomUUID();

        Trip existingTrip =
                Trip.builder()
                        .id(tripId)
                        .name("Trip Name")
                        .userId(USER_ID)
                        .tripSettings(
                                TripSettings.builder()
                                        .tripStatus(TripStatus.CREATED)
                                        .visibility(TripVisibility.PUBLIC)
                                        .build())
                        .tripDetails(TripDetails.builder().build())
                        .creationTimestamp(Instant.now())
                        .enabled(true)
                        .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(existingTrip));

        // When
        tripService.deleteTrip(USER_ID, tripId);

        // Then
        verify(tripRepository).findById(tripId);
        verify(tripRepository).deleteById(tripId);
    }

    @Test
    void deleteTrip_whenUserDoesNotOwnTrip_shouldThrowAccessDeniedException() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        Trip existingTrip =
                Trip.builder()
                        .id(tripId)
                        .name("Trip Name")
                        .userId(USER_ID)
                        .tripSettings(
                                TripSettings.builder()
                                        .tripStatus(TripStatus.CREATED)
                                        .visibility(TripVisibility.PUBLIC)
                                        .build())
                        .tripDetails(TripDetails.builder().build())
                        .creationTimestamp(Instant.now())
                        .enabled(true)
                        .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(existingTrip));

        // When & Then
        assertThatThrownBy(() -> tripService.deleteTrip(otherUserId, tripId))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                .hasMessageContaining("does not have permission");

        verify(tripRepository).findById(tripId);
        verify(tripRepository, never()).deleteById(tripId);
    }

    @Test
    void changeVisibility_whenUserOwnsTrip_shouldChangeVisibility() {
        // Given
        UUID tripId = UUID.randomUUID();

        TripSettings existingSettings =
                TripSettings.builder()
                        .tripStatus(TripStatus.CREATED)
                        .visibility(TripVisibility.PUBLIC)
                        .updateRefresh(null)
                        .build();

        Trip existingTrip =
                Trip.builder()
                        .id(tripId)
                        .name("My Trip")
                        .userId(USER_ID)
                        .tripSettings(existingSettings)
                        .tripDetails(TripDetails.builder().build())
                        .creationTimestamp(Instant.now())
                        .enabled(true)
                        .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(existingTrip));
        when(tripRepository.save(any(Trip.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TripDTO updatedTrip = tripService.changeVisibility(USER_ID, tripId, TripVisibility.PRIVATE);

        // Then
        assertThat(updatedTrip.tripSettings().visibility()).isEqualTo(TripVisibility.PRIVATE);
        verify(tripRepository).findById(tripId);
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void changeVisibility_whenUserDoesNotOwnTrip_shouldThrowAccessDeniedException() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        Trip existingTrip =
                Trip.builder()
                        .id(tripId)
                        .name("Trip Name")
                        .userId(USER_ID)
                        .tripSettings(
                                TripSettings.builder()
                                        .tripStatus(TripStatus.CREATED)
                                        .visibility(TripVisibility.PUBLIC)
                                        .build())
                        .tripDetails(TripDetails.builder().build())
                        .creationTimestamp(Instant.now())
                        .enabled(true)
                        .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(existingTrip));

        // When & Then
        assertThatThrownBy(
                        () ->
                                tripService.changeVisibility(
                                        otherUserId, tripId, TripVisibility.PRIVATE))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                .hasMessageContaining("does not have permission");

        verify(tripRepository).findById(tripId);
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void changeStatus_whenUserOwnsTrip_shouldChangeStatus() {
        // Given
        UUID tripId = UUID.randomUUID();

        TripSettings existingSettings =
                TripSettings.builder()
                        .tripStatus(TripStatus.CREATED)
                        .visibility(TripVisibility.PUBLIC)
                        .updateRefresh(null)
                        .build();

        TripDetails existingDetails =
                TripDetails.builder()
                        .startTimestamp(null)
                        .endTimestamp(null)
                        .startLocation(null)
                        .endLocation(null)
                        .build();

        Trip existingTrip =
                Trip.builder()
                        .id(tripId)
                        .name("My Trip")
                        .userId(USER_ID)
                        .tripSettings(existingSettings)
                        .tripDetails(existingDetails)
                        .creationTimestamp(Instant.now())
                        .enabled(true)
                        .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(existingTrip));
        when(tripRepository.save(any(Trip.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TripDTO updatedTrip = tripService.changeStatus(USER_ID, tripId, TripStatus.IN_PROGRESS);

        // Then
        assertThat(updatedTrip.tripSettings().tripStatus()).isEqualTo(TripStatus.IN_PROGRESS);
        assertThat(updatedTrip.tripDetails().startTimestamp())
                .isNotNull(); // Should set start timestamp
        verify(tripRepository).findById(tripId);
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void changeStatus_whenUserDoesNotOwnTrip_shouldThrowAccessDeniedException() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        Trip existingTrip =
                Trip.builder()
                        .id(tripId)
                        .name("Trip Name")
                        .userId(USER_ID)
                        .tripSettings(
                                TripSettings.builder()
                                        .tripStatus(TripStatus.CREATED)
                                        .visibility(TripVisibility.PUBLIC)
                                        .build())
                        .tripDetails(TripDetails.builder().build())
                        .creationTimestamp(Instant.now())
                        .enabled(true)
                        .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(existingTrip));

        // When & Then
        assertThatThrownBy(
                        () -> tripService.changeStatus(otherUserId, tripId, TripStatus.IN_PROGRESS))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                .hasMessageContaining("does not have permission");

        verify(tripRepository).findById(tripId);
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void changeStatus_toFinished_shouldSetEndTimestamp() {
        // Given
        UUID tripId = UUID.randomUUID();
        Instant startTime = Instant.now().minusSeconds(3600);

        TripSettings existingSettings =
                TripSettings.builder()
                        .tripStatus(TripStatus.IN_PROGRESS)
                        .visibility(TripVisibility.PUBLIC)
                        .updateRefresh(null)
                        .build();

        TripDetails existingDetails =
                TripDetails.builder()
                        .startTimestamp(startTime)
                        .endTimestamp(null)
                        .startLocation(null)
                        .endLocation(null)
                        .build();

        Trip existingTrip =
                Trip.builder()
                        .id(tripId)
                        .name("My Trip")
                        .userId(USER_ID)
                        .tripSettings(existingSettings)
                        .tripDetails(existingDetails)
                        .creationTimestamp(Instant.now().minusSeconds(7200))
                        .enabled(true)
                        .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(existingTrip));
        when(tripRepository.save(any(Trip.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TripDTO updatedTrip = tripService.changeStatus(USER_ID, tripId, TripStatus.FINISHED);

        // Then
        assertThat(updatedTrip.tripSettings().tripStatus()).isEqualTo(TripStatus.FINISHED);
        assertThat(updatedTrip.tripDetails().startTimestamp()).isEqualTo(startTime);
        assertThat(updatedTrip.tripDetails().endTimestamp())
                .isNotNull(); // Should set end timestamp
        verify(tripRepository).findById(tripId);
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void changeStatus_fromCreatedToInProgress_shouldSetStartTimestamp() {
        // Given
        UUID tripId = UUID.randomUUID();

        TripSettings existingSettings =
                TripSettings.builder()
                        .tripStatus(TripStatus.CREATED)
                        .visibility(TripVisibility.PUBLIC)
                        .updateRefresh(null)
                        .build();

        TripDetails existingDetails =
                TripDetails.builder()
                        .startTimestamp(null)
                        .endTimestamp(null)
                        .startLocation(null)
                        .endLocation(null)
                        .build();

        Trip existingTrip =
                Trip.builder()
                        .id(tripId)
                        .name("My Trip")
                        .userId(USER_ID)
                        .tripSettings(existingSettings)
                        .tripDetails(existingDetails)
                        .creationTimestamp(Instant.now())
                        .enabled(true)
                        .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(existingTrip));
        when(tripRepository.save(any(Trip.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TripDTO updatedTrip = tripService.changeStatus(USER_ID, tripId, TripStatus.IN_PROGRESS);

        // Then
        assertThat(updatedTrip.tripSettings().tripStatus()).isEqualTo(TripStatus.IN_PROGRESS);
        assertThat(updatedTrip.tripDetails().startTimestamp()).isNotNull();
        assertThat(updatedTrip.tripDetails().endTimestamp()).isNull();
        verify(tripRepository).findById(tripId);
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void changeStatus_toPaused_shouldNotChangeTimestamps() {
        // Given
        UUID tripId = UUID.randomUUID();
        Instant startTime = Instant.now().minusSeconds(3600);

        TripSettings existingSettings =
                TripSettings.builder()
                        .tripStatus(TripStatus.IN_PROGRESS)
                        .visibility(TripVisibility.PUBLIC)
                        .updateRefresh(null)
                        .build();

        TripDetails existingDetails =
                TripDetails.builder()
                        .startTimestamp(startTime)
                        .endTimestamp(null)
                        .startLocation(null)
                        .endLocation(null)
                        .build();

        Trip existingTrip =
                Trip.builder()
                        .id(tripId)
                        .name("My Trip")
                        .userId(USER_ID)
                        .tripSettings(existingSettings)
                        .tripDetails(existingDetails)
                        .creationTimestamp(Instant.now().minusSeconds(7200))
                        .enabled(true)
                        .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(existingTrip));
        when(tripRepository.save(any(Trip.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TripDTO updatedTrip = tripService.changeStatus(USER_ID, tripId, TripStatus.PAUSED);

        // Then
        assertThat(updatedTrip.tripSettings().tripStatus()).isEqualTo(TripStatus.PAUSED);
        assertThat(updatedTrip.tripDetails().startTimestamp()).isEqualTo(startTime);
        assertThat(updatedTrip.tripDetails().endTimestamp())
                .isNull(); // Should not set end timestamp for pause
        verify(tripRepository).findById(tripId);
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void changeVisibility_whenTripNotFound_shouldThrowEntityNotFoundException() {
        // Given
        UUID nonExistentTripId = UUID.randomUUID();

        when(tripRepository.findById(nonExistentTripId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(
                        () ->
                                tripService.changeVisibility(
                                        USER_ID, nonExistentTripId, TripVisibility.PRIVATE))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trip not found");

        verify(tripRepository).findById(nonExistentTripId);
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void changeStatus_whenTripNotFound_shouldThrowEntityNotFoundException() {
        // Given
        UUID nonExistentTripId = UUID.randomUUID();

        when(tripRepository.findById(nonExistentTripId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(
                        () ->
                                tripService.changeStatus(
                                        USER_ID, nonExistentTripId, TripStatus.IN_PROGRESS))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trip not found");

        verify(tripRepository).findById(nonExistentTripId);
        verify(tripRepository, never()).save(any(Trip.class));
    }
}
