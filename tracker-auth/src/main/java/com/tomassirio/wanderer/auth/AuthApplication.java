package com.tomassirio.wanderer.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(
        scanBasePackages = {"com.tomassirio.wanderer.auth", "com.tomassirio.wanderer.commons"})
@EnableFeignClients(basePackages = {"com.tomassirio.wanderer.auth.client"})
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
