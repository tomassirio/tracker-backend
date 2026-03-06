package com.tomassirio.wanderer.command.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import org.hibernate.validator.constraints.URL;

public record PromoteTripRequest(
        @Schema(
                        description = "Optional donation link for the promoted trip",
                        example = "https://example.com/donate",
                        maxLength = 500)
                @Size(max = 500, message = "Donation link must not exceed 500 characters")
                @URL(message = "Donation link must be a valid URL")
                String donationLink,
        @Schema(
                        description =
                                "Whether this trip is pre-announced (coming soon) to enable a"
                                        + " countdown",
                        example = "true")
                Boolean isPreAnnounced,
        @Schema(
                        description =
                                "The date from which the countdown should start, typically the"
                                        + " planned trip start date",
                        example = "2025-06-01T08:00:00Z")
                Instant countdownStartDate) {}
