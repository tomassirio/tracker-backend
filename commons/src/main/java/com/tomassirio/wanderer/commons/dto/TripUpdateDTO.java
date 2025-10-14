package com.tomassirio.wanderer.commons.dto;

import com.tomassirio.wanderer.commons.domain.GeoLocation;
import com.tomassirio.wanderer.commons.domain.Reactions;
import java.time.Instant;
import java.util.UUID;

public record TripUpdateDTO(
        UUID id,
        UUID tripId,
        GeoLocation location,
        Integer battery,
        String message,
        Reactions reactions,
        Instant timestamp) {}
