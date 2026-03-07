package com.tomassirio.wanderer.command.controller.request;

import com.tomassirio.wanderer.commons.domain.TripModality;
import io.swagger.v3.oas.annotations.media.Schema;

public record TripSettingsRequest(
        @Schema(description = "Interval in seconds for automatic location updates", example = "60")
                Integer updateRefresh,
        @Schema(description = "Whether automatic updates are enabled", example = "true")
                Boolean automaticUpdates,
        @Schema(
                        description = "Trip modality",
                        example = "SIMPLE",
                        allowableValues = {"SIMPLE", "MULTI_DAY"})
                TripModality tripModality) {}
