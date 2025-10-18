package com.tomassirio.wanderer.commons.config;

import com.tomassirio.wanderer.commons.security.JwtBlacklistValidator;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class JwtConfig {

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${security.jwt.secret:${jwt.secret:}}") String secret,
            JwtBlacklistValidator jwtBlacklistValidator) {
        SecretKey key;
        if (secret == null || secret.isBlank()) {
            key = new SecretKeySpec(new byte[32], SignatureAlgorithm.HS256.getJcaName());
        } else {
            key =
                    new SecretKeySpec(
                            secret.getBytes(StandardCharsets.UTF_8),
                            SignatureAlgorithm.HS256.getJcaName());
        }

        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(key).build();

        // Add custom validators including blacklist check
        OAuth2TokenValidator<Jwt> validator =
                new DelegatingOAuth2TokenValidator<>(
                        JwtValidators.createDefault(), jwtBlacklistValidator);

        jwtDecoder.setJwtValidator(validator);
        return jwtDecoder;
    }
}
