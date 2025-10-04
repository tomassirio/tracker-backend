package com.tomassirio.wanderer.commons.dto;

import java.time.Instant;
import java.util.UUID;

public record LocationDTO(
        UUID id,
        Double latitude,
        Double longitude,
        Instant timestamp,
        Double altitude,
        Double accuracy,
        Integer batteryLevel,
        String source) {}
