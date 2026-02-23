package com.tomassirio.wanderer.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Response model for a promoted trip")
public record PromotedTripResponse(
        @Schema(description = "Unique identifier of the promoted trip record") String id,
        @Schema(description = "Unique identifier of the trip") String tripId,
        @Schema(description = "Name of the promoted trip") String tripName,
        @Schema(description = "Optional donation link for the promoted trip") String donationLink,
        @Schema(description = "Unique identifier of the admin who promoted the trip")
                String promotedBy,
        @Schema(description = "Username of the admin who promoted the trip")
                String promotedByUsername,
        @Schema(description = "Unique identifier of the trip owner") String tripOwnerId,
        @Schema(description = "Username of the trip owner") String tripOwnerUsername,
        @Schema(description = "Timestamp when the trip was promoted") Instant promotedAt) {}
