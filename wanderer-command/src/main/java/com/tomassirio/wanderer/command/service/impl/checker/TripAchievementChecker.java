package com.tomassirio.wanderer.command.service.impl.checker;

import com.tomassirio.wanderer.commons.domain.Trip;

/**
 * Achievement checker scoped to a {@link Trip}.
 *
 * <p>Convenience type alias for {@code AchievementChecker<Trip>} — used by Spring to collect all
 * trip-scoped checkers into {@code List<TripAchievementChecker>}.
 *
 * @see AchievementChecker
 */
public interface TripAchievementChecker extends AchievementChecker<Trip> {}
