package com.tomassirio.wanderer.commons.dto;

import com.tomassirio.wanderer.commons.domain.TripStatus;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import java.time.Instant;
import java.util.UUID;

public record TripDTO(
        UUID id,
        String name,
        UUID userId,
        TripStatus tripStatus,
        TripVisibility visibility,
        Integer updateRefresh,
        Instant startTimestamp,
        Instant endTimestamp,
        UUID tripPlanId,
        Instant creationTimestamp,
        Boolean enabled) {}
