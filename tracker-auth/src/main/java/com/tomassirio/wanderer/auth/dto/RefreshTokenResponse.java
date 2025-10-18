package com.tomassirio.wanderer.auth.dto;

public record RefreshTokenResponse(
        String accessToken, String refreshToken, String tokenType, long expiresIn) {}
