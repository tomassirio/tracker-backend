package com.tomassirio.wanderer.query.dto;

import java.util.UUID;

public record UserResponse(UUID id, String username, String email) {}
