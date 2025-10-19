package com.tomassirio.wanderer.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(
        @NotBlank(message = "Email is required") @Email(message = "Email should be valid")
                String email) {}
