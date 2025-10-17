package com.tomassirio.wanderer.query.controller;

import com.tomassirio.wanderer.commons.constants.ApiConstants;
import com.tomassirio.wanderer.commons.dto.FriendRequestResponse;
import com.tomassirio.wanderer.commons.security.CurrentUserId;
import com.tomassirio.wanderer.query.service.FriendRequestQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for querying friend request information.
 *
 * @author tomassirio
 * @since 0.4.0
 */
@RestController
@RequestMapping(ApiConstants.USERS_PATH + "/friend-requests")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Friend Requests Query", description = "Endpoints for querying friend requests")
public class FriendRequestQueryController {

    private final FriendRequestQueryService friendRequestQueryService;

    @GetMapping("/received")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "Get received friend requests",
            description = "Get all pending friend requests received by the current user")
    public ResponseEntity<List<FriendRequestResponse>> getReceivedFriendRequests(
            @Parameter(hidden = true) @CurrentUserId UUID userId) {
        log.info("Received request to get pending friend requests for user {}", userId);
        List<FriendRequestResponse> requests =
                friendRequestQueryService.getPendingReceivedRequests(userId);
        log.info("Found {} pending friend requests for user {}", requests.size(), userId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/sent")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "Get sent friend requests",
            description = "Get all pending friend requests sent by the current user")
    public ResponseEntity<List<FriendRequestResponse>> getSentFriendRequests(
            @Parameter(hidden = true) @CurrentUserId UUID userId) {
        log.info("Received request to get sent friend requests for user {}", userId);
        List<FriendRequestResponse> requests =
                friendRequestQueryService.getPendingSentRequests(userId);
        log.info("Found {} sent friend requests for user {}", requests.size(), userId);
        return ResponseEntity.ok(requests);
    }
}
