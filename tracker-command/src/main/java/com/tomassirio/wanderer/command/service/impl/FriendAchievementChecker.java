package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.command.repository.FriendshipRepository;
import com.tomassirio.wanderer.command.service.SocialAchievementChecker;
import com.tomassirio.wanderer.commons.domain.AchievementType;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Checks achievements based on the number of friends a user has. */
@Component
@RequiredArgsConstructor
public class FriendAchievementChecker implements SocialAchievementChecker {

    private final FriendshipRepository friendshipRepository;

    @Override
    public List<AchievementType> getApplicableTypes() {
        return List.of(
                AchievementType.FRIENDS_5, AchievementType.FRIENDS_20, AchievementType.FRIENDS_50);
    }

    @Override
    public double computeMetric(UUID userId) {
        return friendshipRepository.findByUserId(userId).size();
    }
}
