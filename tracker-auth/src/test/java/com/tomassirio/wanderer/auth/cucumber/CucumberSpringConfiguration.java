package com.tomassirio.wanderer.auth.cucumber;

import static io.cucumber.core.options.Constants.FEATURES_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.PLUGIN_PROPERTY_NAME;

import com.tomassirio.wanderer.auth.AuthApplication;
import com.tomassirio.wanderer.auth.client.TrackerCommandClient;
import com.tomassirio.wanderer.auth.client.TrackerQueryClient;
import com.tomassirio.wanderer.commons.cucumber.BaseCucumberSpringConfiguration;
import com.tomassirio.wanderer.commons.domain.User;
import io.cucumber.spring.CucumberContextConfiguration;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.UUID;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ConfigurationParameter(
        key = PLUGIN_PROPERTY_NAME,
        value = "pretty,html:target/cucumber-report.html")
@ConfigurationParameter(key = FEATURES_PROPERTY_NAME, value = "src/test/resources/features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.tomassirio.wanderer.auth.cucumber")
@ConfigurationParameter(key = "cucumber.publish.quiet", value = "true")
@CucumberContextConfiguration
@SpringBootTest(
        classes = AuthApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfiguration extends BaseCucumberSpringConfiguration {

    @MockitoBean private TrackerCommandClient trackerCommandClient;

    @MockitoBean private TrackerQueryClient trackerQueryClient;

    // Configure mocks
    @PostConstruct
    public void setupMocks() {
        User dummyUser = User.builder().id(UUID.randomUUID()).username("testuser").build();

        Mockito.when(trackerCommandClient.createUser(Mockito.any(Map.class))).thenReturn(dummyUser);
        Mockito.when(trackerQueryClient.getUserByUsername("testuser")).thenReturn(dummyUser);
        Mockito.when(trackerQueryClient.getUserByUsername("nonexistent")).thenReturn(null);
    }
}
