package com.tomassirio.wanderer.command.controller.request;

import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record UserDetailsRequest(
        @Size(max = 100, message = "Display name must not exceed 100 characters")
                String displayName,
        @Size(max = 500, message = "Bio must not exceed 500 characters") String bio,
        @URL(message = "Avatar URL must be a valid URL")
                @Size(max = 512, message = "Avatar URL must not exceed 512 characters")
                String avatarUrl) {}
