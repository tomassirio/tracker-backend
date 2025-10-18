package com.tomassirio.wanderer.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(
        @NotBlank(message = "Current password is required") String currentPassword,
        @NotBlank(message = "New password is required")
                @Size(min = 8, message = "Password must be at least 8 characters")
                String newPassword) {}
