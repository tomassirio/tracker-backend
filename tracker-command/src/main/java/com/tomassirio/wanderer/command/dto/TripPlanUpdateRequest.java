package com.tomassirio.wanderer.command.dto;

import com.tomassirio.wanderer.commons.domain.GeoLocation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TripPlanUpdateRequest(
        @Schema(description = "Plan name", example = "Europe Summer Trip")
                @NotBlank(message = "Plan name is required")
                @Size(
                        min = 3,
                        max = 100,
                        message = "Plan name must be between 3 and 100 characters")
                String name,
        @Schema(description = "Trip start date", example = "2025-10-20")
                @NotNull(message = "Start date is required")
                LocalDate startDate,
        @Schema(description = "Trip end date", example = "2025-10-25")
                @NotNull(message = "End date is required")
                LocalDate endDate,
        @Schema(description = "Starting location") @NotNull(message = "Start location is required")
                GeoLocation startLocation,
        @Schema(description = "Ending location") @NotNull(message = "End location is required")
                GeoLocation endLocation) {}
