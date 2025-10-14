package com.tomassirio.wanderer.commons.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class JwtBuilder {

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static String buildJwt(Map<String, Object> payload, String secret)
            throws NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        String headerJson = mapper.writeValueAsString(header);
        String payloadJson = mapper.writeValueAsString(payload);
        String headerB64 = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
        String payloadB64 = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signingInput = headerB64 + "." + payloadB64;

        Mac mac = Mac.getInstance(SignatureAlgorithm.HS256.getJcaName());
        SecretKeySpec keySpec =
                new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] sig = mac.doFinal(signingInput.getBytes(StandardCharsets.US_ASCII));
        String sigB64 = base64UrlEncode(sig);
        return signingInput + "." + sigB64;
    }

    private static String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
