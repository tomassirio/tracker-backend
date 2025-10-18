package com.tomassirio.wanderer.commons.client;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for communicating with the auth service's token validation endpoint. Used by other
 * services to check if a token has been blacklisted.
 */
@FeignClient(name = "auth-service", url = "${tracker.auth.url:http://localhost:8083}")
public interface AuthServiceClient {

    @GetMapping("/internal/api/1/tokens/validate")
    Map<String, Boolean> isTokenBlacklisted(@RequestParam("jti") String jti);
}
