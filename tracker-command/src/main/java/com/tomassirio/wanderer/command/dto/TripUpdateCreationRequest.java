package com.tomassirio.wanderer.command.dto;

import com.tomassirio.wanderer.commons.domain.GeoLocation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TripUpdateCreationRequest(
        @Schema(description = "Current location", required = true)
                @NotNull(message = "Location is required")
                GeoLocation location,
        @Schema(description = "Battery percentage", example = "75")
                @Min(value = 0, message = "Battery must be between 0 and 100")
                @Max(value = 100, message = "Battery must be between 0 and 100")
                Integer battery,
        @Schema(description = "Optional message or note", example = "Reached checkpoint")
                @Size(max = 500, message = "Message must not exceed 500 characters")
                String message) {}
