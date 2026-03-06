package com.tomassirio.wanderer.commons.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Externalized rate-limiting configuration.
 *
 * <p>Properties are bound from the {@code app.rate-limit.*} namespace and can be overridden per
 * service via its Helm {@code values.yaml} / ConfigMap.
 *
 * <ul>
 *   <li><b>enabled</b> — master switch (default {@code true})
 *   <li><b>max-requests</b> — maximum requests per window (default {@code 100})
 *   <li><b>window-seconds</b> — window duration in seconds (default {@code 60})
 * </ul>
 *
 * @since 0.9.2
 */
@Configuration
@ConfigurationProperties(prefix = "app.rate-limit")
@Getter
@Setter
public class RateLimitProperties {

    /** Whether rate limiting is enabled globally. */
    private boolean enabled = true;

    /** Maximum number of requests allowed per window. */
    private int maxRequests = 100;

    /** Duration of the rate-limit window in seconds. */
    private int windowSeconds = 60;
}
