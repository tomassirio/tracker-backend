package com.tomassirio.wanderer.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
        scanBasePackages = {"com.tomassirio.wanderer.auth", "com.tomassirio.wanderer.commons"})
@EntityScan(
        basePackages = {
            "com.tomassirio.wanderer.auth.domain",
            "com.tomassirio.wanderer.commons.domain"
        })
@EnableJpaRepositories(
        basePackages = {
            "com.tomassirio.wanderer.auth.repository",
            "com.tomassirio.wanderer.commons.repository"
        })
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
