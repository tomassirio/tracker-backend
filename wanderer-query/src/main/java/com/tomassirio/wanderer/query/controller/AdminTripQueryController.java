package com.tomassirio.wanderer.query.controller;

import com.tomassirio.wanderer.commons.constants.ApiConstants;
import com.tomassirio.wanderer.commons.dto.TripMaintenanceStatsDTO;
import com.tomassirio.wanderer.query.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for admin trip query operations. Provides maintenance statistics for the admin
 * dashboard, including polyline and geocoding coverage metrics.
 *
 * <p>All endpoints in this controller require ADMIN role.
 *
 * @since 0.9.0
 */
@RestController
@RequestMapping(value = ApiConstants.ADMIN_TRIPS_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Admin Trip Queries",
        description =
                "Admin-only query endpoints for trip data management"
                        + " and maintenance statistics")
public class AdminTripQueryController {

    private final TripService tripService;

    /**
     * Returns maintenance statistics for all trips, including polyline coverage and geocoding
     * coverage.
     *
     * @return trip maintenance statistics
     */
    @GetMapping(ApiConstants.ADMIN_TRIPS_STATS_ENDPOINT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get trip maintenance statistics",
            description =
                    "Returns overview statistics for trip data management, including "
                            + "polyline coverage (total, with polyline, missing) and "
                            + "geocoding coverage (total updates, with geocoding, missing).")
    @ApiResponse(
            responseCode = "200",
            description = "Statistics retrieved successfully",
            content = @Content(schema = @Schema(implementation = TripMaintenanceStatsDTO.class)))
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - valid JWT required",
            content = @Content)
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden - ADMIN role required",
            content = @Content)
    public ResponseEntity<TripMaintenanceStatsDTO> getTripMaintenanceStats() {
        log.info("Admin retrieving trip maintenance statistics");
        TripMaintenanceStatsDTO stats = tripService.getTripMaintenanceStats();
        log.info(
                "Retrieved trip maintenance stats: {} trips, {} updates ({} geocoded)",
                stats.totalTrips(),
                stats.totalUpdates(),
                stats.updatesWithGeocoding());
        return ResponseEntity.ok(stats);
    }
}
