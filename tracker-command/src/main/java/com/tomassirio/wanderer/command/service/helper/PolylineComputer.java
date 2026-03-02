package com.tomassirio.wanderer.command.service.helper;

import com.google.maps.model.LatLng;
import com.tomassirio.wanderer.command.service.RouteService;
import com.tomassirio.wanderer.commons.domain.GeoLocation;
import com.tomassirio.wanderer.commons.domain.Polylineable;
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Shared helper that computes an encoded polyline from a list of {@link GeoLocation}s and applies
 * it to any {@link Polylineable} entity.
 *
 * <p>Eliminates duplication between trip and trip plan polyline computation. Both follow the same
 * algorithm: filter nulls → compute route → encode → set on entity → persist.
 *
 * @since 0.8.2
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PolylineComputer {

    private final RouteService routeService;

    /**
     * Computes and applies an encoded polyline to the given entity.
     *
     * @param entity the polylineable entity to update
     * @param locations the raw (unfiltered) list of locations
     * @param save callback to persist the entity after updating
     * @param <T> the entity type
     */
    public <T extends Polylineable> void computeAndApply(
            T entity, List<GeoLocation> locations, Consumer<T> save) {
        List<GeoLocation> validLocations =
                locations.stream()
                        .filter(loc -> loc != null && loc.getLat() != null && loc.getLon() != null)
                        .toList();

        if (validLocations.size() < 2) {
            entity.setEncodedPolyline(null);
            entity.setPolylineUpdatedAt(null);
            save.accept(entity);
            log.debug(
                    "{} {} has fewer than 2 valid locations, polyline cleared",
                    entity.getClass().getSimpleName(),
                    entity.getId());
            return;
        }

        List<LatLng> routePoints = routeService.getFullRoutePoints(validLocations);
        String encoded = PolylineCodec.encode(routePoints);

        entity.setEncodedPolyline(encoded);
        entity.setPolylineUpdatedAt(Instant.now());
        save.accept(entity);

        log.info(
                "Polyline computed for {} {}. Locations: {}, Points: {}",
                entity.getClass().getSimpleName(),
                entity.getId(),
                validLocations.size(),
                routePoints.size());
    }
}
