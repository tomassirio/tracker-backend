package com.tomassirio.wanderer.command.service;

import com.tomassirio.wanderer.command.controller.request.CommentCreationRequest;
import com.tomassirio.wanderer.commons.domain.ReactionType;
import java.util.UUID;

/**
 * Service interface for managing comment operations.
 *
 * <p>This service handles all write operations for comments and reactions on trips. Comments can be
 * top-level or replies to other comments (max depth of 1).
 *
 * @author tomassirio
 * @since 0.3.0
 */
public interface CommentService {

    /**
     * Creates a new comment on a trip or a reply to an existing comment.
     *
     * <p>If the request contains a parentCommentId, this creates a reply to that comment.
     * Otherwise, it creates a top-level comment on the trip.
     *
     * @param userId the UUID of the user creating the comment
     * @param tripId the UUID of the trip to comment on
     * @param request the comment creation request containing the message and optional parent
     *     comment ID
     * @return the UUID of the created comment
     * @throws jakarta.persistence.EntityNotFoundException if the trip or parent comment doesn't
     *     exist
     * @throws IllegalArgumentException if trying to create a reply to a reply (max depth is 1)
     */
    UUID createComment(UUID userId, UUID tripId, CommentCreationRequest request);

    /**
     * Adds a reaction to a comment.
     *
     * @param userId the UUID of the user adding the reaction
     * @param commentId the UUID of the comment to react to
     * @param reactionType the type of reaction to add
     * @return the UUID of the comment
     * @throws jakarta.persistence.EntityNotFoundException if the comment doesn't exist
     */
    UUID addReactionToComment(UUID userId, UUID commentId, ReactionType reactionType);

    /**
     * Removes a reaction from a comment.
     *
     * @param userId the UUID of the user removing the reaction
     * @param commentId the UUID of the comment to remove reaction from
     * @param reactionType the type of reaction to remove
     * @return the UUID of the comment
     * @throws jakarta.persistence.EntityNotFoundException if the comment doesn't exist
     */
    UUID removeReactionFromComment(UUID userId, UUID commentId, ReactionType reactionType);
}
