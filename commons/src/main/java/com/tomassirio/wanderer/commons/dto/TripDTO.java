package com.tomassirio.wanderer.commons.dto;

import com.tomassirio.wanderer.commons.domain.TripVisibility;
import java.time.LocalDate;
import java.util.UUID;

public record TripDTO(
        UUID id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        Double totalDistance,
        LocationDTO startingLocation,
        LocationDTO endingLocation,
        TripVisibility visibility) {}
