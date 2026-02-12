package com.tomassirio.wanderer.command.event;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentReactionEvent {
    private UUID tripId;
    private UUID commentId;
    private String reactionType;
    private UUID userId;
    private boolean added; // true for added, false for removed
}
