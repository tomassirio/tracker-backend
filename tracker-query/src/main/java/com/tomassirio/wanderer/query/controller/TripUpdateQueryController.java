package com.tomassirio.wanderer.query.controller;

import com.tomassirio.wanderer.commons.constants.ApiConstants;
import com.tomassirio.wanderer.commons.dto.TripUpdateDTO;
import com.tomassirio.wanderer.query.service.TripUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for trip update query operations. Handles trip update retrieval requests.
 *
 * @since 0.4.2
 */
@RestController
@RequestMapping(ApiConstants.TRIPS_PATH)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Trip Update Queries", description = "Endpoints for retrieving trip update information")
public class TripUpdateQueryController {

    private final TripUpdateService tripUpdateService;

    @GetMapping("/updates/{id}")
    @Operation(
            summary = "Get trip update by ID",
            description = "Retrieves a specific trip update by its ID")
    public ResponseEntity<TripUpdateDTO> getTripUpdate(@PathVariable UUID id) {
        log.info("Received request to retrieve trip update: {}", id);

        TripUpdateDTO tripUpdate = tripUpdateService.getTripUpdate(id);

        log.info("Successfully retrieved trip update with ID: {}", tripUpdate.id());
        return ResponseEntity.ok(tripUpdate);
    }

    @GetMapping(ApiConstants.TRIP_UPDATES_ENDPOINT)
    @Operation(
            summary = "Get all trip updates for a trip",
            description =
                    "Retrieves all trip updates for a specific trip, ordered by timestamp descending (most recent first)")
    public ResponseEntity<List<TripUpdateDTO>> getTripUpdatesForTrip(@PathVariable UUID tripId) {
        log.info("Received request to retrieve trip updates for trip: {}", tripId);

        List<TripUpdateDTO> tripUpdates = tripUpdateService.getTripUpdatesForTrip(tripId);

        log.info("Successfully retrieved {} trip updates for trip {}", tripUpdates.size(), tripId);
        return ResponseEntity.ok(tripUpdates);
    }
}
