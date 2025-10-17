package com.tomassirio.wanderer.auth.service.impl;

import com.tomassirio.wanderer.auth.client.TrackerQueryClient;
import com.tomassirio.wanderer.auth.domain.PasswordResetToken;
import com.tomassirio.wanderer.auth.domain.RefreshToken;
import com.tomassirio.wanderer.auth.domain.TokenBlacklist;
import com.tomassirio.wanderer.auth.dto.RefreshTokenResponse;
import com.tomassirio.wanderer.auth.repository.PasswordResetTokenRepository;
import com.tomassirio.wanderer.auth.repository.RefreshTokenRepository;
import com.tomassirio.wanderer.auth.repository.TokenBlacklistRepository;
import com.tomassirio.wanderer.auth.service.JwtService;
import com.tomassirio.wanderer.auth.service.TokenService;
import com.tomassirio.wanderer.commons.domain.User;
import feign.FeignException;
import io.jsonwebtoken.Claims;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for token management operations. Handles refresh tokens, token blacklist,
 * and password reset tokens.
 */
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtService jwtService;
    private final TrackerQueryClient trackerQueryClient;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public String createRefreshToken(UUID userId) {
        // Generate a secure random token
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        // Hash the token before storing
        String tokenHash = hashToken(token);

        // Calculate expiration
        Instant expiresAt = Instant.now().plusMillis(jwtService.getRefreshExpirationMs());

        // Create and save refresh token
        RefreshToken refreshToken =
                RefreshToken.builder()
                        .tokenId(UUID.randomUUID())
                        .userId(userId)
                        .tokenHash(tokenHash)
                        .expiresAt(expiresAt)
                        .revoked(false)
                        .build();

        refreshTokenRepository.save(refreshToken);

        return token;
    }

    @Override
    @Transactional
    public RefreshTokenResponse refreshAccessToken(String refreshToken) {
        // Hash the provided token to find it in the database
        String tokenHash = hashToken(refreshToken);

        // Find the refresh token
        Optional<RefreshToken> maybeToken = refreshTokenRepository.findByTokenHash(tokenHash);
        if (maybeToken.isEmpty()) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        RefreshToken storedToken = maybeToken.get();

        // Validate token
        if (storedToken.isRevoked()) {
            throw new IllegalArgumentException("Refresh token has been revoked");
        }

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token has expired");
        }

        // Get user information
        User user;
        try {
            user = trackerQueryClient.getUserById(storedToken.getUserId());
        } catch (FeignException e) {
            throw new IllegalStateException("Failed to fetch user information", e);
        }

        // Generate new access token with JTI
        String jti = UUID.randomUUID().toString();
        String newAccessToken = jwtService.generateTokenWithJti(user, jti);

        // Generate new refresh token (token rotation)
        String newRefreshToken = createRefreshToken(user.getId());

        // Revoke old refresh token
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        return new RefreshTokenResponse(
                newAccessToken, newRefreshToken, "Bearer", jwtService.getExpirationMs());
    }

    @Override
    @Transactional
    public void blacklistToken(String token) {
        // Parse token to extract JTI and expiration
        Claims claims;
        try {
            claims = jwtService.parseToken(token);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token", e);
        }

        String jti = claims.getId();
        if (jti == null || jti.isBlank()) {
            throw new IllegalArgumentException("Token does not contain a JTI");
        }

        // Don't blacklist if already blacklisted
        if (tokenBlacklistRepository.existsByTokenJti(jti)) {
            return;
        }

        // Create blacklist entry
        TokenBlacklist blacklistEntry =
                TokenBlacklist.builder()
                        .id(UUID.randomUUID())
                        .tokenJti(jti)
                        .expiresAt(claims.getExpiration().toInstant())
                        .build();

        tokenBlacklistRepository.save(blacklistEntry);
    }

    @Override
    public boolean isTokenBlacklisted(String jti) {
        return tokenBlacklistRepository.existsByTokenJti(jti);
    }

    @Override
    @Transactional
    public void revokeAllRefreshTokensForUser(UUID userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
    }

    @Override
    @Transactional
    public String createPasswordResetToken(UUID userId) {
        // Generate a secure random token
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        // Hash the token before storing
        String tokenHash = hashToken(token);

        // Calculate expiration (1 hour)
        Instant expiresAt = Instant.now().plusSeconds(3600);

        // Create and save password reset token
        PasswordResetToken resetToken =
                PasswordResetToken.builder()
                        .tokenId(UUID.randomUUID())
                        .userId(userId)
                        .tokenHash(tokenHash)
                        .expiresAt(expiresAt)
                        .used(false)
                        .build();

        passwordResetTokenRepository.save(resetToken);

        return token;
    }

    @Override
    public UUID validatePasswordResetToken(String token) {
        // Hash the provided token to find it in the database
        String tokenHash = hashToken(token);

        // Find the reset token
        Optional<PasswordResetToken> maybeToken =
                passwordResetTokenRepository.findByTokenHash(tokenHash);
        if (maybeToken.isEmpty()) {
            throw new IllegalArgumentException("Invalid password reset token");
        }

        PasswordResetToken resetToken = maybeToken.get();

        // Validate token
        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("Password reset token has already been used");
        }

        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Password reset token has expired");
        }

        return resetToken.getUserId();
    }

    @Override
    @Transactional
    public void markPasswordResetTokenAsUsed(String token) {
        String tokenHash = hashToken(token);
        Optional<PasswordResetToken> maybeToken =
                passwordResetTokenRepository.findByTokenHash(tokenHash);
        if (maybeToken.isPresent()) {
            PasswordResetToken resetToken = maybeToken.get();
            resetToken.setUsed(true);
            passwordResetTokenRepository.save(resetToken);
        }
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
