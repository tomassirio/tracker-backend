package com.tomassirio.wanderer.auth.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class FeignConfigTest {

    private FeignConfig feignConfig;

    @BeforeEach
    void setUp() {
        feignConfig = new FeignConfig();
    }

    @Test
    void interceptor_whenAuthorizationHeaderPresent_shouldForwardIt() {
        // Given
        RequestInterceptor interceptor = feignConfig.authorizationForwardingInterceptor();
        RequestTemplate template = new RequestTemplate();

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer test-token");

        ServletRequestAttributes attrs = new ServletRequestAttributes(mockRequest);
        RequestContextHolder.setRequestAttributes(attrs);

        try {
            // When
            interceptor.apply(template);

            // Then
            Map<String, Collection<String>> headers = template.headers();
            assertThat(headers).containsKey(HttpHeaders.AUTHORIZATION);
            assertThat(headers.get(HttpHeaders.AUTHORIZATION)).containsExactly("Bearer test-token");
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void interceptor_whenNoAuthorizationHeader_shouldNotAddIt() {
        // Given
        RequestInterceptor interceptor = feignConfig.authorizationForwardingInterceptor();
        RequestTemplate template = new RequestTemplate();

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        ServletRequestAttributes attrs = new ServletRequestAttributes(mockRequest);
        RequestContextHolder.setRequestAttributes(attrs);

        try {
            // When
            interceptor.apply(template);

            // Then
            Map<String, Collection<String>> headers = template.headers();
            assertThat(headers).doesNotContainKey(HttpHeaders.AUTHORIZATION);
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void interceptor_whenNoRequestContext_shouldNotAddHeader() {
        // Given
        RequestInterceptor interceptor = feignConfig.authorizationForwardingInterceptor();
        RequestTemplate template = new RequestTemplate();

        RequestContextHolder.resetRequestAttributes();

        // When
        interceptor.apply(template);

        // Then
        Map<String, Collection<String>> headers = template.headers();
        assertThat(headers).doesNotContainKey(HttpHeaders.AUTHORIZATION);
    }
}
