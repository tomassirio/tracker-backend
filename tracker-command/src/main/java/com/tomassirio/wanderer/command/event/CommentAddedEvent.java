package com.tomassirio.wanderer.command.event;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentAddedEvent implements DomainEvent {
    private UUID commentId;
    private UUID tripId;
    private UUID userId;
    private String username;
    private String message;
    private UUID parentCommentId;
    private Instant timestamp;
}
