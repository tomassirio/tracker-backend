package com.tomassirio.wanderer.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmRequest(
        @NotBlank(message = "Token is required") String token,
        @NotBlank(message = "New password is required")
                @Size(min = 8, message = "Password must be at least 8 characters")
                String newPassword) {}
