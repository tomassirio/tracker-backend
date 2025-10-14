package com.tomassirio.wanderer.command.controller;

import com.tomassirio.wanderer.command.dto.TripCreationRequest;
import com.tomassirio.wanderer.command.dto.TripStatusRequest;
import com.tomassirio.wanderer.command.dto.TripUpdateCreationRequest;
import com.tomassirio.wanderer.command.dto.TripUpdateRequest;
import com.tomassirio.wanderer.command.dto.TripVisibilityRequest;
import com.tomassirio.wanderer.command.service.TripService;
import com.tomassirio.wanderer.command.service.TripUpdateService;
import com.tomassirio.wanderer.commons.dto.TripDTO;
import com.tomassirio.wanderer.commons.dto.TripUpdateDTO;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for trip command operations. Handles trip creation, update, and deletion
 * requests.
 *
 * @since 0.1.8
 */
@RestController
@RequestMapping("/api/1/trips")
@RequiredArgsConstructor
@Slf4j
public class TripController {

    private final TripService tripService;
    private final TripUpdateService tripUpdateService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<TripDTO> createTrip(
            @Parameter(hidden = true) @CurrentUserId UUID userId,
            @Valid @RequestBody TripCreationRequest request) {

        log.info("Received request to create trip: {} by user {}", request.name(), userId);

        TripDTO createdTrip = tripService.createTrip(userId, request);

        log.info("Successfully created trip with ID: {}", createdTrip.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTrip);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<TripDTO> updateTrip(
            @Parameter(hidden = true) @CurrentUserId UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody TripUpdateRequest request) {
        log.info(
                "Received request to update trip {} with name: {} by user {}",
                id,
                request.name(),
                userId);

        TripDTO updatedTrip = tripService.updateTrip(userId, id, request);

        log.info("Successfully updated trip with ID: {}", updatedTrip.id());
        return ResponseEntity.ok(updatedTrip);
    }

    @PatchMapping("/{id}/visibility")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<TripDTO> changeVisibility(
            @Parameter(hidden = true) @CurrentUserId UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody TripVisibilityRequest request) {
        log.info("Received request to change visibility for trip {} by user {}", id, userId);

        TripDTO updatedTrip = tripService.changeVisibility(userId, id, request.visibility());

        log.info("Successfully changed visibility for trip with ID: {}", updatedTrip.id());
        return ResponseEntity.ok(updatedTrip);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<TripDTO> changeStatus(
            @Parameter(hidden = true) @CurrentUserId UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody TripStatusRequest request) {
        log.info(
                "Received request to change status for trip {} to {} by user {}",
                id,
                request.status(),
                userId);

        TripDTO updatedTrip = tripService.changeStatus(userId, id, request.status());

        log.info("Successfully changed status for trip with ID: {}", updatedTrip.id());
        return ResponseEntity.ok(updatedTrip);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<Void> deleteTrip(
            @Parameter(hidden = true) @CurrentUserId UUID userId, @PathVariable UUID id) {
        log.info("Received request to delete trip {} by user {}", id, userId);

        tripService.deleteTrip(userId, id);

        log.info("Successfully deleted trip with ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{tripId}/updates")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<TripUpdateDTO> createTripUpdate(
            @Parameter(hidden = true) @CurrentUserId UUID userId,
            @PathVariable UUID tripId,
            @Valid @RequestBody TripUpdateCreationRequest request) {
        log.info("Received request to create trip update for trip {} by user {}", tripId, userId);

        TripUpdateDTO createdUpdate = tripUpdateService.createTripUpdate(userId, tripId, request);

        log.info("Successfully created trip update with ID: {}", createdUpdate.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUpdate);
    }
}
