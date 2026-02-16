package com.tomassirio.wanderer.query.repository;

import com.tomassirio.wanderer.commons.domain.UnlockedAchievement;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UnlockedAchievementRepository extends JpaRepository<UnlockedAchievement, UUID> {
    @Query(
            "SELECT ua FROM UnlockedAchievement ua JOIN FETCH ua.achievement WHERE ua.user.id = :userId")
    List<UnlockedAchievement> findByUserId(@Param("userId") UUID userId);

    @Query(
            "SELECT ua FROM UnlockedAchievement ua JOIN FETCH ua.achievement WHERE ua.user.id = :userId AND ua.trip.id = :tripId")
    List<UnlockedAchievement> findByUserIdAndTripId(
            @Param("userId") UUID userId, @Param("tripId") UUID tripId);
}
