package com.tomassirio.wanderer.command.config;

import com.tomassirio.wanderer.commons.config.CorsConfig;
import com.tomassirio.wanderer.commons.config.OpenApiConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/** Application configuration that imports shared configurations from the commons module. */
@Configuration
@ComponentScan(basePackages = "com.tomassirio.wanderer")
@Import({CorsConfig.class, OpenApiConfig.class})
public class AppConfig {}
