package com.tomassirio.wanderer.query;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = {"com.tomassirio.wanderer.query", "com.tomassirio.wanderer.commons"})
public class TrackerQueryApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrackerQueryApplication.class, args);
    }
}
