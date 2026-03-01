package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.command.repository.TripUpdateRepository;
import com.tomassirio.wanderer.command.service.TripAchievementChecker;
import com.tomassirio.wanderer.commons.domain.AchievementType;
import com.tomassirio.wanderer.commons.domain.Trip;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Checks achievements based on the number of trip updates posted. */
@Component
@RequiredArgsConstructor
public class UpdateCountAchievementChecker implements TripAchievementChecker {

    private final TripUpdateRepository tripUpdateRepository;

    @Override
    public List<AchievementType> getApplicableTypes() {
        return List.of(
                AchievementType.UPDATES_10,
                AchievementType.UPDATES_50,
                AchievementType.UPDATES_100);
    }

    @Override
    public double computeMetric(Trip trip) {
        return tripUpdateRepository.countByTripId(trip.getId());
    }
}
