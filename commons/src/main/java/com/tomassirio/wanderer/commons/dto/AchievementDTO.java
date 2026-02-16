package com.tomassirio.wanderer.commons.dto;

import com.tomassirio.wanderer.commons.domain.AchievementCategory;
import com.tomassirio.wanderer.commons.domain.AchievementType;

public record AchievementDTO(
        String id,
        AchievementType type,
        String name,
        String description,
        Integer thresholdValue,
        AchievementCategory category) {}
