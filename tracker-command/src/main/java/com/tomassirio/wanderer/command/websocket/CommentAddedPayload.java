package com.tomassirio.wanderer.command.websocket;

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
}
