package com.tomassirio.wanderer.query.controller;

import com.tomassirio.wanderer.commons.constants.ApiConstants;
import com.tomassirio.wanderer.commons.dto.AchievementDTO;
import com.tomassirio.wanderer.query.service.AchievementQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for achievement query operations. Handles achievement retrieval requests. */
@RestController
@RequestMapping(
        value = ApiConstants.ACHIEVEMENTS_PATH,
        produces = MediaType.APPLICATION_JSON_VALUE)
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
}
