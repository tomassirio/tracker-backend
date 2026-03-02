package com.tomassirio.wanderer.command.controller;

import com.tomassirio.wanderer.command.service.TripPlanPolylineService;
import com.tomassirio.wanderer.commons.constants.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for admin trip plan maintenance operations.
 *
 * <p>All endpoints in this controller require ADMIN role.
 *
 * @since 0.8.2
 */
@RestController
@RequestMapping(
        value = ApiConstants.ADMIN_TRIP_PLANS_PATH,
        produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Trip Plans", description = "Admin-only endpoints for trip plan maintenance")
public class AdminTripPlanController {

    private final TripPlanPolylineService tripPlanPolylineService;

    /**
     * Recomputes the encoded polyline for a trip plan from its start location, waypoints, and end
     * location.
     *
     * @param tripPlanId the ID of the trip plan to recompute
     * @return 204 No Content on success
     */
    @PostMapping(ApiConstants.ADMIN_TRIP_PLAN_RECOMPUTE_POLYLINE_ENDPOINT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Recompute trip plan polyline",
            description =
                    "Fully recomputes the encoded polyline for a trip plan from its "
                            + "start location, waypoints, and end location. "
                            + "Admin-only maintenance endpoint.")
    @ApiResponse(responseCode = "204", description = "Polyline recomputed successfully")
    @ApiResponse(
            responseCode = "404",
            description = "Trip plan not found",
            content = @Content(schema = @Schema(implementation = Map.class)))
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - valid JWT required",
            content = @Content)
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden - ADMIN role required",
            content = @Content)
    public ResponseEntity<Void> recomputePolyline(
            @Parameter(description = "Trip plan ID to recompute polyline for", required = true)
                    @PathVariable
                    UUID tripPlanId) {
        log.info("Admin recomputing polyline for trip plan {}", tripPlanId);
        tripPlanPolylineService.computePolyline(tripPlanId);
        return ResponseEntity.noContent().build();
    }
}
