package com.tomassirio.wanderer.command.repository;

import com.tomassirio.wanderer.commons.domain.AchievementType;
import com.tomassirio.wanderer.commons.domain.UserAchievement;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, UUID> {

    @Query("DELETE FROM UserAchievement ua WHERE ua.user.id = :userId")
    @org.springframework.data.jpa.repository.Modifying
    void deleteByUserId(@Param("userId") UUID userId);

    /**
     * Checks if an achievement already exists, handling both trip-scoped and user-scoped (social)
     * achievements in one query.
     *
     * @param userId the user ID
     * @param achievementType the achievement type
     * @param tripId the trip ID, or {@code null} for social achievements
     * @return {@code true} if the achievement is already unlocked
     */
    @Query(
            "SELECT CASE WHEN COUNT(ua) > 0 THEN true ELSE false END FROM UserAchievement ua "
                    + "JOIN ua.achievement a WHERE ua.user.id = :userId AND a.type = :achievementType "
                    + "AND (:tripId IS NULL AND ua.trip IS NULL OR ua.trip.id = :tripId)")
    boolean existsByUserIdAndAchievementTypeAndOptionalTripId(
            @Param("userId") UUID userId,
            @Param("achievementType") AchievementType achievementType,
            @Param("tripId") UUID tripId);
}
