package com.tomassirio.wanderer.query.controller;

import com.tomassirio.wanderer.commons.dto.TripDTO;
import com.tomassirio.wanderer.commons.security.CurrentUserId;
import com.tomassirio.wanderer.query.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for trip query operations. Handles trip retrieval requests.
 *
 * @since 0.1.8
 */
@RestController
@RequestMapping("/api/1/trips")
@RequiredArgsConstructor
@Slf4j
public class TripController {

    private final TripService tripService;

    @GetMapping("/{id:[0-9a-fA-F\\-]{36}}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<TripDTO> getTrip(@PathVariable UUID id) {
        log.info("Received request to retrieve trip: {}", id);

        TripDTO trip = tripService.getTrip(id);

        log.info("Successfully retrieved trip with ID: {}", trip.id());
        return ResponseEntity.ok(trip);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<TripDTO>> getAllTrips() {
        log.info("Received request to retrieve all trips");

        List<TripDTO> trips = tripService.getAllTrips();

        log.info("Successfully retrieved {} trips", trips.size());
        return ResponseEntity.ok(trips);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Get trips for current authenticated user")
    public ResponseEntity<List<TripDTO>> getMyTrips(
            @Parameter(hidden = true) @CurrentUserId UUID userId) {
        log.info("Received request to retrieve trips for current user: {}", userId);

        List<TripDTO> trips = tripService.getTripsForUser(userId);

        log.info("Successfully retrieved {} trips for user {}", trips.size(), userId);
        return ResponseEntity.ok(trips);
    }
}
