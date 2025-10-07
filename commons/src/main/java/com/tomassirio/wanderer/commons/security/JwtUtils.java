package com.tomassirio.wanderer.commons.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class JwtUtils {

    @Value("${jwt.secret:}")
    private String secret;

    private final ObjectMapper mapper = new ObjectMapper();

    private static String base64UrlDecodeToString(String input) {
        return new String(Base64.getUrlDecoder().decode(padBase64(input)), StandardCharsets.UTF_8);
    }

    private static byte[] base64UrlDecode(String input) {
        return Base64.getUrlDecoder().decode(padBase64(input));
    }

    private static String padBase64(String s) {
        // Java's URL decoder handles paddingless inputs but to be safe we can add padding
        int rem = s.length() % 4;
        if (rem == 2) return s + "==";
        if (rem == 3) return s + "=";
        return s;
    }

    private boolean verifySignature(String headerB64, String payloadB64, String signatureB64) {
        if (secret == null || secret.isBlank()) {
            // if no secret configured, don't verify (environment may choose to provide it)
            return true;
        }
        try {
            String data = headerB64 + "." + payloadB64;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec =
                    new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] computed = mac.doFinal(data.getBytes(StandardCharsets.US_ASCII));
            byte[] provided = base64UrlDecode(signatureB64);
            if (computed.length != provided.length) return false;
            int diff = 0;
            for (int i = 0; i < computed.length; i++) {
                diff |= computed[i] ^ provided[i];
            }
            return diff == 0;
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalArgumentException ex) {
            return false;
        }
    }

    public Map<String, Object> parsePayload(String token) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing token");
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Malformed JWT token");
        }
        String headerB64 = parts[0];
        String payloadB64 = parts[1];
        String signatureB64 = parts[2];

        if (!verifySignature(headerB64, payloadB64, signatureB64)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid JWT signature");
        }

        try {
            String payloadJson = base64UrlDecodeToString(payloadB64);
            return mapper.readValue(payloadJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid JWT payload", ex);
        }
    }

    public UUID getUserIdFromAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Missing Authorization header");
        }
        String prefix = "Bearer ";
        String token =
                authorizationHeader.startsWith(prefix)
                        ? authorizationHeader.substring(prefix.length())
                        : authorizationHeader;
        Map<String, Object> payload = parsePayload(token);
        Object sub = payload.get("sub");
        if (sub == null) {
            // some tokens put subject in "userId" or similar; try "user_id" or "userId"
            sub = payload.get("userId");
            if (sub == null) sub = payload.get("user_id");
        }
        if (sub == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token missing subject");
        }
        try {
            return UUID.fromString(sub.toString());
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid user id in token", ex);
        }
    }

    public List<String> getScopesFromClaims(Map<String, Object> claims) {
        Object scopesObj = claims.get("scopes");
        if (scopesObj == null) {
            scopesObj = claims.get("scope");
        }
        List<String> scopes = new ArrayList<>();
        if (scopesObj instanceof String) {
            String s = (String) scopesObj;
            for (String part : s.split("[, ]+")) {
                if (!part.isBlank()) scopes.add(part.trim());
            }
        } else if (scopesObj instanceof Collection) {
            for (Object o : (Collection<?>) scopesObj) {
                if (o != null) scopes.add(o.toString());
            }
        }
        return scopes;
    }
}
