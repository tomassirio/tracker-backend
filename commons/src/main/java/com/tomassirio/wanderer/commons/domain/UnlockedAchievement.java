package com.tomassirio.wanderer.commons.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an achievement that has been unlocked by a user.
 *
 * <p>This entity tracks which user unlocked which achievement, when they unlocked it, and the value
 * they achieved. For trip-based achievements, the trip is also recorded. For user-based
 * achievements (social), trip is null.
 */
@Entity
@Table(name = "unlocked_achievements")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UnlockedAchievement {

    @Id private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false)
    private BaseAchievement achievement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = true)
    private Trip trip;

    @NotNull
    @Column(name = "unlocked_at", nullable = false)
    private Instant unlockedAt;

    @Column(name = "value_achieved")
    private Double valueAchieved;
}
