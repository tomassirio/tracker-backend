package com.tomassirio.wanderer.commons.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UserFollowRequest(@NotNull UUID followedId) {}
