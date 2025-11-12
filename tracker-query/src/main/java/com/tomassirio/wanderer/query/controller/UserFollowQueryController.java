package com.tomassirio.wanderer.query.controller;

import com.tomassirio.wanderer.commons.constants.ApiConstants;
import com.tomassirio.wanderer.commons.dto.UserFollowResponse;
import com.tomassirio.wanderer.commons.security.CurrentUserId;
import com.tomassirio.wanderer.query.service.UserFollowService;
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
 * REST controller for querying user follows.
 *
 * @author tomassirio
 * @since 0.4.5
 */
@RestController
@RequestMapping(ApiConstants.USERS_PATH)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Follows Query", description = "Endpoints for querying user follows")
public class UserFollowQueryController {

    private final UserFollowService userFollowService;

    @GetMapping("/following")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "Get following list",
            description = "Retrieve the list of users that the current user is following")
    public ResponseEntity<List<UserFollowResponse>> getFollowing(
            @Parameter(hidden = true) @CurrentUserId UUID userId) {
        log.info("Received request to get following list for user: {}", userId);
        List<UserFollowResponse> following = userFollowService.getFollowing(userId);
        log.info(
                "Successfully retrieved {} users that user {} is following",
                following.size(),
                userId);
        return ResponseEntity.ok(following);
    }

    @GetMapping("/followers")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "Get followers list",
            description = "Retrieve the list of users that are following the current user")
    public ResponseEntity<List<UserFollowResponse>> getFollowers(
            @Parameter(hidden = true) @CurrentUserId UUID userId) {
        log.info("Received request to get followers list for user: {}", userId);
        List<UserFollowResponse> followers = userFollowService.getFollowers(userId);
        log.info("Successfully retrieved {} followers for user {}", followers.size(), userId);
        return ResponseEntity.ok(followers);
    }
}
