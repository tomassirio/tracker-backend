package com.tomassirio.wanderer.commons.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.tomassirio.wanderer.commons.config.properties.RateLimitProperties;
import com.tomassirio.wanderer.commons.security.RateLimitFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;

class RateLimitConfigTest {

    private final RateLimitConfig config = new RateLimitConfig();

    @Test
    void shouldCreateRateLimitFilterBean() {
        RateLimitProperties properties = new RateLimitProperties();
        RateLimitFilter filter = config.rateLimitFilter(properties);
        assertThat(filter).isNotNull();
    }

    @Test
    void shouldRegisterFilterWithHighestPrecedence() {
        RateLimitProperties properties = new RateLimitProperties();
        RateLimitFilter filter = config.rateLimitFilter(properties);

        FilterRegistrationBean<RateLimitFilter> registration =
                config.rateLimitFilterRegistration(filter);

        assertThat(registration.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
        assertThat(registration.getUrlPatterns()).containsExactly("/*");
    }
}
