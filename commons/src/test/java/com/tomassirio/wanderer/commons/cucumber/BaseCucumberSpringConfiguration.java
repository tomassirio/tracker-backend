package com.tomassirio.wanderer.commons.cucumber;

import com.tomassirio.wanderer.commons.BaseIntegrationTest;
import com.tomassirio.wanderer.commons.config.TestConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@Import(TestConfig.class)
@TestPropertySource(
        properties = {
            "jwt.secret=test-secret-that-is-long-enough-for-jwt-hmac-sha-algorithm-256-bits-minimum"
        })
public abstract class BaseCucumberSpringConfiguration extends BaseIntegrationTest {}
