package com.tomassirio.wanderer.query.controller;

import com.tomassirio.wanderer.commons.constants.ApiConstants;
import com.tomassirio.wanderer.commons.dto.UserAchievementDTO;
import com.tomassirio.wanderer.query.service.AchievementQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for trip achievement query operations. Handles trip-specific achievement
 * retrieval requests.
 */
@RestController
@RequestMapping(value = ApiConstants.TRIPS_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Trip Achievements", description = "Endpoints for retrieving trip achievements")
public class TripAchievementQueryController {

    private final AchievementQueryService achievementQueryService;

    @GetMapping(ApiConstants.TRIP_ACHIEVEMENTS_BY_ID_ENDPOINT)
    @Operation(
            summary = "Get trip achievements",
            description = "Retrieves all achievements unlocked for a specific trip")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved trip achievements")
    public ResponseEntity<List<UserAchievementDTO>> getTripAchievements(@PathVariable UUID tripId) {
        return ResponseEntity.ok(achievementQueryService.getTripAchievements(tripId));
    }
}
