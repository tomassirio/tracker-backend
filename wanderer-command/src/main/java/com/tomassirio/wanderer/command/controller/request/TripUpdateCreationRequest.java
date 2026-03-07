package com.tomassirio.wanderer.command.controller.request;

import com.tomassirio.wanderer.commons.domain.GeoLocation;
import com.tomassirio.wanderer.commons.domain.UpdateType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TripUpdateCreationRequest(
        @Schema(description = "Current location", required = true)
                @Valid
                @NotNull(message = "Location is required")
                GeoLocation location,
        @Schema(description = "Battery percentage", example = "75")
                @Min(value = 0, message = "Battery must be between 0 and 100")
                @Max(value = 100, message = "Battery must be between 0 and 100")
                Integer battery,
        @Schema(description = "Optional message or note", example = "Reached checkpoint")
                @Size(max = 500, message = "Message must not exceed 500 characters")
                String message,
        @Schema(
                        description =
                                "Type of update: REGULAR, DAY_START, DAY_END, TRIP_STARTED, or TRIP_ENDED",
                        example = "REGULAR",
                        allowableValues = {
                            "REGULAR",
                            "DAY_START",
                            "DAY_END",
                            "TRIP_STARTED",
                            "TRIP_ENDED"
                        })
                UpdateType updateType) {}
