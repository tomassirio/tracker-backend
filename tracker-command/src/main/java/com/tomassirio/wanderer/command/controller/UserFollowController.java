package com.tomassirio.wanderer.command.controller;

import com.tomassirio.wanderer.command.dto.UserFollowRequest;
import com.tomassirio.wanderer.command.dto.UserFollowResponse;
import com.tomassirio.wanderer.command.service.UserFollowService;
import com.tomassirio.wanderer.commons.constants.ApiConstants;
import com.tomassirio.wanderer.commons.security.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
 * @since 0.3.7
 */
@RestController
@RequestMapping(ApiConstants.USERS_PATH + "/follows")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Follows", description = "Endpoints for managing user follows")
public class UserFollowController {

    private final UserFollowService userFollowService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Follow a user", description = "Follow another user")
    public ResponseEntity<UserFollowResponse> followUser(
            @Parameter(hidden = true) @CurrentUserId UUID followerId,
            @Valid @RequestBody UserFollowRequest request) {
        log.info("Received request for user {} to follow user {}", followerId, request.followedId());
        UserFollowResponse response = userFollowService.followUser(followerId, request.followedId());
        log.info("User {} now follows user {}", followerId, request.followedId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{followedId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Unfollow a user", description = "Unfollow a user")
    public ResponseEntity<Void> unfollowUser(
            @Parameter(hidden = true) @CurrentUserId UUID followerId,
            @PathVariable UUID followedId) {
        log.info("Received request for user {} to unfollow user {}", followerId, followedId);
        userFollowService.unfollowUser(followerId, followedId);
        log.info("User {} unfollowed user {}", followerId, followedId);
        return ResponseEntity.noContent().build();
    }
}
