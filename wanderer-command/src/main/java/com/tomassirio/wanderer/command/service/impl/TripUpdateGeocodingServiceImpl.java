package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.repository.TripUpdateRepository;
import com.tomassirio.wanderer.command.service.GeocodingService;
import com.tomassirio.wanderer.command.service.TripUpdateGeocodingService;
import com.tomassirio.wanderer.commons.domain.TripUpdate;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link TripUpdateGeocodingService} that re-geocodes every trip update for a
 * given trip, updating only the {@code city} and {@code country} columns.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TripUpdateGeocodingServiceImpl implements TripUpdateGeocodingService {

    private final TripRepository tripRepository;
    private final TripUpdateRepository tripUpdateRepository;
    private final GeocodingService geocodingService;

    @Override
    @Transactional
    public void recomputeGeocoding(UUID tripId) {
        tripRepository
                .findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("Trip not found: " + tripId));

        List<TripUpdate> updates = tripUpdateRepository.findByTripIdOrderByTimestampAsc(tripId);

        for (TripUpdate update : updates) {
            GeocodingService.GeocodingResult result =
                    geocodingService.reverseGeocode(update.getLocation());
            if (result != null) {
                update.setCity(result.city());
                update.setCountry(result.country());
            } else {
                update.setCity(null);
                update.setCountry(null);
            }
        }

        tripUpdateRepository.saveAll(updates);
        log.info("Recomputed geocoding for {} trip updates of trip {}", updates.size(), tripId);
    }
}
