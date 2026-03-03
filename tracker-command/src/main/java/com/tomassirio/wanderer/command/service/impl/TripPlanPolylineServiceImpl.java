package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.command.repository.TripPlanRepository;
import com.tomassirio.wanderer.command.service.TripPlanPolylineService;
import com.tomassirio.wanderer.command.service.helper.PolylineComputer;
import com.tomassirio.wanderer.commons.domain.GeoLocation;
import com.tomassirio.wanderer.commons.domain.TripPlan;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link TripPlanPolylineService} that computes encoded polylines for trip plans.
 *
 * <p>The polyline is computed from the full planned route: start → waypoints → end. Delegates the
 * actual compute-filter-encode logic to {@link PolylineComputer}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TripPlanPolylineServiceImpl implements TripPlanPolylineService {

    private final TripPlanRepository tripPlanRepository;
    private final PolylineComputer polylineComputer;

    @Override
    @Transactional
    public void computePolyline(UUID tripPlanId) {
        TripPlan tripPlan =
                tripPlanRepository
                        .findById(tripPlanId)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                "Trip plan not found: " + tripPlanId));

        List<GeoLocation> routeLocations = buildRouteLocations(tripPlan);

        polylineComputer.computeAndApply(tripPlan, routeLocations, tripPlanRepository::save);
    }

    /** Builds the ordered list of route locations from start → waypoints → end. */
    private List<GeoLocation> buildRouteLocations(TripPlan tripPlan) {
        List<GeoLocation> locations = new ArrayList<>();

        if (tripPlan.getStartLocation() != null) {
            locations.add(tripPlan.getStartLocation());
        }

        if (tripPlan.getWaypoints() != null) {
            locations.addAll(tripPlan.getWaypoints());
        }

        if (tripPlan.getEndLocation() != null) {
            locations.add(tripPlan.getEndLocation());
        }

        return locations;
    }
}
