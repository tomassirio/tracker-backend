package com.tomassirio.wanderer.query.controller;

import com.tomassirio.wanderer.commons.constants.ApiConstants;
import com.tomassirio.wanderer.commons.dto.AchievementDTO;
import com.tomassirio.wanderer.commons.dto.UserAchievementDTO;
import com.tomassirio.wanderer.commons.security.CurrentUserId;
import com.tomassirio.wanderer.query.service.AchievementQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for achievement query operations. Handles achievement retrieval requests. */
@RestController
@RequestMapping(ApiConstants.ACHIEVEMENTS_PATH)
@RequiredArgsConstructor
@Tag(name = "Achievement Queries", description = "Endpoints for retrieving achievement information")
public class AchievementQueryController {

    private final AchievementQueryService achievementQueryService;

    @GetMapping
    @Operation(
            summary = "Get all available achievements",
            description = "Retrieves all achievements that can be unlocked in the system")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all achievements")
    public ResponseEntity<List<AchievementDTO>> getAvailableAchievements() {
        return ResponseEntity.ok(achievementQueryService.getAvailableAchievements());
    }

    @GetMapping(ApiConstants.USER_ACHIEVEMENTS_ENDPOINT)
    @Operation(
            summary = "Get user achievements",
            description = "Retrieves all achievements unlocked by a specific user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user achievements")
    public ResponseEntity<List<UserAchievementDTO>> getUserAchievements(@PathVariable UUID userId) {
        return ResponseEntity.ok(achievementQueryService.getUserAchievements(userId));
    }

    @GetMapping(ApiConstants.USER_ACHIEVEMENTS_ME_ENDPOINT)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "Get current user achievements",
            description = "Retrieves all achievements unlocked by the currently authenticated user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user achievements")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<List<UserAchievementDTO>> getMyAchievements(
            @Parameter(hidden = true) @CurrentUserId UUID userId) {
        return ResponseEntity.ok(achievementQueryService.getUserAchievements(userId));
    }

    @GetMapping(ApiConstants.TRIP_ACHIEVEMENTS_ENDPOINT)
    @Operation(
            summary = "Get user achievements for a trip",
            description = "Retrieves all achievements unlocked by a user for a specific trip")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved trip achievements")
    public ResponseEntity<List<UserAchievementDTO>> getUserAchievementsByTrip(
            @PathVariable UUID userId, @PathVariable UUID tripId) {
        return ResponseEntity.ok(achievementQueryService.getUserAchievementsByTrip(userId, tripId));
    }
}
