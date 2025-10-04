package com.tomassirio.wanderer.command.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Type-safe configuration properties for the database connection. Binds to properties with the
 * prefix "db".
 */
@Component
@ConfigurationProperties(prefix = "db")
@Data
@Validated
public class DataSourceProperties {

    @NotBlank private String url;

    @NotBlank private String username;

    @NotBlank private String password;

    @NotBlank private String driverClassName;
}
