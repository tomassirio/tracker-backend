package com.tomassirio.wanderer.auth.dto;

public record LoginResponse(String accessToken, String tokenType, long expiresIn) {}
