package com.tomassirio.wanderer.auth.cucumber;

import com.tomassirio.wanderer.auth.AuthApplication;
import com.tomassirio.wanderer.auth.client.TrackerCommandClient;
import com.tomassirio.wanderer.auth.client.TrackerQueryClient;
import com.tomassirio.wanderer.commons.cucumber.BaseCucumberSpringConfiguration;
import com.tomassirio.wanderer.commons.domain.User;
import io.cucumber.spring.CucumberContextConfiguration;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.UUID;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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
        User dummyUser =
                User.builder()
                        .id(UUID.randomUUID())
                        .username("testuser")
                        .email("test@example.com")
                        .build();

        Mockito.when(trackerCommandClient.createUser(Mockito.any(Map.class))).thenReturn(dummyUser);
        Mockito.when(trackerQueryClient.getUserByUsername("testuser")).thenReturn(dummyUser);
        Mockito.when(trackerQueryClient.getUserByUsername("nonexistent")).thenReturn(null);
    }
}
