package com.tomassirio.wanderer.auth.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * Configuration class to specify the base packages for JPA entity and repository scanning. This is
 * necessary because entities and repositories are in different packages or modules. This
 * configuration is only loaded if TestJpaConfiguration is not present.
 */
@Configuration
@ConditionalOnMissingBean(name = "testJpaConfiguration")
@EnableJpaRepositories(basePackages = "com.tomassirio.wanderer.auth.repository")
@EntityScan({"com.tomassirio.wanderer.commons.domain", "com.tomassirio.wanderer.auth.domain"})
public class JpaConfiguration {}
