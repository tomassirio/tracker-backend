package com.tomassirio.wanderer.command.controller;

import com.tomassirio.wanderer.command.dto.TripCreationRequest;
import com.tomassirio.wanderer.command.dto.TripUpdateRequest;
import com.tomassirio.wanderer.command.service.TripService;
import com.tomassirio.wanderer.commons.dto.TripDTO;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/1/trips")
@AllArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    public TripDTO createTrip(@Valid @RequestBody TripCreationRequest request) {
        return tripService.createTrip(request);
    }

    @PutMapping("/{id}")
    public TripDTO updateTrip(
            @PathVariable UUID id, @Valid @RequestBody TripUpdateRequest request) {
        return tripService.updateTrip(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteTrip(@PathVariable UUID id) {
        tripService.deleteTrip(id);
    }
}
