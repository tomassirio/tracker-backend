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
public class FriendRequestSentEvent implements DomainEvent {
    private UUID requestId;
    private UUID senderId;
    private UUID receiverId;
    private String status;
    private Instant createdAt;
}
