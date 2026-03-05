package com.tomassirio.wanderer.query.dto;

import com.tomassirio.wanderer.commons.dto.UserDetailsDTO;
import java.util.UUID;

public record UserResponse(UUID id, String username, UserDetailsDTO userDetails) {}
