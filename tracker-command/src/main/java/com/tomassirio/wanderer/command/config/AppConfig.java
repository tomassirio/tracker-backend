package com.tomassirio.wanderer.command.config;

import com.tomassirio.wanderer.commons.config.CorsConfig;
import com.tomassirio.wanderer.commons.config.DatabaseConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/** Application configuration that imports shared configurations from the commons module. */
@Configuration
@Import({DatabaseConfig.class, CorsConfig.class})
public class AppConfig {}
