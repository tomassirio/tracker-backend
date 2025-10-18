package com.tomassirio.wanderer.query;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
        scanBasePackages = {"com.tomassirio.wanderer.query", "com.tomassirio.wanderer.commons"})
@EntityScan(
        basePackages = {
            "com.tomassirio.wanderer.query.entity",
            "com.tomassirio.wanderer.commons.domain"
        })
@EnableJpaRepositories(
        basePackages = {
            "com.tomassirio.wanderer.query.repository",
            "com.tomassirio.wanderer.commons.repository"
        })
public class TrackerQueryApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrackerQueryApplication.class, args);
    }
}
