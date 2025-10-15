package com.tomassirio.wanderer.commons.dto;

import java.time.Instant;
import java.util.List;

public record CommentDTO(
        String id,
        String userId,
        String tripId,
        String parentCommentId, // null if top-level comment, set if this is a reply
        String message,
        ReactionsDTO reactions,
        List<CommentDTO> replies, // Only populated for top-level comments
        Instant timestamp) {}
