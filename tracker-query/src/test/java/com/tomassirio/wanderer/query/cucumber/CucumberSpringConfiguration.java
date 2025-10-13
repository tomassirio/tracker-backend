package com.tomassirio.wanderer.query.cucumber;

import static io.cucumber.junit.platform.engine.Constants.*;

import com.tomassirio.wanderer.commons.cucumber.BaseCucumberSpringConfiguration;
import com.tomassirio.wanderer.query.TrackerQueryApplication;
import com.tomassirio.wanderer.query.repository.TripRepository;
import com.tomassirio.wanderer.query.repository.UserRepository;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ConfigurationParameter(
        key = PLUGIN_PROPERTY_NAME,
        value = "pretty,html:target/cucumber-report.html")
@ConfigurationParameter(key = FEATURES_PROPERTY_NAME, value = "src/test/resources/features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.tomassirio.wanderer.query.cucumber")
@ConfigurationParameter(key = "cucumber.publish.quiet", value = "true")
@CucumberContextConfiguration
@SpringBootTest(
        classes = TrackerQueryApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfiguration extends BaseCucumberSpringConfiguration {

    @MockitoBean public TripRepository tripRepository;

    @MockitoBean public UserRepository userRepository;
}
