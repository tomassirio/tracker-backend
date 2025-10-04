package com.tomassirio.wanderer.commons.config;

import com.tomassirio.wanderer.commons.config.properties.DataSourceProperties;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class responsible for creating the DataSource bean based on the loaded properties.
 */
@Configuration
@EnableConfigurationProperties(DataSourceProperties.class)
@RequiredArgsConstructor
public class DatabaseConfig {

    private final DataSourceProperties dataSourceProperties;

    @Bean
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(dataSourceProperties.getUrl());
        dataSource.setUsername(dataSourceProperties.getUsername());
        dataSource.setPassword(dataSourceProperties.getPassword());
        dataSource.setDriverClassName(dataSourceProperties.getDriverClassName());
        return dataSource;
    }
}
