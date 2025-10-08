package com.tomassirio.wanderer.commons.cucumber;

import com.tomassirio.wanderer.commons.BaseIntegrationTest;
import com.tomassirio.wanderer.commons.config.TestConfig;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@CucumberContextConfiguration
@Import(TestConfig.class)
@TestPropertySource(properties = {"jwt.secret=test-secret"})
public class CucumberSpringConfiguration extends BaseIntegrationTest {}
