package com.tomassirio.wanderer.command.service;

import com.tomassirio.wanderer.command.dto.LocationUpdateRequest;
import com.tomassirio.wanderer.command.repository.LocationRepository;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.commons.domain.Location;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final LocationRepository locationRepository;
    private final TripRepository tripRepository;

    /**
     * Creates a new location entry for the specified trip.
     *
     * @param tripId the ID of the trip
     * @param request the location update request
     * @return the created Location entity
     * @throws IllegalArgumentException if the trip doesn't exist or request is invalid
     */
    @Transactional
    public Location createLocationUpdate(UUID tripId, LocationUpdateRequest request) {
        log.info(
                "Processing location update for trip {} from source: {}", tripId, request.source());

        if (!tripRepository.existsById(tripId)) {
            log.warn("Trip not found with ID: {}", tripId);
            throw new IllegalArgumentException("Trip not found with ID: " + tripId);
        }

        Instant timestamp =
                (request.timestamp() == null || request.timestamp().trim().isEmpty())
                        ? Instant.now()
                        : Instant.parse(request.timestamp().trim());

        Location location = createLocationEntity(tripId, request, timestamp);

        Location savedLocation = locationRepository.save(location);

        log.info(
                "Successfully created location update with ID: {} at coordinates [{}, {}] for trip {}",
                savedLocation.getId(),
                savedLocation.getLatitude(),
                savedLocation.getLongitude(),
                tripId);

        return savedLocation;
    }

    /** Creates a Location entity from the request data using the builder pattern. */
    private Location createLocationEntity(
            UUID tripId, LocationUpdateRequest request, Instant timestamp) {
        return Location.builder()
                .trip(tripRepository.getReferenceById(tripId))
                .latitude(request.latitude())
                .longitude(request.longitude())
                .timestamp(timestamp)
                .altitude(request.altitude())
                .accuracy(request.accuracy())
                .batteryLevel(request.batteryLevel())
                .source(request.source())
                .build();
    }
}
