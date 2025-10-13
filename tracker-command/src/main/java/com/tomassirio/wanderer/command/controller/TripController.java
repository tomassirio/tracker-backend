package com.tomassirio.wanderer.command.controller;

import com.tomassirio.wanderer.command.dto.TripCreationRequest;
import com.tomassirio.wanderer.command.dto.TripResponse;
import com.tomassirio.wanderer.command.dto.TripUpdateRequest;
import com.tomassirio.wanderer.command.service.TripService;
import com.tomassirio.wanderer.commons.dto.TripDTO;
import com.tomassirio.wanderer.commons.security.CurrentUserId;
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

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<TripResponse> createTrip(
            @CurrentUserId UUID userId, @Valid @RequestBody TripCreationRequest request) {

        log.info("Received request to create trip: {} by user {}", request.name(), userId);

        TripDTO createdTrip = tripService.createTrip(userId, request);
        TripResponse response = new TripResponse(createdTrip.id());

        log.info("Successfully created trip with ID: {}", createdTrip.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<TripResponse> updateTrip(
            @PathVariable UUID id, @Valid @RequestBody TripUpdateRequest request) {
        log.info("Received request to update trip {} with name: {}", id, request.name());

        TripDTO updatedTrip = tripService.updateTrip(id, request);
        TripResponse response = new TripResponse(updatedTrip.id());

        log.info("Successfully updated trip with ID: {}", updatedTrip.id());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<Void> deleteTrip(@PathVariable UUID id) {
        log.info("Received request to delete trip: {}", id);

        tripService.deleteTrip(id);

        log.info("Successfully deleted trip with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
