package com.tomassirio.wanderer.commons.dto;

import java.time.Instant;

/**
 * DTO representing an individual user's reaction on a comment.
 *
 * @param userId the ID of the user who reacted
 * @param username the username of the user who reacted
 * @param reactionType the type of reaction (HEART, SMILEY, SAD, LAUGH, ANGER)
 * @param timestamp when the reaction was added
 */
public record CommentReactionDTO(
        String userId, String username, String reactionType, Instant timestamp) {}
