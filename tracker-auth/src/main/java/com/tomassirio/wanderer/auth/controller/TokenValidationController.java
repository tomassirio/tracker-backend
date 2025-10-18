package com.tomassirio.wanderer.auth.controller;

import com.tomassirio.wanderer.auth.repository.TokenBlacklistRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal API controller for token validation. Used by other services to check if a token has been
 * blacklisted.
 */
@RestController
@RequestMapping("/internal/api/1/tokens")
@RequiredArgsConstructor
@Tag(name = "Token Validation (Internal)", description = "Internal endpoints for token validation")
public class TokenValidationController {

    private final TokenBlacklistRepository tokenBlacklistRepository;

    @GetMapping("/validate")
    @Operation(
            summary = "Check if token JTI is blacklisted",
            description =
                    "Internal endpoint used by other services to validate tokens. Returns whether the token's JTI is in the blacklist.")
    public ResponseEntity<Map<String, Boolean>> isTokenBlacklisted(@RequestParam String jti) {
        boolean isBlacklisted = tokenBlacklistRepository.existsByTokenJti(jti);
        return ResponseEntity.ok(Map.of("blacklisted", isBlacklisted));
    }
}
