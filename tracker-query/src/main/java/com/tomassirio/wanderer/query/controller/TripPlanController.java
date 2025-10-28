package com.tomassirio.wanderer.query.controller;

import com.tomassirio.wanderer.commons.constants.ApiConstants;
import com.tomassirio.wanderer.commons.dto.TripPlanDTO;
import com.tomassirio.wanderer.commons.security.CurrentUserId;
import com.tomassirio.wanderer.query.service.TripPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * REST controller for trip plan query operations.
 *
 * @since 0.3.0
 */
@RestController
@RequestMapping(ApiConstants.TRIP_PLANS_PATH)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Trip Plan Queries", description = "Endpoints for retrieving trip plan information")
public class TripPlanController {

    private final TripPlanService tripPlanService;

    @GetMapping(ApiConstants.TRIP_PLAN_BY_ID_ENDPOINT)
    @Operation(
            summary = "Get trip plan by ID",
            description = "Retrieves a specific trip plan by its ID")
    public ResponseEntity<TripPlanDTO> getTripPlan(@PathVariable UUID planId) {
        log.info("Received request to retrieve trip plan: {}", planId);

        TripPlanDTO tripPlan = tripPlanService.getTripPlan(planId);

        log.info("Successfully retrieved trip plan with ID: {}", tripPlan.id());
        return ResponseEntity.ok(tripPlan);
    }

    @GetMapping(ApiConstants.ME_SUFFIX)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "Get my trip plans",
            description = "Retrieves all trip plans belonging to the authenticated user")
    public ResponseEntity<List<TripPlanDTO>> getMyTripPlans(
            @Parameter(hidden = true) @CurrentUserId UUID userId) {
        log.info("Received request to retrieve trip plans for current user: {}", userId);

        List<TripPlanDTO> tripPlans = tripPlanService.getTripPlansForUser(userId);

        log.info("Successfully retrieved {} trip plans for user {}", tripPlans.size(), userId);
        return ResponseEntity.ok(tripPlans);
    }
}
