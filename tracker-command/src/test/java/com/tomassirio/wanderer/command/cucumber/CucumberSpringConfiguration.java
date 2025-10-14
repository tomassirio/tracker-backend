package com.tomassirio.wanderer.command.cucumber;

import static io.cucumber.junit.platform.engine.Constants.*;

import com.tomassirio.wanderer.command.TrackerCommandApplication;
import com.tomassirio.wanderer.commons.cucumber.BaseCucumberSpringConfiguration;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.springframework.boot.test.context.SpringBootTest;

@ConfigurationParameter(
        key = PLUGIN_PROPERTY_NAME,
        value = "pretty,html:target/cucumber-report.html")
@ConfigurationParameter(key = FEATURES_PROPERTY_NAME, value = "src/test/resources/features")
@ConfigurationParameter(
        key = GLUE_PROPERTY_NAME,
        value = "com.tomassirio.wanderer.command.cucumber")
@ConfigurationParameter(key = "cucumber.publish.quiet", value = "true")
@CucumberContextConfiguration
@SpringBootTest(
        classes = TrackerCommandApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfiguration extends BaseCucumberSpringConfiguration {}
