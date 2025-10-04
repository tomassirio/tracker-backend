package com.tomassirio.wanderer.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record LocationRequest(
        @Schema(description = "Latitude coordinate", example = "39.7392")
                @NotNull(message = "Latitude is required")
                @DecimalMin(
                        value = "-90.0",
                        message = "Latitude must be between -90 and 90 degrees")
                @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90 degrees")
                Double latitude,
        @Schema(description = "Longitude coordinate", example = "-104.9903")
                @NotNull(message = "Longitude is required")
                @DecimalMin(
                        value = "-180.0",
                        message = "Longitude must be between -180 and 180 degrees")
                @DecimalMax(
                        value = "180.0",
                        message = "Longitude must be between -180 and 180 degrees")
                Double longitude,
        @Schema(description = "Altitude in meters (optional)", example = "1609.3")
                @DecimalMin(value = "-1000.0", message = "Altitude seems too low")
                @DecimalMax(value = "10000.0", message = "Altitude seems too high")
                Double altitude) {}
