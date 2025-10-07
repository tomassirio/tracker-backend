package com.tomassirio.wanderer.commons.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class JwtUtilsTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String createJwt(Map<String, Object> payload, String secret) {
        try {
            String headerJson = MAPPER.writeValueAsString(Map.of("alg", "HS256", "typ", "JWT"));
            String payloadJson = MAPPER.writeValueAsString(payload);
            String headerB64 = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
            String payloadB64 = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
            String data = headerB64 + "." + payloadB64;
            byte[] signature = new byte[0];
            if (secret != null) {
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
                signature = mac.doFinal(data.getBytes(java.nio.charset.StandardCharsets.US_ASCII));
            }
            String sigB64 = base64UrlEncode(signature);
            return headerB64 + "." + payloadB64 + "." + sigB64;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    void parsePayload_withoutSecret_accepts_token() {
        JwtUtils jwtUtils = new JwtUtils(); // secret is null -> verification skipped
        UUID id = UUID.randomUUID();
        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", id.toString());
        payload.put("scopes", "login admin");

        String token = createJwt(payload, "any-secret");

        Map<String, Object> parsed = jwtUtils.parsePayload(token);
        assertEquals(id.toString(), parsed.get("sub"));
    }

    @Test
    void parsePayload_malformed_shouldThrow() {
        JwtUtils jwtUtils = new JwtUtils();
        String malformed = "abc.def"; // not three parts
        assertThrows(ResponseStatusException.class, () -> jwtUtils.parsePayload(malformed));
    }

    @Test
    void parsePayload_invalidSignature_whenSecretSet_shouldThrow() throws Exception {
        JwtUtils jwtUtils = new JwtUtils();
        // set secret via reflection
        var field = JwtUtils.class.getDeclaredField("secret");
        field.setAccessible(true);
        field.set(jwtUtils, "correct-secret");

        Map<String, Object> payload = Map.of("sub", UUID.randomUUID().toString());
        // create a token signed with a different secret
        String token = createJwt(payload, "wrong-secret");
        assertThrows(ResponseStatusException.class, () -> jwtUtils.parsePayload(token));
    }

    @Test
    void getUserIdFromAuthorizationHeader_extracts_sub() throws Exception {
        JwtUtils jwtUtils = new JwtUtils();
        var field = JwtUtils.class.getDeclaredField("secret");
        field.setAccessible(true);
        String secret = "my-test-secret-which-is-sufficiently-long";
        field.set(jwtUtils, secret);

        UUID id = UUID.randomUUID();
        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", id.toString());
        payload.put("scopes", List.of("login"));

        String token = createJwt(payload, secret);
        UUID extracted = jwtUtils.getUserIdFromAuthorizationHeader("Bearer " + token);
        assertEquals(id, extracted);
    }

    @Test
    void getScopesFromClaims_handles_string_and_collection() {
        JwtUtils jwtUtils = new JwtUtils();
        Map<String, Object> claims1 = Map.of("scopes", "login admin");
        List<String> scopes1 = jwtUtils.getScopesFromClaims(claims1);
        assertIterableEquals(List.of("login", "admin"), scopes1);

        Map<String, Object> claims2 = Map.of("scopes", List.of("login", "admin"));
        List<String> scopes2 = jwtUtils.getScopesFromClaims(claims2);
        assertIterableEquals(List.of("login", "admin"), scopes2);
    }
}
