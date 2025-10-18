package com.tomassirio.wanderer.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tomassirio.wanderer.auth.service.impl.JwtServiceImpl;
import com.tomassirio.wanderer.commons.domain.User;
import com.tomassirio.wanderer.commons.security.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    private JwtServiceImpl jwtService;

    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtServiceImpl();
        testUser = User.builder().id(UUID.randomUUID()).username("testuser").build();

        // Set secret via reflection (must be at least 32 bytes for HS256)
        Field secretField = JwtServiceImpl.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(jwtService, "mySecretKeyForTestingPurposesOnlyWith32BytesMinimum");

        // Set expirationMs via reflection
        Field expirationField = JwtServiceImpl.class.getDeclaredField("expirationMs");
        expirationField.setAccessible(true);
        expirationField.set(jwtService, 3600000L);

        // Set refreshExpirationMs via reflection
        Field refreshExpirationField = JwtServiceImpl.class.getDeclaredField("refreshExpirationMs");
        refreshExpirationField.setAccessible(true);
        refreshExpirationField.set(jwtService, 604800000L);
    }

    @Test
    void generateToken_shouldReturnValidToken() {
        String token = jwtService.generateToken(testUser);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateToken_shouldContainCorrectClaims() {
        String token = jwtService.generateToken(testUser);
        Claims claims = jwtService.parseToken(token);

        assertEquals(testUser.getId().toString(), claims.getSubject());
        assertEquals(testUser.getUsername(), claims.get("username"));
        assertNotNull(claims.get("roles"));
        assertInstanceOf(List.class, claims.get("roles"));
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("roles");
        assertEquals(1, roles.size());
        assertEquals(Role.USER.name(), roles.getFirst());
    }

    @Test
    void generateToken_shouldSetIssuedAtAndExpiration() {
        String token = jwtService.generateToken(testUser);
        Claims claims = jwtService.parseToken(token);

        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());

        long actualExpirationDuration = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();

        // Allow for up to 1 second difference due to JWT's second-level precision
        assertTrue(Math.abs(actualExpirationDuration - 3600000L) <= 1000L,
                "Expected duration close to 3600000ms, but was " + actualExpirationDuration + "ms");
    }

    @Test
    void generateTokenWithJti_shouldReturnValidTokenWithJti() {
        String jti = UUID.randomUUID().toString();
        String token = jwtService.generateTokenWithJti(testUser, jti);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        Claims claims = jwtService.parseToken(token);
        assertEquals(jti, claims.getId());
    }

    @Test
    void generateTokenWithJti_shouldContainCorrectClaims() {
        String jti = UUID.randomUUID().toString();
        String token = jwtService.generateTokenWithJti(testUser, jti);
        Claims claims = jwtService.parseToken(token);

        assertEquals(testUser.getId().toString(), claims.getSubject());
        assertEquals(testUser.getUsername(), claims.get("username"));
        assertEquals(jti, claims.getId());
        assertNotNull(claims.get("roles"));
        assertInstanceOf(List.class, claims.get("roles"));
    }

    @Test
    void generateTokenWithJti_shouldSetIssuedAtAndExpiration() {
        String jti = UUID.randomUUID().toString();
        String token = jwtService.generateTokenWithJti(testUser, jti);
        Claims claims = jwtService.parseToken(token);

        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());

        long actualExpirationDuration = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();

        // Allow for up to 1 second difference due to JWT's second-level precision
        assertTrue(Math.abs(actualExpirationDuration - 3600000L) <= 1000L,
                "Expected duration close to 3600000ms, but was " + actualExpirationDuration + "ms");
    }

    @Test
    void parseToken_shouldReturnClaimsWithCorrectSubject() {
        String token = jwtService.generateToken(testUser);

        Claims claims = jwtService.parseToken(token);

        assertEquals(testUser.getId().toString(), claims.getSubject());
        assertEquals(testUser.getUsername(), claims.get("username"));
        assertNotNull(claims.get("roles"));
    }

    @Test
    void parseToken_shouldThrowExceptionForInvalidToken() {
        String invalidToken = "invalid.token.value";

        assertThrows(MalformedJwtException.class, () -> jwtService.parseToken(invalidToken));
    }

    @Test
    void parseToken_shouldThrowExceptionForTokenWithWrongSignature() throws Exception {
        // Generate token with one secret
        String token = jwtService.generateToken(testUser);

        // Create a new instance with a different secret (must be at least 32 bytes for HS256)
        JwtServiceImpl differentJwtService = new JwtServiceImpl();
        Field secretField = JwtServiceImpl.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(differentJwtService, "aDifferentSecretKeyForTestingWith32BytesMinimum!!");

        // Parsing should fail due to signature mismatch
        assertThrows(SignatureException.class, () -> differentJwtService.parseToken(token));
    }

    @Test
    void parseToken_shouldThrowExceptionForExpiredToken() throws Exception {
        // Set expiration to -1 millisecond (already expired)
        Field expirationField = JwtServiceImpl.class.getDeclaredField("expirationMs");
        expirationField.setAccessible(true);
        expirationField.set(jwtService, -1L);

        String token = jwtService.generateToken(testUser);

        // Wait a tiny bit to ensure token is definitely expired
        Thread.sleep(10);

        assertThrows(ExpiredJwtException.class, () -> jwtService.parseToken(token));
    }

    @Test
    void parseToken_shouldThrowExceptionForNullToken() {
        // JJWT throws IllegalArgumentException for null tokens
        assertThrows(Exception.class, () -> jwtService.parseToken(null));
    }

    @Test
    void parseToken_shouldThrowExceptionForEmptyToken() {
        // JJWT throws MalformedJwtException for empty tokens
        assertThrows(Exception.class, () -> jwtService.parseToken(""));
    }

    @Test
    void getExpirationMs_shouldReturnConfiguredValue() {
        long expirationMs = jwtService.getExpirationMs();

        assertEquals(3600000L, expirationMs);
    }

    @Test
    void getRefreshExpirationMs_shouldReturnConfiguredValue() {
        long refreshExpirationMs = jwtService.getRefreshExpirationMs();

        assertEquals(604800000L, refreshExpirationMs);
    }

    @Test
    void generateToken_withDifferentUsers_shouldGenerateDifferentTokens() {
        User user1 = User.builder().id(UUID.randomUUID()).username("user1").build();
        User user2 = User.builder().id(UUID.randomUUID()).username("user2").build();

        String token1 = jwtService.generateToken(user1);
        String token2 = jwtService.generateToken(user2);

        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);
    }

    @Test
    void generateTokenWithJti_withDifferentJtis_shouldGenerateDifferentTokens() {
        String jti1 = UUID.randomUUID().toString();
        String jti2 = UUID.randomUUID().toString();

        String token1 = jwtService.generateTokenWithJti(testUser, jti1);
        String token2 = jwtService.generateTokenWithJti(testUser, jti2);

        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);

        Claims claims1 = jwtService.parseToken(token1);
        Claims claims2 = jwtService.parseToken(token2);

        assertEquals(jti1, claims1.getId());
        assertEquals(jti2, claims2.getId());
    }

    @Test
    void generateToken_shouldBeValidForEntireExpirationPeriod() {
        String token = jwtService.generateToken(testUser);
        Claims claims = jwtService.parseToken(token);

        // Token should be valid now
        assertTrue(claims.getExpiration().after(new Date()));
    }
}
