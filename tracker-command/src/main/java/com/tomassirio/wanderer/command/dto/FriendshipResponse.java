package com.tomassirio.wanderer.command.dto;

import java.util.UUID;

public record FriendshipResponse(UUID userId, UUID friendId) {}
