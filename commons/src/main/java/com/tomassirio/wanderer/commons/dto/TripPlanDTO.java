package com.tomassirio.wanderer.commons.dto;

import com.tomassirio.wanderer.commons.domain.GeoLocation;
import com.tomassirio.wanderer.commons.domain.TripPlanType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TripPlanDTO(
        UUID id,
        String name,
        TripPlanType planType,
        UUID userId,
        Instant createdTimestamp,
        LocalDate startDate,
        LocalDate endDate,
        GeoLocation startLocation,
        GeoLocation endLocation) {}
