package com.tomassirio.wanderer.command.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreationRequest(
        @NotBlank(message = "Username is required") @Size(min = 3, max = 50) String username,
        @NotBlank(message = "Email is required") @Email(message = "Email should be valid")
                String email) {}
