package com.tomassirio.wanderer.command.controller;

import com.tomassirio.wanderer.command.controller.request.CommentCreationRequest;
import com.tomassirio.wanderer.command.controller.request.ReactionRequest;
import com.tomassirio.wanderer.command.service.CommentService;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for comment and reaction operations. Handles comment creation and reactions on
 * trips. Comments can be top-level or replies to other comments (max depth of 1).
 *
 * @since 0.3.0
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Comments", description = "Endpoints for managing comments and reactions on trips")
public class CommentController {

    private final CommentService commentService;

    @PostMapping(ApiConstants.TRIPS_PATH + ApiConstants.TRIP_COMMENTS_ENDPOINT)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "Add a comment or reply to a trip",
            description =
                    "Creates a new top-level comment on a trip or a reply to an existing comment. "
                            + "To create a reply, include the parentCommentId in the request body. "
                            + "Maximum nesting depth is 1 (cannot reply to a reply). Returns 202 Accepted with the comment ID as the operation completes asynchronously.")
    public ResponseEntity<UUID> createComment(
            @Parameter(hidden = true) @CurrentUserId UUID userId,
            @PathVariable UUID tripId,
            @Valid @RequestBody CommentCreationRequest request) {

        log.info("Received request to create comment on trip {} by user {}", tripId, userId);

        UUID commentId = commentService.createComment(userId, tripId, request);

        log.info("Accepted comment creation request with ID: {}", commentId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(commentId);
    }

    @PostMapping(ApiConstants.COMMENTS_PATH + ApiConstants.COMMENT_REACTIONS_ENDPOINT)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "Add a reaction to a comment",
            description =
                    "Adds a reaction to a comment or reply. Returns 202 Accepted with the comment ID as the operation completes asynchronously.")
    public ResponseEntity<UUID> addReactionToComment(
            @Parameter(hidden = true) @CurrentUserId UUID userId,
            @PathVariable UUID commentId,
            @Valid @RequestBody ReactionRequest request) {

        log.info(
                "Received request to add reaction {} to comment {} by user {}",
                request.reactionType(),
                commentId,
                userId);

        UUID updatedCommentId =
                commentService.addReactionToComment(userId, commentId, request.reactionType());

        log.info("Accepted reaction addition request for comment {}", commentId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(updatedCommentId);
    }

    @DeleteMapping(ApiConstants.COMMENTS_PATH + ApiConstants.COMMENT_REACTIONS_ENDPOINT)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "Remove a reaction from a comment",
            description =
                    "Removes a reaction from a comment or reply. Returns 202 Accepted with the comment ID as the operation completes asynchronously.")
    public ResponseEntity<UUID> removeReactionFromComment(
            @Parameter(hidden = true) @CurrentUserId UUID userId,
            @PathVariable UUID commentId,
            @Valid @RequestBody ReactionRequest request) {

        log.info(
                "Received request to remove reaction {} from comment {} by user {}",
                request.reactionType(),
                commentId,
                userId);

        UUID updatedCommentId =
                commentService.removeReactionFromComment(userId, commentId, request.reactionType());

        log.info("Accepted reaction removal request for comment {}", commentId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(updatedCommentId);
    }
}
