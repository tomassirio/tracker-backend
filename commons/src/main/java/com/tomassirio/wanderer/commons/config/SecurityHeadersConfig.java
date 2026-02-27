package com.tomassirio.wanderer.commons.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.header.writers.ContentSecurityPolicyHeaderWriter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

/**
 * Shared security headers configuration. Provides a customizer that modules can apply to their
 * SecurityFilterChain to enforce consistent security response headers across all services.
 *
 * <p>Configures the following headers:
 *
 * <ul>
 *   <li><b>X-Content-Type-Options: nosniff</b> — Prevents MIME-type sniffing attacks
 *   <li><b>X-Frame-Options: DENY</b> — Prevents clickjacking by disallowing framing
 *   <li><b>X-XSS-Protection: 0</b> — Disables legacy XSS auditor (CSP is preferred)
 *   <li><b>Content-Security-Policy</b> — Restricts resource loading to same origin
 *   <li><b>Referrer-Policy: strict-origin-when-cross-origin</b> — Limits referrer leakage
 *   <li><b>Permissions-Policy</b> — Restricts browser feature access
 *   <li><b>Cache-Control / Pragma / Expires</b> — Prevents sensitive data caching
 * </ul>
 *
 * @since 0.6.0
 */
@Configuration
public class SecurityHeadersConfig {

    /**
     * Provides a reusable headers customizer bean that each module's SecurityConfig can apply.
     *
     * @return a SecurityHeadersCustomizer instance
     */
    @Bean
    public SecurityHeadersCustomizer securityHeadersCustomizer() {
        return new SecurityHeadersCustomizer();
    }

    /**
     * Encapsulates the shared security headers configuration so that each module can apply it
     * consistently to its own SecurityFilterChain.
     */
    public static class SecurityHeadersCustomizer {

        /**
         * Applies security headers to the given HeadersConfigurer.
         *
         * @param headers the HeadersConfigurer to configure
         */
        public void configure(
                org.springframework.security.config.annotation.web.configurers.HeadersConfigurer<
                                org.springframework.security.config.annotation.web.builders
                                        .HttpSecurity>
                        headers) {
            headers
                    // X-Content-Type-Options: nosniff — prevents MIME sniffing
                    .contentTypeOptions(Customizer.withDefaults())
                    // X-Frame-Options: DENY — prevents clickjacking
                    .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                    // X-XSS-Protection: 0 — disable legacy XSS auditor; CSP is preferred
                    .xssProtection(
                            xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.DISABLED))
                    // Content-Security-Policy — restrict resource origins
                    .addHeaderWriter(
                            new ContentSecurityPolicyHeaderWriter(
                                    "default-src 'self'; frame-ancestors 'none'"))
                    // Referrer-Policy — limit referrer information leakage
                    .referrerPolicy(
                            referrer ->
                                    referrer.policy(
                                            ReferrerPolicyHeaderWriter.ReferrerPolicy
                                                    .STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                    // Permissions-Policy — restrict browser features
                    .addHeaderWriter(
                            new StaticHeadersWriter(
                                    "Permissions-Policy",
                                    "geolocation=(), camera=(), microphone=()"))
                    // Cache-Control: no-cache, no-store — prevent caching of sensitive responses
                    .cacheControl(Customizer.withDefaults());
        }
    }
}
