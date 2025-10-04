package com.tomassirio.wanderer.commons.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        basePackages = {
            "com.tomassirio.wanderer.command.repository",
            "com.tomassirio.wanderer.query.repository"
        })
@EntityScan("com.tomassirio.wanderer.commons.domain")
public class TestJpaConfiguration {}
