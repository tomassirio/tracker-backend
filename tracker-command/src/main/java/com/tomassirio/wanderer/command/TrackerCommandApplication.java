package com.tomassirio.wanderer.command;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(
        scanBasePackages = {"com.tomassirio.wanderer.command", "com.tomassirio.wanderer.commons"})
@EnableFeignClients(basePackages = {"com.tomassirio.wanderer.commons.client"})
public class TrackerCommandApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrackerCommandApplication.class, args);
    }
}
