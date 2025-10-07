package com.tomassirio.wanderer.command.cucumber;

import io.cucumber.spring.ScenarioScope;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.stereotype.Component;

import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.repository.UserRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;

@Component
@ScenarioScope
public class TestScenarioContext {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private UserRepository userRepository;
    @Autowired private TripRepository tripRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    // per-scenario state
    @Getter
    @Setter
    private String tempAuthHeader;
    @Getter
    @Setter
    private String lastCreatedUsername;

    public TestRestTemplate rest() {
        return restTemplate;
    }

    public UserRepository users() {
        return userRepository;
    }

    public TripRepository trips() {
        return tripRepository;
    }

    public String buildJwt(String subject, String scopes, String secret, Map<String, Object> extraClaims)
            throws NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", subject);
        payload.put("scopes", scopes);
        if (extraClaims != null) payload.putAll(extraClaims);
        String headerJson = mapper.writeValueAsString(header);
        String payloadJson = mapper.writeValueAsString(payload);
        String headerB64 = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
        String payloadB64 = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signingInput = headerB64 + "." + payloadB64;

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] sig = mac.doFinal(signingInput.getBytes(StandardCharsets.US_ASCII));
        String sigB64 = base64UrlEncode(sig);
        return signingInput + "." + sigB64;
    }

    private static String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

