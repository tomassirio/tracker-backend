package com.tomassirio.wanderer.commons.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Trip-based achievement that is unlocked based on trip activities.
 *
 * <p>Examples: distance walked, number of updates, trip duration.
 */
@Entity
@DiscriminatorValue("TRIP")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class TripAchievement extends BaseAchievement {

    @Override
    public AchievementCategory getCategory() {
        return AchievementCategory.TRIP;
    }
}
