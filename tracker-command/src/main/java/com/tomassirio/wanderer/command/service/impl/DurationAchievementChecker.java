package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.command.service.TripAchievementChecker;
import com.tomassirio.wanderer.commons.domain.AchievementType;
import com.tomassirio.wanderer.commons.domain.Trip;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

/** Checks achievements based on the duration of a trip. */
@Component
public class DurationAchievementChecker implements TripAchievementChecker {

    @Override
    public List<AchievementType> getApplicableTypes() {
        return List.of(
                AchievementType.DURATION_7_DAYS,
                AchievementType.DURATION_30_DAYS,
                AchievementType.DURATION_45_DAYS,
                AchievementType.DURATION_60_DAYS);
    }

    @Override
    public double computeMetric(Trip trip) {
        if (trip.getTripDetails() == null || trip.getTripDetails().getStartTimestamp() == null) {
            return 0.0;
        }

        Instant endTime =
                trip.getTripDetails().getEndTimestamp() != null
                        ? trip.getTripDetails().getEndTimestamp()
                        : Instant.now();

        return Duration.between(trip.getTripDetails().getStartTimestamp(), endTime).toDays();
    }
}
