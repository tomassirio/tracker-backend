package com.tomassirio.wanderer.command.controller;

import com.tomassirio.wanderer.command.service.FriendshipService;
import com.tomassirio.wanderer.commons.constants.ApiConstants;
import com.tomassirio.wanderer.commons.security.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for friendship command operations. Handles friendship removal requests.
 *
 * @author tomassirio
 * @since 0.5.0
 */
@RestController
@RequestMapping(value = ApiConstants.FRIENDS_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Friends", description = "Endpoints for managing friendships")
public class FriendshipController {

    private final FriendshipService friendshipService;

    @DeleteMapping(ApiConstants.FRIEND_BY_ID_ENDPOINT)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "Remove a friend",
            description =
                    "Removes a friendship between the current user and the specified friend. "
                            + "This operation is bidirectional - both users will no longer be friends. "
                            + "Returns 202 Accepted as the operation completes asynchronously.")
    @ApiResponse(responseCode = "202", description = "Friendship removal accepted")
    @ApiResponse(responseCode = "400", description = "Users are not friends")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public ResponseEntity<Void> removeFriend(
            @Parameter(hidden = true) @CurrentUserId UUID userId,
            @Parameter(description = "UUID of the friend to remove") @PathVariable UUID friendId) {
        log.info("Received request to remove friendship between {} and {}", userId, friendId);

        friendshipService.removeFriendship(userId, friendId);

        log.info("Accepted friendship removal request between {} and {}", userId, friendId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
