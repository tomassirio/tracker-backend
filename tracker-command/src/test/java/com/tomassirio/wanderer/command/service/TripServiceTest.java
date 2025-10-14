package com.tomassirio.wanderer.command.service;

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
import com.tomassirio.wanderer.command.service.impl.TripServiceImpl;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock private TripRepository tripRepository;

    @Mock private UserRepository userRepository;

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
        assertThat(createdTrip.userId()).isEqualTo(USER_ID);
        assertThat(createdTrip.tripStatus()).isEqualTo(TripStatus.CREATED);
        assertThat(createdTrip.visibility()).isEqualTo(TripVisibility.PUBLIC);
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
        assertThat(createdTrip.visibility()).isEqualTo(TripVisibility.PRIVATE);
        assertThat(createdTrip.tripStatus()).isEqualTo(TripStatus.CREATED);

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
        assertThat(createdTrip.startTimestamp()).isNull();
        assertThat(createdTrip.endTimestamp()).isNull();
        assertThat(createdTrip.tripPlanId()).isNull();
        assertThat(createdTrip.updateRefresh()).isNull();

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
        TripDTO updatedTrip = tripService.updateTrip(tripId, request);

        // Then
        assertThat(updatedTrip).isNotNull();
        assertThat(updatedTrip.id()).isEqualTo(tripId);
        assertThat(updatedTrip.name()).isEqualTo("Updated Trip Name");
        assertThat(updatedTrip.visibility()).isEqualTo(TripVisibility.PUBLIC);

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
        assertThatThrownBy(() -> tripService.updateTrip(nonExistentTripId, request))
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
        TripDTO updatedTrip = tripService.updateTrip(tripId, request);

        // Then
        assertThat(updatedTrip.visibility()).isEqualTo(TripVisibility.PUBLIC);
        assertThat(updatedTrip.name()).isEqualTo("Trip Name");

        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void deleteTrip_whenCalled_shouldCallRepositoryDelete() {
        // Given
        UUID tripId = UUID.randomUUID();

        // When
        tripService.deleteTrip(tripId);

        // Then
        verify(tripRepository).deleteById(tripId);
    }

    @Test
    void createTrip_shouldSetStatusToCreated() {
        // Given
        TripCreationRequest request =
                TestEntityFactory.createTripCreationRequest("Test Trip", TripVisibility.PUBLIC);

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
        assertThat(createdTrip.tripStatus()).isEqualTo(TripStatus.CREATED);
        assertThat(createdTrip.enabled()).isTrue();

        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void createTrip_shouldInitializeEmbeddedObjects() {
        // Given
        TripCreationRequest request =
                TestEntityFactory.createTripCreationRequest("Test Trip", TripVisibility.PROTECTED);

        when(tripRepository.save(any(Trip.class)))
                .thenAnswer(
                        invocation -> {
                            Trip trip = invocation.getArgument(0);
                            // Verify embedded objects are properly initialized
                            assertThat(trip.getTripSettings()).isNotNull();
                            assertThat(trip.getTripSettings().getTripStatus())
                                    .isEqualTo(TripStatus.CREATED);
                            assertThat(trip.getTripSettings().getVisibility())
                                    .isEqualTo(TripVisibility.PROTECTED);
                            assertThat(trip.getTripDetails()).isNotNull();
                            trip.setId(UUID.randomUUID());
                            return trip;
                        });

        // When
        TripDTO createdTrip = tripService.createTrip(USER_ID, request);

        // Then
        assertThat(createdTrip).isNotNull();
        assertThat(createdTrip.visibility()).isEqualTo(TripVisibility.PROTECTED);

        verify(tripRepository).save(any(Trip.class));
    }
}
