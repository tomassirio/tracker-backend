package com.tomassirio.wanderer.commons.security;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final String TEST_SECRET = "test-secret-key-for-jwt-validation";

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secret", TEST_SECRET);
    }

    @Test
    void parsePayload_shouldParseValidToken() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", userId.toString());
        payload.put("roles", List.of("USER", "ADMIN"));
        String token = createValidToken(payload);

        // When
        Map<String, Object> result = jwtUtils.parsePayload(token);

        // Then
        assertNotNull(result);
        assertEquals(userId.toString(), result.get("sub"));
        assertNotNull(result.get("roles"));
    }

    @Test
    void parsePayload_shouldThrowExceptionForNullToken() {
        // When & Then
        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> jwtUtils.parsePayload(null));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertTrue(Objects.requireNonNull(exception.getReason()).contains("Missing token"));
    }

    @Test
    void parsePayload_shouldThrowExceptionForBlankToken() {
        // When & Then
        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> jwtUtils.parsePayload("   "));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertTrue(Objects.requireNonNull(exception.getReason()).contains("Missing token"));
    }

    @Test
    void parsePayload_shouldThrowExceptionForMalformedToken() {
        // When & Then
        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> jwtUtils.parsePayload("invalid.token"));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertTrue(Objects.requireNonNull(exception.getReason()).contains("Malformed JWT token"));
    }

    @Test
    void parsePayload_shouldThrowExceptionForInvalidSignature() throws Exception {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", UUID.randomUUID().toString());
        String token = createValidToken(payload);
        // Tamper with the signature
        String[] parts = token.split("\\.");
        String tamperedToken = parts[0] + "." + parts[1] + ".invalidSignature";

        // When & Then
        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> jwtUtils.parsePayload(tamperedToken));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertTrue(Objects.requireNonNull(exception.getReason()).contains("Invalid JWT signature"));
    }

    @Test
    void parsePayload_shouldThrowExceptionForInvalidPayloadJson() {
        // Given
        String header = base64UrlEncode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String invalidPayload = base64UrlEncode("not-valid-json");
        String signature = computeSignature(header, invalidPayload);
        String token = header + "." + invalidPayload + "." + signature;

        // When & Then
        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> jwtUtils.parsePayload(token));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertTrue(Objects.requireNonNull(exception.getReason()).contains("Invalid JWT payload"));
    }

    @Test
    void parsePayload_shouldAcceptTokenWithoutSecretVerification() throws Exception {
        // Given - JwtUtils without secret
        JwtUtils noSecretJwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(noSecretJwtUtils, "secret", "");
        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", UUID.randomUUID().toString());
        String token = createTokenWithoutSignature(payload);

        // When
        Map<String, Object> result = noSecretJwtUtils.parsePayload(token);

        // Then
        assertNotNull(result);
    }

    @Test
    void getUserIdFromAuthorizationHeader_shouldExtractUserIdWithBearerPrefix() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", userId.toString());
        String token = createValidToken(payload);
        String authHeader = "Bearer " + token;

        // When
        UUID result = jwtUtils.getUserIdFromAuthorizationHeader(authHeader);

        // Then
        assertEquals(userId, result);
    }

    @Test
    void getUserIdFromAuthorizationHeader_shouldExtractUserIdWithoutBearerPrefix()
            throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", userId.toString());
        String token = createValidToken(payload);

        // When
        UUID result = jwtUtils.getUserIdFromAuthorizationHeader(token);

        // Then
        assertEquals(userId, result);
    }

    @Test
    void getUserIdFromAuthorizationHeader_shouldExtractUserIdFromUserIdClaim() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId.toString());
        String token = createValidToken(payload);

        // When
        UUID result = jwtUtils.getUserIdFromAuthorizationHeader(token);

        // Then
        assertEquals(userId, result);
    }

    @Test
    void getUserIdFromAuthorizationHeader_shouldExtractUserIdFromUser_idClaim() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", userId.toString());
        String token = createValidToken(payload);

        // When
        UUID result = jwtUtils.getUserIdFromAuthorizationHeader(token);

        // Then
        assertEquals(userId, result);
    }

    @Test
    void getUserIdFromAuthorizationHeader_shouldThrowExceptionForNullHeader() {
        // When & Then
        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> jwtUtils.getUserIdFromAuthorizationHeader(null));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertTrue(Objects.requireNonNull(exception.getReason()).contains("Missing Authorization header"));
    }

    @Test
    void getUserIdFromAuthorizationHeader_shouldThrowExceptionForBlankHeader() {
        // When & Then
        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> jwtUtils.getUserIdFromAuthorizationHeader("   "));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertTrue(Objects.requireNonNull(exception.getReason()).contains("Missing Authorization header"));
    }

    @Test
    void getUserIdFromAuthorizationHeader_shouldThrowExceptionForMissingSubject()
            throws Exception {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", "test@example.com");
        String token = createValidToken(payload);

        // When & Then
        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> jwtUtils.getUserIdFromAuthorizationHeader(token));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertTrue(Objects.requireNonNull(exception.getReason()).contains("Token missing subject"));
    }

    @Test
    void getUserIdFromAuthorizationHeader_shouldThrowExceptionForInvalidUUID() throws Exception {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", "not-a-valid-uuid");
        String token = createValidToken(payload);

        // When & Then
        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> jwtUtils.getUserIdFromAuthorizationHeader(token));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertTrue(Objects.requireNonNull(exception.getReason()).contains("Invalid user id in token"));
    }

    @Test
    void getRolesFromClaims_shouldExtractRolesFromList() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", List.of("USER", "ADMIN", "MODERATOR"));

        // When
        List<String> roles = jwtUtils.getRolesFromClaims(claims);

        // Then
        assertEquals(3, roles.size());
        assertTrue(roles.contains("USER"));
        assertTrue(roles.contains("ADMIN"));
        assertTrue(roles.contains("MODERATOR"));
    }

    @Test
    void getRolesFromClaims_shouldExtractRoleFromString() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "USER");

        // When
        List<String> roles = jwtUtils.getRolesFromClaims(claims);

        // Then
        assertEquals(1, roles.size());
        assertEquals("USER", roles.getFirst());
    }

    @Test
    void getRolesFromClaims_shouldExtractRolesFromCommaSeparatedString() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", "USER, ADMIN, MODERATOR");

        // When
        List<String> roles = jwtUtils.getRolesFromClaims(claims);

        // Then
        assertEquals(3, roles.size());
        assertTrue(roles.contains("USER"));
        assertTrue(roles.contains("ADMIN"));
        assertTrue(roles.contains("MODERATOR"));
    }

    @Test
    void getRolesFromClaims_shouldExtractRolesFromSpaceSeparatedString() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", "USER ADMIN MODERATOR");

        // When
        List<String> roles = jwtUtils.getRolesFromClaims(claims);

        // Then
        assertEquals(3, roles.size());
        assertTrue(roles.contains("USER"));
        assertTrue(roles.contains("ADMIN"));
        assertTrue(roles.contains("MODERATOR"));
    }

    @Test
    void getRolesFromClaims_shouldReturnEmptyListForNoRoles() {
        // Given
        Map<String, Object> claims = new HashMap<>();

        // When
        List<String> roles = jwtUtils.getRolesFromClaims(claims);

        // Then
        assertTrue(roles.isEmpty());
    }

    @Test
    void getRolesFromClaims_shouldHandleBlankStringRoles() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", "   ");

        // When
        List<String> roles = jwtUtils.getRolesFromClaims(claims);

        // Then
        assertTrue(roles.isEmpty());
    }

    @Test
    void getRolesFromClaims_shouldTrimRoleNames() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", "  USER  ,  ADMIN  ");

        // When
        List<String> roles = jwtUtils.getRolesFromClaims(claims);

        // Then
        assertEquals(2, roles.size());
        assertTrue(roles.contains("USER"));
        assertTrue(roles.contains("ADMIN"));
    }

    // Helper methods
    private String createValidToken(Map<String, Object> payload) throws Exception {
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String headerB64 = base64UrlEncode(header);
        String payloadJson = mapper.writeValueAsString(payload);
        String payloadB64 = base64UrlEncode(payloadJson);
        String signature = computeSignature(headerB64, payloadB64);
        return headerB64 + "." + payloadB64 + "." + signature;
    }

    private String createTokenWithoutSignature(Map<String, Object> payload) throws Exception {
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String headerB64 = base64UrlEncode(header);
        String payloadJson = mapper.writeValueAsString(payload);
        String payloadB64 = base64UrlEncode(payloadJson);
        return headerB64 + "." + payloadB64 + ".fake-signature";
    }

    private String base64UrlEncode(String input) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    private String computeSignature(String headerB64, String payloadB64) {
        try {
            String data = headerB64 + "." + payloadB64;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec =
                    new SecretKeySpec(TEST_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] computed = mac.doFinal(data.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(computed);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
