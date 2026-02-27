package com.tomassirio.wanderer.query.controller;

import com.tomassirio.wanderer.commons.constants.ApiConstants;
import com.tomassirio.wanderer.query.dto.PromotedTripResponse;
import com.tomassirio.wanderer.query.service.PromotedTripQueryService;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for promoted trip query operations. Handles promoted trip retrieval requests.
 * These endpoints are public so all users can see promoted trips.
 *
 * @author tomassirio
 * @since 0.5.0
 */
@RestController
@RequiredArgsConstructor
@Tag(
        name = "Promoted Trip Queries",
        description = "Public endpoints for retrieving information about promoted trips")
public class PromotedTripQueryController {

    private final PromotedTripQueryService promotedTripQueryService;

    @GetMapping(
            value = ApiConstants.PROMOTED_TRIPS_PATH + ApiConstants.ALL_PROMOTED_TRIPS_ENDPOINT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get all promoted trips",
            description = "Retrieves a list of all currently promoted trips. Public endpoint.")
    @ApiResponse(
            responseCode = "200",
            description = "List of promoted trips retrieved successfully")
    public ResponseEntity<List<PromotedTripResponse>> getAllPromotedTrips() {
        return ResponseEntity.ok(promotedTripQueryService.getAllPromotedTrips());
    }

    @GetMapping(
            value = ApiConstants.TRIPS_PATH + ApiConstants.TRIP_PROMOTION_INFO_ENDPOINT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get promotion info for a trip",
            description =
                    "Retrieves promotion information for a specific trip, including donation link if available. Public endpoint.")
    @ApiResponse(responseCode = "200", description = "Promotion info retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Trip or promotion not found")
    public ResponseEntity<PromotedTripResponse> getPromotionInfo(@PathVariable UUID id) {
        return ResponseEntity.ok(promotedTripQueryService.getPromotionByTripId(id));
    }
}
