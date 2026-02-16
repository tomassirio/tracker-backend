package com.tomassirio.wanderer.query.service;

import com.tomassirio.wanderer.commons.dto.AchievementDTO;
import com.tomassirio.wanderer.commons.dto.UnlockedAchievementDTO;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for achievement query operations. Provides methods to retrieve achievement
 * information.
 */
public interface AchievementQueryService {

    /**
     * Retrieves all available achievements in the system.
     *
     * @return list of all enabled achievements
     */
    List<AchievementDTO> getAvailableAchievements();

    /**
     * Retrieves all achievements unlocked by a specific user.
     *
     * @param userId the user's unique identifier
     * @return list of user achievements
     */
    List<UnlockedAchievementDTO> getUserAchievements(UUID userId);

    /**
     * Retrieves all achievements unlocked by a user for a specific trip.
     *
     * @param userId the user's unique identifier
     * @param tripId the trip's unique identifier
     * @return list of user achievements for the trip
     */
    List<UnlockedAchievementDTO> getUserAchievementsByTrip(UUID userId, UUID tripId);
}
