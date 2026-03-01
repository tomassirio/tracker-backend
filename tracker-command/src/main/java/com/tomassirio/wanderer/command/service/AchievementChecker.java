package com.tomassirio.wanderer.command.service;

import com.tomassirio.wanderer.command.service.impl.AchievementCalculationService;
import com.tomassirio.wanderer.commons.domain.AchievementType;
import java.util.List;

/**
 * Strategy interface for checking achievements against a context of type {@code T}.
 *
 * <p>Each implementation is responsible for a single achievement category (e.g., distance, update
 * count, followers). It declares which {@link AchievementType}s it covers and computes the current
 * metric value from the given context.
 *
 * <ul>
 *   <li>{@code AchievementChecker<Trip>} — trip-scoped (distance, updates, duration)
 *   <li>{@code AchievementChecker<UUID>} — user-scoped / social (followers, friends)
 * </ul>
 *
 * <p>Implementations are auto-discovered by Spring and collected by {@link
 * AchievementCalculationService}.
 *
 * @param <T> the context type used to compute the metric (e.g., {@code Trip}, {@code UUID})
 * @since 0.8.0
 */
public interface AchievementChecker<T> {

    /**
     * Returns the achievement types this checker is responsible for.
     *
     * @return ordered list of applicable achievement types
     */
    List<AchievementType> getApplicableTypes();

    /**
     * Computes the current metric value from the given context.
     *
     * @param context the context to evaluate (e.g., a Trip or a user ID)
     * @return the computed metric value
     */
    double computeMetric(T context);
}
