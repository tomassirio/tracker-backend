package com.tomassirio.wanderer.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

/**
 * Response model for admin user listing with statistics.
 *
 * @since 0.5.2
 */
@Schema(description = "User details with statistics for admin view")
public record UserAdminResponse(
        @Schema(description = "Unique identifier of the user") UUID id,
        @Schema(description = "Username of the user") String username,
        @Schema(description = "Number of friends the user has") long friendsCount,
        @Schema(description = "Number of followers the user has") long followersCount,
        @Schema(description = "Number of trips the user has created") long tripsCount,
        @Schema(description = "Timestamp when the user was created") Instant createdAt) {}
