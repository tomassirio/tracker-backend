package com.tomassirio.wanderer.command;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = {"com.tomassirio.wanderer.command", "com.tomassirio.wanderer.commons"})
public class TrackerCommandApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrackerCommandApplication.class, args);
    }
}
