package com.tomassirio.wanderer.command.controller;

import com.tomassirio.wanderer.command.controller.request.TripPlanCreationRequest;
import com.tomassirio.wanderer.command.controller.request.TripPlanUpdateRequest;
import com.tomassirio.wanderer.command.service.TripPlanService;
import com.tomassirio.wanderer.commons.constants.ApiConstants;
import com.tomassirio.wanderer.commons.security.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for trip plan command operations. Handles trip plan creation, update, and
 * deletion requests.
 *
 * @since 0.3.0
 */
@RestController
@RequestMapping(
        value = ApiConstants.TRIP_PLANS_PATH,
        produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Trip Plans", description = "Endpoints for managing trip plans and waypoints")
public class TripPlanController {

    private final TripPlanService tripPlanService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "Create a trip plan",
            description =
                    "Creates a new trip plan with waypoints and route details. Returns 202 Accepted with the trip plan ID as the operation completes asynchronously.")
    public ResponseEntity<UUID> createTripPlan(
            @Parameter(hidden = true) @CurrentUserId UUID userId,
            @Valid @RequestBody TripPlanCreationRequest request) {
        log.info("Received request to create trip plan by user {}", userId);

        UUID planId = tripPlanService.createTripPlan(userId, request);

        log.info("Accepted trip plan creation request with ID: {}", planId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(planId);
    }

    @PutMapping(
            value = ApiConstants.TRIP_PLAN_BY_ID_ENDPOINT,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "Update a trip plan",
            description =
                    "Updates an existing trip plan with new waypoints or route information. Returns 202 Accepted with the trip plan ID as the operation completes asynchronously.")
    public ResponseEntity<UUID> updateTripPlan(
            @Parameter(hidden = true) @CurrentUserId UUID userId,
            @PathVariable UUID planId,
            @Valid @RequestBody TripPlanUpdateRequest request) {
        log.info("Received request to update trip plan {} by user {}", planId, userId);

        UUID updatedPlanId = tripPlanService.updateTripPlan(userId, planId, request);

        log.info("Accepted trip plan update request for ID: {}", updatedPlanId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(updatedPlanId);
    }

    @DeleteMapping(ApiConstants.TRIP_PLAN_BY_ID_ENDPOINT)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "Delete a trip plan",
            description =
                    "Deletes a trip plan and all associated waypoints. Returns 202 Accepted as the operation completes asynchronously.")
    public ResponseEntity<Void> deleteTripPlan(
            @Parameter(hidden = true) @CurrentUserId UUID userId, @PathVariable UUID planId) {
        log.info("Received request to delete trip plan {} by user {}", planId, userId);

        tripPlanService.deleteTripPlan(userId, planId);

        log.info("Accepted trip plan deletion request for ID: {}", planId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
