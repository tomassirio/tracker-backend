package com.tomassirio.wanderer.command.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Configuration class to specify the base packages for JPA entity and repository scanning. This is
 * necessary because entities and repositories are in different packages or modules.
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.tomassirio.wanderer.command.repository")
@EntityScan("com.tomassirio.wanderer.commons.domain")
public class JpaConfiguration {}
