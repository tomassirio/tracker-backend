package com.tomassirio.wanderer.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripStatus;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import com.tomassirio.wanderer.commons.dto.TripDTO;
import com.tomassirio.wanderer.query.repository.FriendshipRepository;
import com.tomassirio.wanderer.query.repository.TripRepository;
import com.tomassirio.wanderer.query.repository.UserFollowRepository;
import com.tomassirio.wanderer.query.service.impl.TripServiceImpl;
import com.tomassirio.wanderer.query.utils.TestEntityFactory;
import jakarta.persistence.EntityNotFoundException;
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
class TripServiceTest {

    @Mock private TripRepository tripRepository;

    @Mock private FriendshipRepository friendshipRepository;

    @Mock private UserFollowRepository userFollowRepository;

    @InjectMocks private TripServiceImpl tripService;

    @Test
    void getTrip_whenTripExists_shouldReturnTripDTO() {
        // Given
        UUID tripId = UUID.randomUUID();
        Trip trip = TestEntityFactory.createTrip(tripId, "Test Trip");

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        // When
        TripDTO result = tripService.getTrip(tripId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(tripId.toString());
        assertThat(result.name()).isEqualTo("Test Trip");
        assertThat(result.userId()).isEqualTo(TestEntityFactory.USER_ID.toString());
        assertThat(result.tripSettings().visibility()).isEqualTo(TripVisibility.PUBLIC);
        assertThat(result.tripSettings().tripStatus()).isEqualTo(TripStatus.CREATED);
        assertThat(result.enabled()).isTrue();

        verify(tripRepository).findById(tripId);
    }

    @Test
    void getTrip_whenTripDoesNotExist_shouldThrowEntityNotFoundException() {
        // Given
        UUID nonExistentTripId = UUID.randomUUID();
        when(tripRepository.findById(nonExistentTripId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tripService.getTrip(nonExistentTripId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trip not found");

        verify(tripRepository).findById(nonExistentTripId);
    }

    @Test
    void getAllTrips_whenTripsExist_shouldReturnListOfTripDTOs() {
        // Given
        Trip trip1 =
                TestEntityFactory.createTrip(UUID.randomUUID(), "Trip 1", TripVisibility.PUBLIC);
        Trip trip2 =
                TestEntityFactory.createTrip(UUID.randomUUID(), "Trip 2", TripVisibility.PRIVATE);

        List<Trip> trips = List.of(trip1, trip2);
        when(tripRepository.findAll()).thenReturn(trips);

        // When
        List<TripDTO> result = tripService.getAllTrips();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().name()).isEqualTo("Trip 1");
        assertThat(result.get(0).tripSettings().visibility()).isEqualTo(TripVisibility.PUBLIC);
        assertThat(result.get(0).tripSettings().tripStatus()).isEqualTo(TripStatus.CREATED);
        assertThat(result.get(1).name()).isEqualTo("Trip 2");
        assertThat(result.get(1).tripSettings().visibility()).isEqualTo(TripVisibility.PRIVATE);
        assertThat(result.get(1).tripSettings().tripStatus()).isEqualTo(TripStatus.CREATED);

        verify(tripRepository).findAll();
    }

    @Test
    void getAllTrips_whenNoTripsExist_shouldReturnEmptyList() {
        // Given
        when(tripRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<TripDTO> result = tripService.getAllTrips();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(tripRepository).findAll();
    }

    @Test
    void getAllTrips_shouldMapAllFieldsCorrectly() {
        // Given
        UUID tripId = UUID.randomUUID();

        Trip trip =
                TestEntityFactory.createTrip(tripId, "Summer Road Trip", TripVisibility.PROTECTED);

        when(tripRepository.findAll()).thenReturn(List.of(trip));

        // When
        List<TripDTO> result = tripService.getAllTrips();

        // Then
        assertThat(result).hasSize(1);
        TripDTO tripDTO = result.getFirst();
        assertThat(tripDTO.id()).isEqualTo(tripId.toString());
        assertThat(tripDTO.name()).isEqualTo("Summer Road Trip");
        assertThat(tripDTO.userId()).isEqualTo(TestEntityFactory.USER_ID.toString());
        assertThat(tripDTO.tripSettings().visibility()).isEqualTo(TripVisibility.PROTECTED);
        assertThat(tripDTO.tripSettings().tripStatus()).isEqualTo(TripStatus.CREATED);
        assertThat(tripDTO.enabled()).isTrue();
        assertThat(tripDTO.creationTimestamp()).isNotNull();

        verify(tripRepository).findAll();
    }

    @Test
    void getTripsForUser_whenTripsExist_shouldReturnTripDTOs() {
        // Given
        UUID userId = TestEntityFactory.USER_ID;
        UUID tripId = UUID.randomUUID();
        Trip trip = TestEntityFactory.createTrip(tripId, "Owned Trip");

        when(tripRepository.findByUserId(userId)).thenReturn(List.of(trip));

        // When
        List<TripDTO> result = tripService.getTripsForUser(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        TripDTO dto = result.getFirst();
        assertThat(dto.id()).isEqualTo(tripId.toString());
        assertThat(dto.userId()).isEqualTo(userId.toString());
        assertThat(dto.name()).isEqualTo("Owned Trip");

        verify(tripRepository).findByUserId(userId);
    }

    @Test
    void getTripsForUser_whenNoTripsExist_shouldReturnEmptyList() {
        // Given
        UUID userId = TestEntityFactory.USER_ID;
        when(tripRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        // When
        List<TripDTO> result = tripService.getTripsForUser(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(tripRepository).findByUserId(userId);
    }

    @Test
    void getPublicTrips_whenPublicTripsExist_shouldReturnOnlyPublicTrips() {
        // Given
        Trip publicTrip1 =
                TestEntityFactory.createTrip(
                        UUID.randomUUID(), "Public Trip 1", TripVisibility.PUBLIC);
        Trip publicTrip2 =
                TestEntityFactory.createTrip(
                        UUID.randomUUID(), "Public Trip 2", TripVisibility.PUBLIC);

        when(tripRepository.findByTripSettingsVisibility(TripVisibility.PUBLIC))
                .thenReturn(List.of(publicTrip1, publicTrip2));

        // When
        List<TripDTO> result = tripService.getPublicTrips();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Public Trip 1");
        assertThat(result.get(0).tripSettings().visibility()).isEqualTo(TripVisibility.PUBLIC);
        assertThat(result.get(1).name()).isEqualTo("Public Trip 2");
        assertThat(result.get(1).tripSettings().visibility()).isEqualTo(TripVisibility.PUBLIC);

        verify(tripRepository).findByTripSettingsVisibility(TripVisibility.PUBLIC);
    }

    @Test
    void getPublicTrips_whenNoPublicTripsExist_shouldReturnEmptyList() {
        // Given
        when(tripRepository.findByTripSettingsVisibility(TripVisibility.PUBLIC))
                .thenReturn(Collections.emptyList());

        // When
        List<TripDTO> result = tripService.getPublicTrips();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(tripRepository).findByTripSettingsVisibility(TripVisibility.PUBLIC);
    }

    @Test
    void getTripsForUserWithVisibility_whenTripsExist_shouldReturnPublicAndProtectedTrips() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID requestingUserId = UUID.randomUUID();
        Trip publicTrip =
                TestEntityFactory.createTrip(
                        UUID.randomUUID(), "Public Trip", TripVisibility.PUBLIC);
        Trip protectedTrip =
                TestEntityFactory.createTrip(
                        UUID.randomUUID(), "Protected Trip", TripVisibility.PROTECTED);

        when(friendshipRepository.existsByUserIdAndFriendId(requestingUserId, userId))
                .thenReturn(true);
        List<TripVisibility> visibilities =
                List.of(TripVisibility.PUBLIC, TripVisibility.PROTECTED);
        when(tripRepository.findByUserIdAndVisibilityIn(userId, visibilities))
                .thenReturn(List.of(publicTrip, protectedTrip));

        // When
        List<TripDTO> result = tripService.getTripsForUserWithVisibility(userId, requestingUserId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Public Trip");
        assertThat(result.get(0).tripSettings().visibility()).isEqualTo(TripVisibility.PUBLIC);
        assertThat(result.get(1).name()).isEqualTo("Protected Trip");
        assertThat(result.get(1).tripSettings().visibility()).isEqualTo(TripVisibility.PROTECTED);

        verify(tripRepository).findByUserIdAndVisibilityIn(userId, visibilities);
    }

    @Test
    void getTripsForUserWithVisibility_whenNoTripsExist_shouldReturnEmptyList() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID requestingUserId = UUID.randomUUID();
        when(friendshipRepository.existsByUserIdAndFriendId(requestingUserId, userId))
                .thenReturn(true);
        List<TripVisibility> visibilities =
                List.of(TripVisibility.PUBLIC, TripVisibility.PROTECTED);
        when(tripRepository.findByUserIdAndVisibilityIn(userId, visibilities))
                .thenReturn(Collections.emptyList());

        // When
        List<TripDTO> result = tripService.getTripsForUserWithVisibility(userId, requestingUserId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(tripRepository).findByUserIdAndVisibilityIn(userId, visibilities);
    }

    @Test
    void getTripsForUserWithVisibility_shouldNotIncludePrivateTrips() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID requestingUserId = UUID.randomUUID();
        Trip publicTrip =
                TestEntityFactory.createTrip(
                        UUID.randomUUID(), "Public Trip", TripVisibility.PUBLIC);

        when(friendshipRepository.existsByUserIdAndFriendId(requestingUserId, userId))
                .thenReturn(true);
        List<TripVisibility> visibilities =
                List.of(TripVisibility.PUBLIC, TripVisibility.PROTECTED);
        when(tripRepository.findByUserIdAndVisibilityIn(userId, visibilities))
                .thenReturn(List.of(publicTrip));

        // When
        List<TripDTO> result = tripService.getTripsForUserWithVisibility(userId, requestingUserId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(
                        result.stream()
                                .noneMatch(
                                        dto ->
                                                dto.tripSettings().visibility()
                                                        == TripVisibility.PRIVATE))
                .isTrue();

        verify(tripRepository).findByUserIdAndVisibilityIn(userId, visibilities);
    }

    @Test
    void getOngoingPublicTrips_whenOngoingTripsExist_shouldReturnOngoingPublicTrips() {
        // Given
        Trip ongoingTrip1 =
                TestEntityFactory.createTrip(
                        UUID.randomUUID(), "Ongoing Trip 1", TripVisibility.PUBLIC);
        ongoingTrip1.getTripSettings().setTripStatus(TripStatus.IN_PROGRESS);

        Trip ongoingTrip2 =
                TestEntityFactory.createTrip(
                        UUID.randomUUID(), "Ongoing Trip 2", TripVisibility.PUBLIC);
        ongoingTrip2.getTripSettings().setTripStatus(TripStatus.IN_PROGRESS);

        when(tripRepository.findByVisibilityAndStatus(
                        TripVisibility.PUBLIC, TripStatus.IN_PROGRESS))
                .thenReturn(List.of(ongoingTrip1, ongoingTrip2));

        // When
        List<TripDTO> result = tripService.getOngoingPublicTrips(null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Ongoing Trip 1");
        assertThat(result.get(0).tripSettings().visibility()).isEqualTo(TripVisibility.PUBLIC);
        assertThat(result.get(0).tripSettings().tripStatus()).isEqualTo(TripStatus.IN_PROGRESS);
        assertThat(result.get(1).name()).isEqualTo("Ongoing Trip 2");
        assertThat(result.get(1).tripSettings().tripStatus()).isEqualTo(TripStatus.IN_PROGRESS);

        verify(tripRepository)
                .findByVisibilityAndStatus(TripVisibility.PUBLIC, TripStatus.IN_PROGRESS);
    }

    @Test
    void getOngoingPublicTrips_whenNoOngoingTripsExist_shouldReturnEmptyList() {
        // Given
        when(tripRepository.findByVisibilityAndStatus(
                        TripVisibility.PUBLIC, TripStatus.IN_PROGRESS))
                .thenReturn(Collections.emptyList());

        // When
        List<TripDTO> result = tripService.getOngoingPublicTrips(null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(tripRepository)
                .findByVisibilityAndStatus(TripVisibility.PUBLIC, TripStatus.IN_PROGRESS);
    }

    @Test
    void getOngoingPublicTrips_shouldOnlyReturnInProgressPublicTrips() {
        // Given
        Trip ongoingPublicTrip =
                TestEntityFactory.createTrip(
                        UUID.randomUUID(), "Ongoing Public", TripVisibility.PUBLIC);
        ongoingPublicTrip.getTripSettings().setTripStatus(TripStatus.IN_PROGRESS);

        when(tripRepository.findByVisibilityAndStatus(
                        TripVisibility.PUBLIC, TripStatus.IN_PROGRESS))
                .thenReturn(List.of(ongoingPublicTrip));

        // When
        List<TripDTO> result = tripService.getOngoingPublicTrips(null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).tripSettings().visibility()).isEqualTo(TripVisibility.PUBLIC);
        assertThat(result.get(0).tripSettings().tripStatus()).isEqualTo(TripStatus.IN_PROGRESS);

        verify(tripRepository)
                .findByVisibilityAndStatus(TripVisibility.PUBLIC, TripStatus.IN_PROGRESS);
    }
}
