package com.tomassirio.wanderer.commons.dto;

import com.tomassirio.wanderer.commons.domain.GeoLocation;
import java.time.Instant;

public record TripDetailsDTO(
        Instant startTimestamp,
        Instant endTimestamp,
        GeoLocation startLocation,
        GeoLocation endLocation) {}
