package com.tomassirio.wanderer.command.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record TripSettingsRequest(
        @Schema(description = "Interval in seconds for automatic location updates", example = "60")
                Integer updateRefresh,
        @Schema(description = "Whether automatic updates are enabled", example = "true")
                Boolean automaticUpdates) {}
