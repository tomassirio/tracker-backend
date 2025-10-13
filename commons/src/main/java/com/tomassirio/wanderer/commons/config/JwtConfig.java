package com.tomassirio.wanderer.commons.config;

import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class JwtConfig {

    @Bean
    public JwtDecoder jwtDecoder(@Value("${security.jwt.secret:${jwt.secret:}}") String secret) {
        if (secret == null || secret.isBlank()) {
            return NimbusJwtDecoder.withSecretKey(
                            new SecretKeySpec(new byte[32], SignatureAlgorithm.HS256.getJcaName()))
                    .build();
        }
        SecretKey key =
                new SecretKeySpec(
                        secret.getBytes(StandardCharsets.UTF_8),
                        SignatureAlgorithm.HS256.getJcaName());
        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}
