package com.tomassirio.wanderer.command.config;

import com.tomassirio.wanderer.commons.config.CorsConfig;
import com.tomassirio.wanderer.commons.config.DatabaseConfig;
import com.tomassirio.wanderer.commons.config.OpenApiConfig;
import com.tomassirio.wanderer.commons.mapper.CommentMapper;
import com.tomassirio.wanderer.commons.mapper.TripPlanMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/** Application configuration that imports shared configurations from the commons module. */
@Configuration
@ComponentScan(basePackages = "com.tomassirio.wanderer")
@Import({DatabaseConfig.class, CorsConfig.class, OpenApiConfig.class})
public class AppConfig {

    @Bean
    public CommentMapper commentMapper() {
        return CommentMapper.INSTANCE;
    }

    @Bean
    public TripPlanMapper tripPlanMapper() {
        return TripPlanMapper.INSTANCE;
    }
}
