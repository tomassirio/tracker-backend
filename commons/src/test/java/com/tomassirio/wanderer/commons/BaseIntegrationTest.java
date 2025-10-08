package com.tomassirio.wanderer.commons;

import com.tomassirio.wanderer.commons.config.TestJpaConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
@Import(TestJpaConfiguration.class)
public abstract class BaseIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("tracker_test")
                    .withUsername("test")
                    .withPassword("test");

    static {
        // Ensure the container is started eagerly so DynamicPropertySource can read mapped ports
        try {
            if (!postgres.isRunning()) {
                postgres.start();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to start Postgres testcontainer", e);
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("db.url", postgres::getJdbcUrl);
        registry.add("db.username", postgres::getUsername);
        registry.add("db.password", postgres::getPassword);
        registry.add("db.driver-class-name", () -> "org.postgresql.Driver");
    }
}
