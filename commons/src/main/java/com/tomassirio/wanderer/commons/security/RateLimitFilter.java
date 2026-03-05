package com.tomassirio.wanderer.commons.security;

import com.tomassirio.wanderer.commons.config.properties.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Per-IP rate-limiting filter using a fixed-window counter stored in memory. Each service configures
 * its own limits via {@code app.rate-limit.*} properties (set per app through Helm values), so no
 * endpoint-specific branching is needed here.
 *
 * <p>Rate-limit metadata is exposed via response headers:
 *
 * <ul>
 *   <li>{@code X-RateLimit-Limit} — maximum requests in the current window
 *   <li>{@code X-RateLimit-Remaining} — remaining requests in the current window
 *   <li>{@code X-RateLimit-Reset} — epoch-second when the window resets
 * </ul>
 *
 * @since 0.9.2
 */
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    static final String HEADER_RATE_LIMIT = "X-RateLimit-Limit";
    static final String HEADER_RATE_REMAINING = "X-RateLimit-Remaining";
    static final String HEADER_RATE_RESET = "X-RateLimit-Reset";
    static final String HEADER_RETRY_AFTER = "Retry-After";

    private final RateLimitProperties properties;
    private final Map<String, ClientBucket> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(RateLimitProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);
        int maxReqs = properties.getMaxRequests();
        int winSecs = properties.getWindowSeconds();

        ClientBucket bucket =
                buckets.compute(
                        clientIp,
                        (key, existing) -> {
                            long now = System.currentTimeMillis();
                            if (existing == null || existing.isExpired(now)) {
                                return new ClientBucket(maxReqs, winSecs, now);
                            }
                            return existing;
                        });

        boolean allowed = bucket.tryConsume();
        long resetEpoch = bucket.getResetEpochSecond();

        response.setIntHeader(HEADER_RATE_LIMIT, maxReqs);
        response.setIntHeader(HEADER_RATE_REMAINING, Math.max(0, bucket.getRemaining()));
        response.setHeader(HEADER_RATE_RESET, String.valueOf(resetEpoch));

        if (!allowed) {
            long retryAfter = Math.max(1, resetEpoch - (System.currentTimeMillis() / 1000));
            response.setHeader(HEADER_RETRY_AFTER, String.valueOf(retryAfter));
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter()
                    .write("{\"error\":\"Too many requests. Please try again later.\"}");
            log.warn(
                    "Rate limit exceeded for client {} on {} {}",
                    clientIp,
                    request.getMethod(),
                    request.getRequestURI());
            return;
        }

        filterChain.doFilter(request, response);
    }

    String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    Map<String, ClientBucket> getBuckets() {
        return buckets;
    }

    static class ClientBucket {
        private final int maxRequests;
        private final long windowMillis;
        private final long windowStartMillis;
        private final AtomicInteger counter;

        ClientBucket(int maxRequests, int windowSeconds, long startMillis) {
            this.maxRequests = maxRequests;
            this.windowMillis = windowSeconds * 1000L;
            this.windowStartMillis = startMillis;
            this.counter = new AtomicInteger(0);
        }

        boolean isExpired(long nowMillis) {
            return nowMillis - windowStartMillis >= windowMillis;
        }

        boolean tryConsume() {
            return counter.incrementAndGet() <= maxRequests;
        }

        int getRemaining() {
            return Math.max(0, maxRequests - counter.get());
        }

        long getResetEpochSecond() {
            return (windowStartMillis + windowMillis) / 1000;
        }
    }
}


