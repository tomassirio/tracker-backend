package com.tomassirio.wanderer.command.dto;

import com.tomassirio.wanderer.commons.domain.TripVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record TripVisibilityRequest(
        @Schema(
                        description = "Trip visibility",
                        example = "PUBLIC",
                        allowableValues = {"PRIVATE", "PROTECTED", "PUBLIC"})
                @NotNull(message = "Visibility is required")
                TripVisibility visibility) {}
