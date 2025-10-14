package com.tomassirio.wanderer.command.controller;

import com.tomassirio.wanderer.command.dto.TripPlanCreationRequest;
import com.tomassirio.wanderer.command.dto.TripPlanUpdateRequest;
import com.tomassirio.wanderer.command.service.TripPlanService;
import com.tomassirio.wanderer.commons.dto.TripPlanDTO;
import com.tomassirio.wanderer.commons.security.CurrentUserId;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
 * @since 0.2.0
 */
@RestController
@RequestMapping("/api/1/trips/plans")
@RequiredArgsConstructor
@Slf4j
public class TripPlanController {

    private final TripPlanService tripPlanService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<TripPlanDTO> createTripPlan(
            @Parameter(hidden = true) @CurrentUserId UUID userId,
            @Valid @RequestBody TripPlanCreationRequest request) {
        log.info("Received request to create trip plan by user {}", userId);

        TripPlanDTO createdPlan = tripPlanService.createTripPlan(userId, request);

        log.info("Successfully created trip plan with ID: {}", createdPlan.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPlan);
    }

    @PutMapping("/{planId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<TripPlanDTO> updateTripPlan(
            @Parameter(hidden = true) @CurrentUserId UUID userId,
            @PathVariable UUID planId,
            @Valid @RequestBody TripPlanUpdateRequest request) {
        log.info("Received request to update trip plan {} by user {}", planId, userId);

        TripPlanDTO updatedPlan = tripPlanService.updateTripPlan(userId, planId, request);

        log.info("Successfully updated trip plan with ID: {}", updatedPlan.id());
        return ResponseEntity.ok(updatedPlan);
    }

    @DeleteMapping("/{planId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<Void> deleteTripPlan(
            @Parameter(hidden = true) @CurrentUserId UUID userId, @PathVariable UUID planId) {
        log.info("Received request to delete trip plan {} by user {}", planId, userId);

        tripPlanService.deleteTripPlan(userId, planId);

        log.info("Successfully deleted trip plan with ID: {}", planId);
        return ResponseEntity.noContent().build();
    }
}
