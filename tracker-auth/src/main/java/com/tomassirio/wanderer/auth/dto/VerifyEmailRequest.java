package com.tomassirio.wanderer.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(@NotBlank(message = "Token is required") String token) {}
