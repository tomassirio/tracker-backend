package com.tomassirio.wanderer.command.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
@EntityScan("com.tomassirio.wanderer.commons.domain")
public class TestJpaConfiguration {}
