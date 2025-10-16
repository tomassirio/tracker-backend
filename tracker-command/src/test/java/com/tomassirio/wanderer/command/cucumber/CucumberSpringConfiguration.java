package com.tomassirio.wanderer.command.cucumber;

import com.tomassirio.wanderer.command.TrackerCommandApplication;
import com.tomassirio.wanderer.commons.cucumber.BaseCucumberSpringConfiguration;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(
        classes = TrackerCommandApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfiguration extends BaseCucumberSpringConfiguration {
}
