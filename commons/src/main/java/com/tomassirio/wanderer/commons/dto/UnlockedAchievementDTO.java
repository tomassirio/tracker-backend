package com.tomassirio.wanderer.commons.dto;

import java.time.Instant;

public record UnlockedAchievementDTO(
        String id,
        String userId,
        AchievementDTO achievement,
        String tripId,
        Instant unlockedAt,
        Double valueAchieved) {}
