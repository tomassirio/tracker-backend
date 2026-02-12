package com.tomassirio.wanderer.command.websocket;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentReactionPayload {
    private UUID tripId;
    private UUID commentId;
    private String reactionType;
    private UUID userId;
}
