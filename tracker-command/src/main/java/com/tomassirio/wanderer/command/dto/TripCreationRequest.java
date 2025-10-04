package com.tomassirio.wanderer.command.dto;

import com.tomassirio.wanderer.commons.domain.TripVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TripCreationRequest(
        @Schema(description = "Trip name", example = "Summer Road Trip 2025")
                @NotBlank(message = "Trip name is required")
                @Size(
                        min = 3,
                        max = 100,
                        message = "Trip name must be between 3 and 100 characters")
                String name,
        @Schema(description = "Trip start date", example = "2025-06-01")
                @NotNull(message = "Start date is required")
                @PastOrPresent(message = "Start date cannot be in the future")
                LocalDate startDate,
        @Schema(description = "Trip end date", example = "2025-06-15")
                @Future(message = "End date must be in the future")
                LocalDate endDate,
        @Schema(description = "Total distance in kilometers", example = "1250.5")
                @DecimalMin(value = "0.0", message = "Total distance must be positive")
                @DecimalMax(value = "50000.0", message = "Total distance seems unrealistic")
                Double totalDistance,
        @Schema(description = "Starting location of the trip with coordinates")
                @NotNull(message = "Starting location is required")
                @Valid
                LocationRequest startingLocation,
        @Schema(description = "Ending location of the trip with coordinates") @Valid
                LocationRequest endingLocation,
        @Schema(description = "Trip visibility", example = "PUBLIC")
                @NotNull(message = "Visibility is required")
                TripVisibility visibility) {}
