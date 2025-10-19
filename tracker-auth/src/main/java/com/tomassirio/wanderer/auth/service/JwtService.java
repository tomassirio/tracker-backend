package com.tomassirio.wanderer.auth.service;

import com.tomassirio.wanderer.commons.domain.User;
import io.jsonwebtoken.Claims;

/**
 * Service interface for JWT token operations. Provides methods for generating and parsing JWT
 * tokens.
 *
 * @since 0.1.8
 */
public interface JwtService {

    /**
     * Generates a JWT token for the given user. The token includes user ID, username, email, and
     * roles as claims.
     *
     * @param user the user for whom the token is generated
     * @return the generated JWT token as a String
     */
    String generateToken(User user);

    /**
     * Generates a JWT token with a custom JTI for the given user.
     *
     * @param user the user for whom the token is generated
     * @param jti the JWT ID to include in the token
     * @return the generated JWT token as a String
     */
    String generateTokenWithJti(User user, String jti);

    /**
     * Parses the given JWT token and returns the claims.
     *
     * @param token the JWT token to parse
     * @return the Claims object containing the token's payload
     * @throws io.jsonwebtoken.JwtException if the token is invalid or expired
     */
    Claims parseToken(String token);

    /**
     * Returns the expiration time in milliseconds for JWT tokens.
     *
     * @return the expiration time in milliseconds
     */
    long getExpirationMs();

    /**
     * Returns the expiration time in milliseconds for refresh tokens.
     *
     * @return the expiration time in milliseconds
     */
    long getRefreshExpirationMs();
}
