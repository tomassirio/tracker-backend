package com.tomassirio.wanderer.commons.security;

import com.tomassirio.wanderer.commons.repository.TokenBlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Custom JWT validator that checks if a token's JTI (JWT ID) is blacklisted. This validator is used
 * across all services to ensure that logged-out tokens cannot be used even if they haven't expired.
 */
@Component
@RequiredArgsConstructor
public class JwtBlacklistValidator implements OAuth2TokenValidator<Jwt> {

    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        String jti = jwt.getId();

        // If token has no JTI, it's an older token format - allow it for backward compatibility
        if (jti == null || jti.isBlank()) {
            return OAuth2TokenValidatorResult.success();
        }

        // Check if the token's JTI is in the blacklist
        boolean isBlacklisted = tokenBlacklistRepository.existsByTokenJti(jti);

        if (isBlacklisted) {
            OAuth2Error error =
                    new OAuth2Error(
                            "invalid_token",
                            "The token has been revoked",
                            "https://tools.ietf.org/html/rfc6750#section-3.1");
            return OAuth2TokenValidatorResult.failure(error);
        }

        return OAuth2TokenValidatorResult.success();
    }
}
