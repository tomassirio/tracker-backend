package com.tomassirio.wanderer.commons.dto;

import com.tomassirio.wanderer.commons.domain.FriendRequestStatus;
import java.time.Instant;
import java.util.UUID;

public record FriendRequestResponse(
        UUID id,
        UUID senderId,
        UUID receiverId,
        FriendRequestStatus status,
        Instant createdAt,
        Instant updatedAt) {}
