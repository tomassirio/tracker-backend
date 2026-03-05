package com.tomassirio.wanderer.commons.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.tomassirio.wanderer.commons.config.properties.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    private RateLimitProperties properties;
    private RateLimitFilter filter;

    @Mock private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        properties = new RateLimitProperties();
        properties.setEnabled(true);
        properties.setMaxRequests(5);
        properties.setWindowSeconds(60);
        filter = new RateLimitFilter(properties);
    }

    @Test
    void shouldAllowRequestWithinLimit() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/1/trips");
        request.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(response.getHeader(RateLimitFilter.HEADER_RATE_LIMIT)).isEqualTo("5");
        assertThat(response.getHeader(RateLimitFilter.HEADER_RATE_REMAINING)).isEqualTo("4");
        assertThat(response.getHeader(RateLimitFilter.HEADER_RATE_RESET)).isNotNull();
    }

    @Test
    void shouldBlockRequestWhenLimitExceeded() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/1/trips");
        request.setRemoteAddr("10.0.0.2");

        // Exhaust the limit
        for (int i = 0; i < 5; i++) {
            MockHttpServletResponse resp = new MockHttpServletResponse();
            filter.doFilterInternal(request, resp, filterChain);
        }

        // Next request should be blocked
        MockHttpServletResponse blockedResponse = new MockHttpServletResponse();
        filter.doFilterInternal(request, blockedResponse, filterChain);

        assertThat(blockedResponse.getStatus()).isEqualTo(429);
        assertThat(blockedResponse.getContentAsString()).contains("Too many requests");
        assertThat(blockedResponse.getHeader(RateLimitFilter.HEADER_RETRY_AFTER)).isNotNull();
        // filterChain should only have been called 5 times, not 6
        verify(filterChain, times(5)).doFilter(any(), any());
    }

    @Test
    void shouldApplySameLimitToAllEndpoints() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/1/auth/login");
        request.setRemoteAddr("10.0.0.3");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        // Auth endpoints use the same limit as all other endpoints
        assertThat(response.getHeader(RateLimitFilter.HEADER_RATE_LIMIT)).isEqualTo("5");
    }

    @Test
    void shouldTrackDifferentIpsSeparately() throws ServletException, IOException {
        // Exhaust limit for IP 10.0.0.4
        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/1/trips");
            req.setRemoteAddr("10.0.0.4");
            filter.doFilterInternal(req, new MockHttpServletResponse(), filterChain);
        }

        // Different IP should still be allowed
        MockHttpServletRequest otherIpRequest = new MockHttpServletRequest("GET", "/api/1/trips");
        otherIpRequest.setRemoteAddr("10.0.0.5");
        MockHttpServletResponse otherIpResponse = new MockHttpServletResponse();
        filter.doFilterInternal(otherIpRequest, otherIpResponse, filterChain);

        assertThat(otherIpResponse.getStatus()).isEqualTo(200);
        // 5 from first IP + 1 from second IP
        verify(filterChain, times(6)).doFilter(any(), any());
    }

    @Test
    void shouldBypassFilterWhenDisabled() throws ServletException, IOException {
        properties.setEnabled(false);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/1/trips");
        request.setRemoteAddr("10.0.0.6");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(response.getHeader(RateLimitFilter.HEADER_RATE_LIMIT)).isNull();
    }

    @Test
    void shouldUseXForwardedForHeaderWhenPresent() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/1/trips");
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("X-Forwarded-For", "203.0.113.50, 70.41.3.18, 150.172.238.178");

        String clientIp = filter.resolveClientIp(request);

        assertThat(clientIp).isEqualTo("203.0.113.50");
    }

    @Test
    void shouldFallBackToRemoteAddrWhenNoXForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/1/trips");
        request.setRemoteAddr("192.168.1.100");

        String clientIp = filter.resolveClientIp(request);

        assertThat(clientIp).isEqualTo("192.168.1.100");
    }

    @Test
    void shouldShareSingleBucketAcrossEndpointsForSameIp()
            throws ServletException, IOException {
        String ip = "10.0.0.7";

        // Use 3 requests on auth endpoint
        for (int i = 0; i < 3; i++) {
            MockHttpServletRequest authReq =
                    new MockHttpServletRequest("POST", "/api/1/auth/login");
            authReq.setRemoteAddr(ip);
            filter.doFilterInternal(authReq, new MockHttpServletResponse(), filterChain);
        }

        // Use remaining 2 requests on a different endpoint
        for (int i = 0; i < 2; i++) {
            MockHttpServletRequest tripReq = new MockHttpServletRequest("GET", "/api/1/trips");
            tripReq.setRemoteAddr(ip);
            filter.doFilterInternal(tripReq, new MockHttpServletResponse(), filterChain);
        }

        // 6th request (any endpoint) should be blocked
        MockHttpServletRequest blocked = new MockHttpServletRequest("GET", "/api/1/trips");
        blocked.setRemoteAddr(ip);
        MockHttpServletResponse blockedResp = new MockHttpServletResponse();
        filter.doFilterInternal(blocked, blockedResp, filterChain);
        assertThat(blockedResp.getStatus()).isEqualTo(429);

        verify(filterChain, times(5)).doFilter(any(), any());
    }

    @Test
    void shouldSetCorrectRateLimitHeaders() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/1/trips");
        request.setRemoteAddr("10.0.0.8");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getHeader(RateLimitFilter.HEADER_RATE_LIMIT)).isEqualTo("5");
        assertThat(response.getHeader(RateLimitFilter.HEADER_RATE_REMAINING)).isEqualTo("4");
        assertThat(response.getHeader(RateLimitFilter.HEADER_RATE_RESET)).isNotNull();

        // Second request
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        filter.doFilterInternal(request, response2, filterChain);
        assertThat(response2.getHeader(RateLimitFilter.HEADER_RATE_REMAINING)).isEqualTo("3");
    }

    @Test
    void clientBucket_shouldExpireAfterWindow() {
        long startTime = System.currentTimeMillis();
        RateLimitFilter.ClientBucket bucket = new RateLimitFilter.ClientBucket(10, 60, startTime);

        assertThat(bucket.isExpired(startTime + 59_999)).isFalse();
        assertThat(bucket.isExpired(startTime + 60_000)).isTrue();
        assertThat(bucket.isExpired(startTime + 60_001)).isTrue();
    }

    @Test
    void clientBucket_tryConsume_shouldTrackCorrectly() {
        RateLimitFilter.ClientBucket bucket =
                new RateLimitFilter.ClientBucket(3, 60, System.currentTimeMillis());

        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.getRemaining()).isEqualTo(2);
        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.getRemaining()).isEqualTo(1);
        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.getRemaining()).isEqualTo(0);
        assertThat(bucket.tryConsume()).isFalse();
        assertThat(bucket.getRemaining()).isEqualTo(0);
    }

    @Test
    void clientBucket_getResetEpochSecond_shouldReturnCorrectValue() {
        long startTime = 1000000L; // 1000 seconds
        RateLimitFilter.ClientBucket bucket = new RateLimitFilter.ClientBucket(10, 60, startTime);

        // (1000000 + 60000) / 1000 = 1060
        assertThat(bucket.getResetEpochSecond()).isEqualTo(1060);
    }
}

