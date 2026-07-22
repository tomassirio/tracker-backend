package com.tomassirio.wanderer.command.service.impl.checker;

import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.commons.domain.AchievementType;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Checks achievements based on the number of trips a user has created. */
@Component
@RequiredArgsConstructor
public class TripCountAchievementChecker implements SocialAchievementChecker {

    private final TripRepository tripRepository;

    @Override
    public List<AchievementType> getApplicableTypes() {
        return List.of(AchievementType.FIRST_TRIP);
    }

    @Override
    public double computeMetric(UUID userId) {
        return tripRepository.findAllByUserId(userId).size();
    }
}
