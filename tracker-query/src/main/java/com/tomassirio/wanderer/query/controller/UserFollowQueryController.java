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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for querying user follows.
 *
 * @author tomassirio
 * @since 0.4.5
 */
@RestController
@RequestMapping(value = ApiConstants.USERS_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Follows Query", description = "Endpoints for querying user follows")
public class UserFollowQueryController {

    private final UserFollowService userFollowService;

    @GetMapping(ApiConstants.FOLLOWING_ME_ENDPOINT)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "Get my following list",
            description = "Retrieve the list of users that the current user is following")
    public ResponseEntity<List<UserFollowResponse>> getMyFollowing(
            @Parameter(hidden = true) @CurrentUserId UUID userId) {
        log.info("Received request to get following list for current user: {}", userId);
        List<UserFollowResponse> following = userFollowService.getFollowing(userId);
        log.info(
                "Successfully retrieved {} users that user {} is following",
                following.size(),
                userId);
        return ResponseEntity.ok(following);
    }

    @GetMapping(ApiConstants.FOLLOWING_BY_USER_ENDPOINT)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "Get user's following list",
            description = "Retrieve the list of users that a specific user is following")
    public ResponseEntity<List<UserFollowResponse>> getFollowingByUserId(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        log.info("Received request to get following list for user: {}", userId);
        List<UserFollowResponse> following = userFollowService.getFollowing(userId);
        log.info(
                "Successfully retrieved {} users that user {} is following",
                following.size(),
                userId);
        return ResponseEntity.ok(following);
    }

    @GetMapping(ApiConstants.FOLLOWERS_ME_ENDPOINT)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "Get my followers list",
            description = "Retrieve the list of users that are following the current user")
    public ResponseEntity<List<UserFollowResponse>> getMyFollowers(
            @Parameter(hidden = true) @CurrentUserId UUID userId) {
        log.info("Received request to get followers list for current user: {}", userId);
        List<UserFollowResponse> followers = userFollowService.getFollowers(userId);
        log.info("Successfully retrieved {} followers for user {}", followers.size(), userId);
        return ResponseEntity.ok(followers);
    }

    @GetMapping(ApiConstants.FOLLOWERS_BY_USER_ENDPOINT)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "Get user's followers list",
            description = "Retrieve the list of users that are following a specific user")
    public ResponseEntity<List<UserFollowResponse>> getFollowersByUserId(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        log.info("Received request to get followers list for user: {}", userId);
        List<UserFollowResponse> followers = userFollowService.getFollowers(userId);
        log.info("Successfully retrieved {} followers for user {}", followers.size(), userId);
        return ResponseEntity.ok(followers);
    }
}
