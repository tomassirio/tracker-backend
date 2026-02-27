package com.tomassirio.wanderer.command.controller;

import com.tomassirio.wanderer.command.service.UserFollowService;
import com.tomassirio.wanderer.commons.constants.ApiConstants;
import com.tomassirio.wanderer.commons.dto.UserFollowRequest;
import com.tomassirio.wanderer.commons.security.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user follow operations.
 *
 * @author tomassirio
 * @since 0.4.0
 */
@RestController
@RequestMapping(
        value = ApiConstants.FOLLOWS_PATH,
        produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Follows", description = "Endpoints for managing user follows")
public class UserFollowController {

    private final UserFollowService userFollowService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "Follow a user",
            description =
                    "Follow another user. Returns 202 Accepted with the follow relationship ID as the operation completes asynchronously.")
    public ResponseEntity<UUID> followUser(
            @Parameter(hidden = true) @CurrentUserId UUID followerId,
            @Valid @RequestBody UserFollowRequest request) {
        log.info(
                "Received request for user {} to follow user {}", followerId, request.followedId());
        UUID followId = userFollowService.followUser(followerId, request.followedId());
        log.info("Accepted follow request from {} to {}", followerId, request.followedId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(followId);
    }

    @DeleteMapping(ApiConstants.FOLLOW_BY_ID_ENDPOINT)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "Unfollow a user",
            description =
                    "Unfollow a user. Returns 202 Accepted as the operation completes asynchronously.")
    public ResponseEntity<Void> unfollowUser(
            @Parameter(hidden = true) @CurrentUserId UUID followerId,
            @PathVariable UUID followedId) {
        log.info("Received request for user {} to unfollow user {}", followerId, followedId);
        userFollowService.unfollowUser(followerId, followedId);
        log.info("Accepted unfollow request from {} to {}", followerId, followedId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
