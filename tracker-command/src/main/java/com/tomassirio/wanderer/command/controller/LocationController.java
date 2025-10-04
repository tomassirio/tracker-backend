package com.tomassirio.wanderer.command.controller;

import com.tomassirio.wanderer.command.dto.LocationCreationResponse;
import com.tomassirio.wanderer.command.dto.LocationUpdateRequest;
import com.tomassirio.wanderer.command.service.LocationService;
import com.tomassirio.wanderer.commons.domain.Location;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/1/{tripId}/location")
@RequiredArgsConstructor
@Slf4j
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<LocationCreationResponse> submitLocationUpdate(
            @PathVariable UUID tripId, @Valid @RequestBody LocationUpdateRequest request) {

        log.info("Received location update for trip {} from source: {}", tripId, request.source());

        Location createdLocation = locationService.createLocationUpdate(tripId, request);
        LocationCreationResponse response = new LocationCreationResponse(createdLocation.getId());

        log.info("Successfully processed location update with ID: {}", createdLocation.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
