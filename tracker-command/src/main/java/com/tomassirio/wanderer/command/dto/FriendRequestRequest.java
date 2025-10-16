package com.tomassirio.wanderer.command.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record FriendRequestRequest(@NotNull UUID receiverId) {}
