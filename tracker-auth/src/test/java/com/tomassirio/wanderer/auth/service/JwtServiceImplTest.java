package com.tomassirio.wanderer.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.tomassirio.wanderer.auth.service.impl.JwtServiceImpl;
import com.tomassirio.wanderer.commons.domain.User;
import io.jsonwebtoken.Claims;
import java.lang.reflect.Field;
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
        testUser =
                User.builder()
                        .id(UUID.randomUUID())
                        .username("testuser")
                        .email("test@example.com")
                        .build();

        // Set secret via reflection
        Field secretField = JwtServiceImpl.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(jwtService, "mySecretKeyForTestingPurposesOnly");

        // Set expirationMs via reflection
        Field expirationField = JwtServiceImpl.class.getDeclaredField("expirationMs");
        expirationField.setAccessible(true);
        expirationField.set(jwtService, 3600000L);
    }

    @Test
    void generateToken_shouldReturnValidToken() {
        String token = jwtService.generateToken(testUser);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void parseToken_shouldReturnClaimsWithCorrectSubject() {
        String token = jwtService.generateToken(testUser);

        Claims claims = jwtService.parseToken(token);

        assertEquals(testUser.getId().toString(), claims.getSubject());
        assertEquals(testUser.getUsername(), claims.get("username"));
        assertEquals(testUser.getEmail(), claims.get("email"));
        assertNotNull(claims.get("roles"));
    }

    @Test
    void getExpirationMs_shouldReturnConfiguredValue() {
        long expirationMs = jwtService.getExpirationMs();

        assertEquals(3600000L, expirationMs);
    }
}
