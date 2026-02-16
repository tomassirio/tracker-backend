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
    @Query(
            "SELECT CASE WHEN COUNT(ua) > 0 THEN true ELSE false END FROM UserAchievement ua "
                    + "JOIN ua.achievement a WHERE ua.user.id = :userId AND a.type = :achievementType AND ua.trip.id = :tripId")
    boolean existsByUserIdAndAchievementTypeAndTripId(
            @Param("userId") UUID userId,
            @Param("achievementType") AchievementType achievementType,
            @Param("tripId") UUID tripId);
}
