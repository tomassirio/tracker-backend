package com.tomassirio.wanderer.command.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record PromoteTripRequest(
        @Schema(
                        description = "Optional donation link for the promoted trip",
                        example = "https://example.com/donate",
                        maxLength = 500)
                @Size(max = 500, message = "Donation link must not exceed 500 characters")
                String donationLink) {}
