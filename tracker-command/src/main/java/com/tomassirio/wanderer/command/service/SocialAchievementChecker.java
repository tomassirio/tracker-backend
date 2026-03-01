package com.tomassirio.wanderer.command.service;

import java.util.UUID;

/**
 * Achievement checker scoped to a user (social achievements).
 *
 * <p>Convenience type alias for {@code AchievementChecker<UUID>} — used by Spring to collect all
 * user-scoped checkers into {@code List<SocialAchievementChecker>}.
 *
 * @see AchievementChecker
 */
public interface SocialAchievementChecker extends AchievementChecker<UUID> {}
