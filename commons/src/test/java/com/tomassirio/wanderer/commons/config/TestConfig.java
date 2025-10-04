package com.tomassirio.wanderer.commons.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Test configuration to enable component scanning for all wanderer modules. This is only used in
 * integration tests, keeping the main applications clean.
 */
@TestConfiguration
@ComponentScan(basePackages = "com.tomassirio.wanderer")
public class TestConfig {}
