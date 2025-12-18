package com.tomassirio.wanderer.command.dto;

import com.tomassirio.wanderer.commons.domain.TripVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record TripFromPlanCreationRequest(
        @Schema(
                        description = "ID of the trip plan to create a trip from",
                        example = "123e4567-e89b-12d3-a456-426614174000")
                @NotNull(message = "Trip plan ID is required")
                UUID tripPlanId,
        @Schema(
                        description = "Trip visibility",
                        example = "PUBLIC",
                        allowableValues = {"PRIVATE", "PROTECTED", "PUBLIC"})
                @NotNull(message = "Visibility is required")
                TripVisibility visibility) {}
