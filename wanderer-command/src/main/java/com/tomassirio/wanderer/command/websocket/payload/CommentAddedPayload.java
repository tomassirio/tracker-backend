package com.tomassirio.wanderer.command.websocket.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentAddedPayload {
    private UUID tripId;
    private UUID commentId;
    private UUID id; // Frontend expects both commentId and id
    private UUID userId;
    private String username;
    private String message;
    private UUID parentCommentId;

    /**
     * Creates a payload with the comment ID automatically set in both fields.
     *
     * @param tripId the trip ID
     * @param commentId the comment ID (will be set in both commentId and id fields)
     * @param userId the user ID
     * @param username the username
     * @param message the comment message
     * @param parentCommentId the parent comment ID (null for top-level comments)
     * @return a new CommentAddedPayload with consistent ID fields
     */
    public static CommentAddedPayload create(
            UUID tripId,
            UUID commentId,
            UUID userId,
            String username,
            String message,
            UUID parentCommentId) {
        return CommentAddedPayload.builder()
                .tripId(tripId)
                .commentId(commentId)
                .id(commentId) // Both fields get the same value
                .userId(userId)
                .username(username)
                .message(message)
                .parentCommentId(parentCommentId)
                .build();
    }
}
