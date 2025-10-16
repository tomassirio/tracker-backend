package com.tomassirio.wanderer.command.dto;

import java.time.Instant;
import java.util.UUID;

public record UserFollowResponse(UUID id, UUID followerId, UUID followedId, Instant createdAt) {}
