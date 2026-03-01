package com.tomassirio.wanderer.command.service;

import java.util.UUID;

/**
 * Service contract for evaluating and unlocking achievements.
 *
 * <p>Provides two entry points depending on the trigger context:
 *
 * <ul>
 *   <li>{@link #checkAndUnlockAchievements(UUID)} — trip-scoped (distance, updates, duration)
 *   <li>{@link #checkAndUnlockSocialAchievements(UUID)} — user-scoped (followers, friends)
 * </ul>
 *
 * @since 0.8.0
 */
public interface AchievementService {

    /**
     * Checks and unlocks trip-scoped achievements after a trip update.
     *
     * @param tripId the trip ID to evaluate
     */
    void checkAndUnlockAchievements(UUID tripId);

    /**
     * Checks and unlocks social achievements for a user.
     *
     * @param userId the user ID to evaluate
     */
    void checkAndUnlockSocialAchievements(UUID userId);
}
