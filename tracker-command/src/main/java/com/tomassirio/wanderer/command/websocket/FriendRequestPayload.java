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
public class FriendRequestPayload {
    private UUID requestId;
    private UUID senderId;
    private UUID receiverId;
    private String status;
}
