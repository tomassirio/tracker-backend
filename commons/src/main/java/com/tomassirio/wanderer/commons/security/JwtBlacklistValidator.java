package com.tomassirio.wanderer.commons.security;

import com.tomassirio.wanderer.commons.client.AuthServiceClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Custom JWT validator that checks if a token's JTI (JWT ID) is blacklisted. This validator is used
 * across all services to ensure that logged-out tokens cannot be used even if they haven't expired.
 *
 * <p>This validator calls the auth service's token validation endpoint to check the blacklist,
 * maintaining proper separation of concerns and avoiding database coupling between services.
 */
@Component
@RequiredArgsConstructor
public class JwtBlacklistValidator implements OAuth2TokenValidator<Jwt> {

    private static final Logger log = LoggerFactory.getLogger(JwtBlacklistValidator.class);

    private final AuthServiceClient authServiceClient;

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        String jti = jwt.getId();

        // If token has no JTI, it's an older token format - allow it for backward compatibility
        if (jti == null || jti.isBlank()) {
            return OAuth2TokenValidatorResult.success();
        }

        try {
            // Call auth service to check if token is blacklisted
            var response = authServiceClient.isTokenBlacklisted(jti);
            boolean isBlacklisted = response.getOrDefault("blacklisted", false);

            if (isBlacklisted) {
                log.debug("Token with JTI {} is blacklisted", jti);
                OAuth2Error error =
                        new OAuth2Error(
                                "invalid_token",
                                "The token has been revoked",
                                "https://tools.ietf.org/html/rfc6750#section-3.1");
                return OAuth2TokenValidatorResult.failure(error);
            }

            return OAuth2TokenValidatorResult.success();
        } catch (Exception e) {
            // If auth service is unavailable, log error and fail open (allow the token)
            // This prevents a cascading failure if auth service is down
            log.warn(
                    "Failed to validate token blacklist status, allowing token: {}",
                    e.getMessage());
            return OAuth2TokenValidatorResult.success();
        }
    }
}
