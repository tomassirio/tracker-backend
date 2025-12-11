package com.tomassirio.wanderer.auth.config;

import com.tomassirio.wanderer.commons.config.CorsConfig;
import com.tomassirio.wanderer.commons.config.OpenApiConfig;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/** Application configuration that imports shared configurations from the commons module. */
@Configuration
@EnableFeignClients(basePackages = "com.tomassirio.wanderer.auth.client")
@ComponentScan(basePackages = "com.tomassirio.wanderer")
@Import({CorsConfig.class, OpenApiConfig.class})
public class AppConfig {}
