package com.tomassirio.wanderer.query.controller;

import com.tomassirio.wanderer.commons.constants.ApiConstants;
import com.tomassirio.wanderer.query.dto.PromotedTripResponse;
import com.tomassirio.wanderer.query.service.PromotedTripQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for promoted trip query operations. Handles promoted trip retrieval requests.
 *
 * @author tomassirio
 * @since 0.5.0
 */
@RestController
@RequiredArgsConstructor
@Tag(
        name = "Promoted Trip Queries",
        description = "Endpoints for retrieving information about promoted trips")
public class PromotedTripQueryController {

    private final PromotedTripQueryService promotedTripQueryService;

    @GetMapping(ApiConstants.PROMOTED_TRIPS_PATH + ApiConstants.ALL_PROMOTED_TRIPS_ENDPOINT)
    @Operation(
            summary = "Get all promoted trips",
            description = "Retrieves a list of all currently promoted trips")
    public ResponseEntity<List<PromotedTripResponse>> getAllPromotedTrips() {
        return ResponseEntity.ok(promotedTripQueryService.getAllPromotedTrips());
    }

    @GetMapping(ApiConstants.TRIPS_PATH + ApiConstants.TRIP_PROMOTION_INFO_ENDPOINT)
    @Operation(
            summary = "Get promotion info for a trip",
            description =
                    "Retrieves promotion information for a specific trip, including donation link if available")
    public ResponseEntity<PromotedTripResponse> getPromotionInfo(@PathVariable UUID id) {
        return ResponseEntity.ok(promotedTripQueryService.getPromotionByTripId(id));
    }
}
