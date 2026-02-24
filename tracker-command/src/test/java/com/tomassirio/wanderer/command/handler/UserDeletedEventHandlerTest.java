package com.tomassirio.wanderer.command.handler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.UserDeletedEvent;
import com.tomassirio.wanderer.command.repository.ActiveTripRepository;
import com.tomassirio.wanderer.command.repository.FriendRequestRepository;
import com.tomassirio.wanderer.command.repository.FriendshipRepository;
import com.tomassirio.wanderer.command.repository.PromotedTripRepository;
import com.tomassirio.wanderer.command.repository.TripPlanRepository;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.repository.UserAchievementRepository;
import com.tomassirio.wanderer.command.repository.UserFollowRepository;
import com.tomassirio.wanderer.command.repository.UserRepository;
import com.tomassirio.wanderer.commons.domain.Trip;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserDeletedEventHandlerTest {

    @Mock private UserRepository userRepository;
    @Mock private TripRepository tripRepository;
    @Mock private TripPlanRepository tripPlanRepository;
    @Mock private FriendshipRepository friendshipRepository;
    @Mock private FriendRequestRepository friendRequestRepository;
    @Mock private UserFollowRepository userFollowRepository;
    @Mock private UserAchievementRepository userAchievementRepository;
    @Mock private ActiveTripRepository activeTripRepository;
    @Mock private PromotedTripRepository promotedTripRepository;

    @InjectMocks private UserDeletedEventHandler handler;

    @Test
    void handle_shouldDeleteAllUserRelatedData() {
        // Given
        UUID userId = UUID.randomUUID();
        UserDeletedEvent event = UserDeletedEvent.builder().userId(userId).build();

        UUID tripId1 = UUID.randomUUID();
        UUID tripId2 = UUID.randomUUID();
        Trip trip1 = Trip.builder().id(tripId1).userId(userId).build();
        Trip trip2 = Trip.builder().id(tripId2).userId(userId).build();
        List<Trip> userTrips = List.of(trip1, trip2);

        when(tripRepository.findAllByUserId(userId)).thenReturn(userTrips);

        // When
        handler.handle(event);

        // Then
        verify(userAchievementRepository).deleteByUserId(userId);
        verify(activeTripRepository).deleteById(userId);
        verify(userFollowRepository).deleteByFollowerIdOrFollowedId(userId, userId);
        verify(friendRequestRepository).deleteBySenderIdOrReceiverId(userId, userId);
        verify(friendshipRepository).deleteByUserIdOrFriendId(userId, userId);
        verify(promotedTripRepository).deleteByTripId(tripId1);
        verify(promotedTripRepository).deleteByTripId(tripId2);
        verify(tripRepository).findAllByUserId(userId);
        verify(tripRepository).deleteAll(userTrips);
        verify(tripPlanRepository).deleteByUserId(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void handle_whenNoTrips_shouldStillDeleteUserAndRelationships() {
        // Given
        UUID userId = UUID.randomUUID();
        UserDeletedEvent event = UserDeletedEvent.builder().userId(userId).build();

        when(tripRepository.findAllByUserId(userId)).thenReturn(List.of());

        // When
        handler.handle(event);

        // Then
        verify(userAchievementRepository).deleteByUserId(userId);
        verify(activeTripRepository).deleteById(userId);
        verify(userFollowRepository).deleteByFollowerIdOrFollowedId(userId, userId);
        verify(friendRequestRepository).deleteBySenderIdOrReceiverId(userId, userId);
        verify(friendshipRepository).deleteByUserIdOrFriendId(userId, userId);
        verify(tripRepository).deleteAll(List.of());
        verify(tripPlanRepository).deleteByUserId(userId);
        verify(userRepository).deleteById(userId);
    }
}
