package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.command.repository.UserFollowRepository;
import com.tomassirio.wanderer.command.service.SocialAchievementChecker;
import com.tomassirio.wanderer.commons.domain.AchievementType;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Checks achievements based on the number of followers a user has. */
@Component
@RequiredArgsConstructor
public class FollowerAchievementChecker implements SocialAchievementChecker {

    private final UserFollowRepository userFollowRepository;

    @Override
    public List<AchievementType> getApplicableTypes() {
        return List.of(
                AchievementType.FOLLOWERS_10,
                AchievementType.FOLLOWERS_50,
                AchievementType.FOLLOWERS_100);
    }

    @Override
    public double computeMetric(UUID userId) {
        return userFollowRepository.findByFollowedId(userId).size();
    }
}
