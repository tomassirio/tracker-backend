package com.tomassirio.wanderer.command.service.impl;

import com.google.maps.model.LatLng;
import com.tomassirio.wanderer.command.repository.TripUpdateRepository;
import com.tomassirio.wanderer.command.service.DistanceCalculationStrategy;
import com.tomassirio.wanderer.command.service.TripAchievementChecker;
import com.tomassirio.wanderer.commons.domain.AchievementType;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripUpdate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Checks achievements based on the total distance walked during a trip. */
@Component
@RequiredArgsConstructor
public class DistanceAchievementChecker implements TripAchievementChecker {

    private final TripUpdateRepository tripUpdateRepository;
    private final DistanceCalculationStrategy distanceCalculationStrategy;

    @Override
    public List<AchievementType> getApplicableTypes() {
        return List.of(
                AchievementType.DISTANCE_100KM,
                AchievementType.DISTANCE_200KM,
                AchievementType.DISTANCE_500KM,
                AchievementType.DISTANCE_800KM,
                AchievementType.DISTANCE_1000KM,
                AchievementType.DISTANCE_1600KM,
                AchievementType.DISTANCE_2200KM);
    }

    @Override
    public double computeMetric(Trip trip) {
        List<TripUpdate> updates =
                tripUpdateRepository.findByTripIdOrderByTimestampAsc(trip.getId());

        if (updates.size() < 2) {
            return 0.0;
        }

        List<LatLng> coordinates =
                updates.stream()
                        .filter(
                                update ->
                                        update.getLocation() != null
                                                && update.getLocation().getLat() != null
                                                && update.getLocation().getLon() != null)
                        .map(
                                update ->
                                        new LatLng(
                                                update.getLocation().getLat(),
                                                update.getLocation().getLon()))
                        .toList();

        if (coordinates.size() < 2) {
            return 0.0;
        }

        return distanceCalculationStrategy.calculatePathDistance(coordinates);
    }
}
