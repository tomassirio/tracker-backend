package com.tomassirio.wanderer.query;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(
        scanBasePackages = {"com.tomassirio.wanderer.query", "com.tomassirio.wanderer.commons"})
@EnableFeignClients(basePackages = {"com.tomassirio.wanderer.commons.client"})
public class TrackerQueryApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrackerQueryApplication.class, args);
    }
}
