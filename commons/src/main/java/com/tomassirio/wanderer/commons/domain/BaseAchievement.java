package com.tomassirio.wanderer.commons.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Abstract base class for achievement definitions.
 *
 * <p>Achievements can be either trip-based (TripAchievement) or user-based (UserAchievement). This
 * class contains the common properties shared by all achievement types.
 */
@Entity
@Table(name = "achievements")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "achievement_category")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public abstract class BaseAchievement {

    @Id private UUID id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, unique = true)
    private AchievementType type;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Column(name = "threshold_value", nullable = false)
    private Integer thresholdValue;

    @NotNull
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    /**
     * Returns the category of this achievement.
     *
     * @return the achievement category
     */
    public abstract AchievementCategory getCategory();
}
