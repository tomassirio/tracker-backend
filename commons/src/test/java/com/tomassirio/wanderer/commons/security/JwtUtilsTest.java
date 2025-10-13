package com.tomassirio.wanderer.commons.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tomassirio.wanderer.commons.utils.JwtBuilder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class JwtUtilsTest {

    @Test
    void parsePayload_withoutSecret_accepts_token()
            throws NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
        JwtUtils jwtUtils = new JwtUtils(); // secret is null -> verification skipped
        UUID id = UUID.randomUUID();
        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", id.toString());
        payload.put("roles", "login admin");

        String token = JwtBuilder.buildJwt(payload, "any-secret");

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
        String token = JwtBuilder.buildJwt(payload, "wrong-secret");
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
        payload.put("roles", List.of("user"));

        String token = JwtBuilder.buildJwt(payload, secret);
        UUID extracted = jwtUtils.getUserIdFromAuthorizationHeader("Bearer " + token);
        assertEquals(id, extracted);
    }

    @Test
    void getRolesFromClaims_handles_string_and_collection() {
        JwtUtils jwtUtils = new JwtUtils();
        Map<String, Object> claims1 = Map.of("roles", "user admin");
        List<String> roles1 = jwtUtils.getRolesFromClaims(claims1);
        assertIterableEquals(List.of("user", "admin"), roles1);

        Map<String, Object> claims2 = Map.of("roles", List.of("user", "admin"));
        List<String> roles2 = jwtUtils.getRolesFromClaims(claims2);
        assertIterableEquals(List.of("user", "admin"), roles2);
    }
}
