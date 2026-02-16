package com.tomassirio.wanderer.command.service;

import com.tomassirio.wanderer.command.event.AchievementUnlockedEvent;
import com.tomassirio.wanderer.command.repository.FriendshipRepository;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.repository.TripUpdateRepository;
import com.tomassirio.wanderer.command.repository.UnlockedAchievementRepository;
import com.tomassirio.wanderer.command.repository.UserFollowRepository;
import com.tomassirio.wanderer.commons.domain.AchievementCategory;
import com.tomassirio.wanderer.commons.domain.AchievementType;
import com.tomassirio.wanderer.commons.domain.BaseAchievement;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripAchievement;
import com.tomassirio.wanderer.commons.domain.TripUpdate;
import com.tomassirio.wanderer.commons.domain.UserAchievement;
import jakarta.persistence.EntityManager;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for calculating and unlocking achievements based on trip activities.
 *
 * <p>This service checks various achievement criteria (distance, updates count, duration) and
 * publishes AchievementUnlockedEvent when criteria are met.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementCalculationService {

    private final TripRepository tripRepository;
    private final TripUpdateRepository tripUpdateRepository;
    private final UnlockedAchievementRepository unlockedAchievementRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserFollowRepository userFollowRepository;
    private final EntityManager entityManager;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Checks and unlocks achievements for a trip after a new update is added.
     *
     * @param tripId the trip ID to check achievements for
     */
    @Transactional
    public void checkAndUnlockAchievements(UUID tripId) {
        Trip trip =
                tripRepository
                        .findById(tripId)
                        .orElseThrow(() -> new RuntimeException("Trip not found"));

        // Check update count achievements
        checkUpdateCountAchievements(trip);

        // Check distance achievements (placeholder until Google Maps integration)
        checkDistanceAchievements(trip);

        // Check duration achievements
        checkDurationAchievements(trip);
    }

    /**
     * Checks and unlocks social achievements for a user (followers and friends).
     *
     * @param userId the user ID to check achievements for
     */
    @Transactional
    public void checkAndUnlockSocialAchievements(UUID userId) {
        // Check follower achievements
        checkFollowerAchievements(userId);

        // Check friend achievements
        checkFriendAchievements(userId);
    }

    private void checkUpdateCountAchievements(Trip trip) {
        long updateCount = tripUpdateRepository.countByTripId(trip.getId());

        for (AchievementType type :
                List.of(
                        AchievementType.UPDATES_10,
                        AchievementType.UPDATES_50,
                        AchievementType.UPDATES_100)) {
            if (updateCount >= type.getThreshold()) {
                unlockAchievementIfNotExists(trip, type, (double) updateCount);
            }
        }
    }

    private void checkDistanceAchievements(Trip trip) {
        // Calculate approximate distance from trip updates
        // TODO: Replace with Google Maps Distance Matrix API integration
        double distanceKm = calculateApproximateDistance(trip.getId());

        for (AchievementType type :
                List.of(
                        AchievementType.DISTANCE_100KM,
                        AchievementType.DISTANCE_200KM,
                        AchievementType.DISTANCE_500KM,
                        AchievementType.DISTANCE_800KM,
                        AchievementType.DISTANCE_1000KM,
                        AchievementType.DISTANCE_1600KM,
                        AchievementType.DISTANCE_2200KM)) {
            if (distanceKm >= type.getThreshold()) {
                unlockAchievementIfNotExists(trip, type, distanceKm);
            }
        }
    }

    private void checkDurationAchievements(Trip trip) {
        if (trip.getTripDetails() == null || trip.getTripDetails().getStartTimestamp() == null) {
            return;
        }

        Instant endTime =
                trip.getTripDetails().getEndTimestamp() != null
                        ? trip.getTripDetails().getEndTimestamp()
                        : Instant.now();

        long durationDays =
                Duration.between(trip.getTripDetails().getStartTimestamp(), endTime).toDays();

        for (AchievementType type :
                List.of(
                        AchievementType.DURATION_7_DAYS,
                        AchievementType.DURATION_30_DAYS,
                        AchievementType.DURATION_45_DAYS,
                        AchievementType.DURATION_60_DAYS)) {
            if (durationDays >= type.getThreshold()) {
                unlockAchievementIfNotExists(trip, type, (double) durationDays);
            }
        }
    }

    private void checkFollowerAchievements(UUID userId) {
        // Count followers (users who follow this user)
        long followerCount =
                userFollowRepository.findByFollowedId(userId).stream()
                        .filter(follow -> follow.getFollowedId().equals(userId))
                        .count();

        for (AchievementType type :
                List.of(
                        AchievementType.FOLLOWERS_10,
                        AchievementType.FOLLOWERS_50,
                        AchievementType.FOLLOWERS_100)) {
            if (followerCount >= type.getThreshold()) {
                unlockSocialAchievementIfNotExists(userId, type, (double) followerCount);
            }
        }
    }

    private void checkFriendAchievements(UUID userId) {
        // Count friends
        long friendCount = friendshipRepository.findByUserId(userId).size();

        for (AchievementType type :
                List.of(
                        AchievementType.FRIENDS_5,
                        AchievementType.FRIENDS_20,
                        AchievementType.FRIENDS_50)) {
            if (friendCount >= type.getThreshold()) {
                unlockSocialAchievementIfNotExists(userId, type, (double) friendCount);
            }
        }
    }

    /**
     * Calculates approximate distance using Haversine formula between consecutive trip updates.
     *
     * <p>This is a placeholder implementation. In production, this should use Google Maps Distance
     * Matrix API to calculate the shortest path distance.
     *
     * @param tripId the trip ID
     * @return approximate distance in kilometers
     */
    private double calculateApproximateDistance(UUID tripId) {
        List<TripUpdate> updates = tripUpdateRepository.findByTripIdOrderByTimestampAsc(tripId);

        if (updates.size() < 2) {
            return 0.0;
        }

        double totalDistance = 0.0;
        for (int i = 0; i < updates.size() - 1; i++) {
            TripUpdate current = updates.get(i);
            TripUpdate next = updates.get(i + 1);

            if (current.getLocation() != null && next.getLocation() != null) {
                totalDistance +=
                        haversineDistance(
                                current.getLocation().getLat(),
                                current.getLocation().getLon(),
                                next.getLocation().getLat(),
                                next.getLocation().getLon());
            }
        }

        return totalDistance;
    }

    /**
     * Calculates distance between two coordinates using Haversine formula.
     *
     * @param lat1 latitude of first point
     * @param lon1 longitude of first point
     * @param lat2 latitude of second point
     * @param lon2 longitude of second point
     * @return distance in kilometers
     */
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // Earth's radius in kilometers

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2)
                        + Math.cos(Math.toRadians(lat1))
                                * Math.cos(Math.toRadians(lat2))
                                * Math.sin(dLon / 2)
                                * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    private void unlockAchievementIfNotExists(Trip trip, AchievementType type, Double value) {
        // Check if user already has this achievement for this trip
        boolean exists =
                userAchievementRepository.existsByUserIdAndAchievementTypeAndTripId(
                        trip.getUserId(), type, trip.getId());

        if (exists) {
            log.debug(
                    "BaseAchievement {} already unlocked for user {} on trip {}",
                    type,
                    trip.getUserId(),
                    trip.getId());
            return;
        }

        // Get or create achievement
        BaseAchievement achievement = getOrCreateAchievement(type);

        // Publish achievement unlocked event
        UUID userAchievementId = UUID.randomUUID();
        eventPublisher.publishEvent(
                AchievementUnlockedEvent.builder()
                        .userAchievementId(userAchievementId)
                        .userId(trip.getUserId())
                        .achievementId(achievement.getId())
                        .tripId(trip.getId())
                        .achievementType(type)
                        .achievementName(achievement.getName())
                        .valueAchieved(value)
                        .unlockedAt(Instant.now())
                        .build());

        log.info(
                "BaseAchievement {} unlocked for user {} on trip {}",
                type,
                trip.getUserId(),
                trip.getId());
    }

    private void unlockSocialAchievementIfNotExists(
            UUID userId, AchievementType type, Double value) {
        // Check if user already has this social achievement (trip_id is null for social
        // achievements)
        boolean exists =
                entityManager
                        .createQuery(
                                "SELECT COUNT(ua) > 0 FROM UnlockedAchievement ua JOIN ua.achievement a "
                                        + "WHERE ua.user.id = :userId AND a.type = :type AND ua.trip IS NULL",
                                Boolean.class)
                        .setParameter("userId", userId)
                        .setParameter("type", type)
                        .getSingleResult();

        if (exists) {
            log.debug("Social achievement {} already unlocked for user {}", type, userId);
            return;
        }

        // Get or create achievement
        BaseAchievement achievement = getOrCreateAchievement(type);

        // Publish achievement unlocked event (with null tripId for social achievements)
        UUID userAchievementId = UUID.randomUUID();
        eventPublisher.publishEvent(
                AchievementUnlockedEvent.builder()
                        .userAchievementId(userAchievementId)
                        .userId(userId)
                        .achievementId(achievement.getId())
                        .tripId(null) // Social achievements are not tied to a specific trip
                        .achievementType(type)
                        .achievementName(achievement.getName())
                        .valueAchieved(value)
                        .unlockedAt(Instant.now())
                        .build());

        log.info("Social achievement {} unlocked for user {}", type, userId);
    }

    private BaseAchievement getOrCreateAchievement(AchievementType type) {
        // Try to find existing achievement
        return entityManager
                .createQuery(
                        "SELECT a FROM BaseAchievement a WHERE a.type = :type AND a.enabled = true",
                        BaseAchievement.class)
                .setParameter("type", type)
                .getResultStream()
                .findFirst()
                .orElseGet(
                        () -> {
                            // Create new achievement based on category
                            BaseAchievement achievement;
                            if (type.getCategory() == AchievementCategory.TRIP) {
                                achievement =
                                        TripAchievement.builder()
                                                .id(UUID.randomUUID())
                                                .type(type)
                                                .name(type.getName())
                                                .description(type.getDescription())
                                                .thresholdValue(type.getThreshold())
                                                .enabled(true)
                                                .build();
                            } else {
                                achievement =
                                        UserAchievement.builder()
                                                .id(UUID.randomUUID())
                                                .type(type)
                                                .name(type.getName())
                                                .description(type.getDescription())
                                                .thresholdValue(type.getThreshold())
                                                .enabled(true)
                                                .build();
                            }
                            entityManager.persist(achievement);
                            return achievement;
                        });
    }
}
