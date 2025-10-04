package com.tomassirio.wanderer.query.controller;

import com.tomassirio.wanderer.commons.dto.TripDTO;
import com.tomassirio.wanderer.query.service.TripService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/1/trips")
@AllArgsConstructor
public class TripController {

    private final TripService tripService;

    @GetMapping("/{id}")
    public TripDTO getTrip(@PathVariable UUID id) {
        return tripService.getTrip(id);
    }

    @GetMapping
    public List<TripDTO> getAllTrips() {
        return tripService.getAllTrips();
    }
}
