package com.tomassirio.wanderer.command.controller;

import com.tomassirio.wanderer.command.service.FriendRequestService;
import com.tomassirio.wanderer.commons.constants.ApiConstants;
import com.tomassirio.wanderer.commons.dto.FriendRequestRequest;
import com.tomassirio.wanderer.commons.dto.FriendRequestResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for friend request command operations.
 *
 * @author tomassirio
 * @since 0.4.0
 */
@RestController
@RequestMapping(ApiConstants.FRIEND_REQUESTS_PATH)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Friend Requests", description = "Endpoints for managing friend requests")
public class FriendRequestController {

    private final FriendRequestService friendRequestService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "Send a friend request",
            description =
                    "Send a friend request to another user. Returns 202 Accepted as the operation completes asynchronously.")
    public ResponseEntity<FriendRequestResponse> sendFriendRequest(
            @Parameter(hidden = true) @CurrentUserId UUID senderId,
            @Valid @RequestBody FriendRequestRequest request) {
        log.info(
                "Received request to send friend request from {} to {}",
                senderId,
                request.receiverId());
        FriendRequestResponse response =
                friendRequestService.sendFriendRequest(senderId, request.receiverId());
        log.info("Accepted friend request with ID: {}", response.id());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PostMapping(ApiConstants.FRIEND_REQUEST_ACCEPT_ENDPOINT)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "Accept a friend request",
            description =
                    "Accept a pending friend request. Returns 202 Accepted as the operation completes asynchronously.")
    public ResponseEntity<FriendRequestResponse> acceptFriendRequest(
            @Parameter(hidden = true) @CurrentUserId UUID userId, @PathVariable UUID requestId) {
        log.info("Received request to accept friend request {} by user {}", requestId, userId);
        FriendRequestResponse response =
                friendRequestService.acceptFriendRequest(requestId, userId);
        log.info("Accepted friend request {} acceptance", requestId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PostMapping(ApiConstants.FRIEND_REQUEST_DECLINE_ENDPOINT)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "Decline a friend request",
            description =
                    "Decline a pending friend request. Returns 202 Accepted as the operation completes asynchronously.")
    public ResponseEntity<FriendRequestResponse> declineFriendRequest(
            @Parameter(hidden = true) @CurrentUserId UUID userId, @PathVariable UUID requestId) {
        log.info("Received request to decline friend request {} by user {}", requestId, userId);
        FriendRequestResponse response =
                friendRequestService.declineFriendRequest(requestId, userId);
        log.info("Accepted friend request {} declination", requestId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
