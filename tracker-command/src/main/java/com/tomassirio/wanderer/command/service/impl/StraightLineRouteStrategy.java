package com.tomassirio.wanderer.command.service.impl;

import com.google.maps.model.LatLng;
import com.tomassirio.wanderer.command.service.RouteService;
import com.tomassirio.wanderer.commons.domain.GeoLocation;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Offline route strategy that returns straight-line segments between locations.
 *
 * <p>Used when the Google Maps API is not configured or disabled. Each segment is represented by
 * exactly two points (origin and destination) with no road-snapping.
 */
@Slf4j
public class StraightLineRouteStrategy implements RouteService {

    @Override
    public List<LatLng> getRoutePoints(GeoLocation origin, GeoLocation destination) {
        return List.of(
                new LatLng(origin.getLat(), origin.getLon()),
                new LatLng(destination.getLat(), destination.getLon()));
    }

    @Override
    public List<LatLng> getFullRoutePoints(List<GeoLocation> locations) {
        if (locations == null || locations.size() < 2) {
            return List.of();
        }

        ArrayList<LatLng> allPoints = new ArrayList<>();

        for (int i = 0; i < locations.size() - 1; i++) {
            List<LatLng> segmentPoints = getRoutePoints(locations.get(i), locations.get(i + 1));
            if (allPoints.isEmpty()) {
                allPoints.addAll(segmentPoints);
            } else if (!segmentPoints.isEmpty()) {
                // Skip first point of new segment to avoid duplicate with last point
                allPoints.addAll(segmentPoints.subList(1, segmentPoints.size()));
            }
        }

        log.debug("Straight-line route computed with {} points", allPoints.size());
        return allPoints;
    }
}
