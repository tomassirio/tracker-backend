package com.tomassirio.wanderer.auth.service;

import com.tomassirio.wanderer.auth.dto.RefreshTokenResponse;
import java.util.UUID;

/**
 * Service interface for token management operations. Provides methods for managing refresh tokens
 * and password reset tokens.
 *
 * @since 0.4.1
 */
public interface TokenService {

    /**
     * Creates a refresh token for the given user.
     *
     * @param userId the user ID
     * @return the generated refresh token
     */
    String createRefreshToken(UUID userId);

    /**
     * Validates and refreshes the access token using the provided refresh token.
     *
     * @param refreshToken the refresh token
     * @return a RefreshTokenResponse containing new access and refresh tokens
     * @throws IllegalArgumentException if the refresh token is invalid, expired, or revoked
     */
    RefreshTokenResponse refreshAccessToken(String refreshToken);

    /**
     * Revokes all refresh tokens for a user (used for logout all devices).
     *
     * @param userId the user ID
     */
    void revokeAllRefreshTokensForUser(UUID userId);

    /**
     * Creates a password reset token for the given user.
     *
     * @param userId the user ID
     * @return the generated password reset token
     */
    String createPasswordResetToken(UUID userId);

    /**
     * Validates a password reset token and returns the associated user ID.
     *
     * @param token the password reset token
     * @return the user ID associated with the token
     * @throws IllegalArgumentException if the token is invalid, expired, or already used
     */
    UUID validatePasswordResetToken(String token);

    /**
     * Marks a password reset token as used.
     *
     * @param token the password reset token
     */
    void markPasswordResetTokenAsUsed(String token);
}
