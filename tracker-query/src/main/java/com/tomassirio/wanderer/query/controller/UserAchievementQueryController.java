package com.tomassirio.wanderer.query.controller;

import com.tomassirio.wanderer.commons.constants.ApiConstants;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user achievement query operations. Handles user-specific achievement
 * retrieval requests.
 */
@RestController
@RequestMapping(
        value = ApiConstants.USERS_PATH,
        produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "User Achievements", description = "Endpoints for retrieving user achievements")
public class UserAchievementQueryController {

    private final AchievementQueryService achievementQueryService;

    @GetMapping(ApiConstants.MY_ACHIEVEMENTS_ENDPOINT)
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

    @GetMapping(ApiConstants.USER_ACHIEVEMENTS_ENDPOINT)
    @Operation(
            summary = "Get user achievements",
            description = "Retrieves all achievements unlocked by a specific user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user achievements")
    public ResponseEntity<List<UserAchievementDTO>> getUserAchievements(@PathVariable UUID userId) {
        return ResponseEntity.ok(achievementQueryService.getUserAchievements(userId));
    }
}
