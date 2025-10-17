package com.tomassirio.wanderer.query.controller;

import com.tomassirio.wanderer.commons.constants.ApiConstants;
import com.tomassirio.wanderer.commons.dto.FriendshipResponse;
import com.tomassirio.wanderer.commons.security.CurrentUserId;
import com.tomassirio.wanderer.query.service.FriendshipQueryService;
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
 * REST controller for querying friendship information.
 *
 * @author tomassirio
 * @since 0.4.0
 */
@RestController
@RequestMapping(ApiConstants.FRIENDS_PATH)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Friends Query", description = "Endpoints for querying friends")
public class FriendshipQueryController {

    private final FriendshipQueryService friendshipQueryService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Get friends", description = "Get all friends of the current user")
    public ResponseEntity<List<FriendshipResponse>> getFriends(
            @Parameter(hidden = true) @CurrentUserId UUID userId) {
        log.info("Received request to get friends for user {}", userId);
        List<FriendshipResponse> friends = friendshipQueryService.getFriends(userId);
        log.info("Found {} friends for user {}", friends.size(), userId);
        return ResponseEntity.ok(friends);
    }
}
