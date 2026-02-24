package com.tomassirio.wanderer.command.handler;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event handler for UserDeletedEvent that cascades the deletion of all user-related data.
 *
 * <p>Deletes user achievements, active trips, promoted trips, trips (which cascade to comments and
 * trip updates), trip plans, friendships, friend requests, follow relationships, and finally the
 * user record itself.
 *
 * @since 0.5.3
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserDeletedEventHandler implements EventHandler<UserDeletedEvent> {

    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final TripPlanRepository tripPlanRepository;
    private final FriendshipRepository friendshipRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final UserFollowRepository userFollowRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final ActiveTripRepository activeTripRepository;
    private final PromotedTripRepository promotedTripRepository;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(UserDeletedEvent event) {
        UUID userId = event.getUserId();
        log.debug("Processing UserDeletedEvent for user: {}", userId);

        // Delete user achievements
        userAchievementRepository.deleteByUserId(userId);
        log.debug("Deleted achievements for user: {}", userId);

        // Delete active trip record
        activeTripRepository.deleteById(userId);
        log.debug("Deleted active trip for user: {}", userId);

        // Delete follow relationships (both directions)
        userFollowRepository.deleteByFollowerIdOrFollowedId(userId, userId);
        log.debug("Deleted follow relationships for user: {}", userId);

        // Delete friend requests (both sent and received)
        friendRequestRepository.deleteBySenderIdOrReceiverId(userId, userId);
        log.debug("Deleted friend requests for user: {}", userId);

        // Delete friendships (both directions)
        friendshipRepository.deleteByUserIdOrFriendId(userId, userId);
        log.debug("Deleted friendships for user: {}", userId);

        // Delete promoted trips for user's trips, then delete trips
        List<Trip> userTrips = tripRepository.findAllByUserId(userId);
        for (Trip trip : userTrips) {
            promotedTripRepository.deleteByTripId(trip.getId());
        }
        tripRepository.deleteAll(userTrips);
        log.debug("Deleted {} trips (and their promotions) for user: {}", userTrips.size(), userId);

        // Delete trip plans
        tripPlanRepository.deleteByUserId(userId);
        log.debug("Deleted trip plans for user: {}", userId);

        // Delete the user record
        userRepository.deleteById(userId);
        log.info("User deleted and all related data cleaned up: {}", userId);
    }
}
