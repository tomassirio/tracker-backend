package com.tomassirio.wanderer.command;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
        scanBasePackages = {"com.tomassirio.wanderer.command", "com.tomassirio.wanderer.commons"})
@EntityScan(
        basePackages = {
            "com.tomassirio.wanderer.command.entity",
            "com.tomassirio.wanderer.commons.domain"
        })
@EnableJpaRepositories(
        basePackages = {
            "com.tomassirio.wanderer.command.repository",
            "com.tomassirio.wanderer.commons.repository"
        })
public class TrackerCommandApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrackerCommandApplication.class, args);
    }
}
