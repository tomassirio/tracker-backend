package com.tomassirio.wanderer.command.controller.request;

import com.tomassirio.wanderer.commons.domain.TripStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record TripStatusRequest(
        @Schema(
                        description = "Trip status",
                        example = "IN_PROGRESS",
                        allowableValues = {"CREATED", "IN_PROGRESS", "PAUSED", "FINISHED"})
                @NotNull(message = "Status is required")
                TripStatus status) {}
