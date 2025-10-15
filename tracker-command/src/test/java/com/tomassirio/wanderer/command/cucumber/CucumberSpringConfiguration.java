package com.tomassirio.wanderer.command.cucumber;

import com.tomassirio.wanderer.command.TrackerCommandApplication;
import com.tomassirio.wanderer.command.repository.CommentRepository;
import com.tomassirio.wanderer.command.repository.TripPlanRepository;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.repository.UserRepository;
import com.tomassirio.wanderer.commons.cucumber.BaseCucumberSpringConfiguration;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@CucumberContextConfiguration
@SpringBootTest(
        classes = TrackerCommandApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfiguration extends BaseCucumberSpringConfiguration {

    @MockitoBean public TripRepository tripRepository;

    @MockitoBean public UserRepository userRepository;

    @MockitoBean public CommentRepository commentRepository;

    @MockitoBean public TripPlanRepository tripPlanRepository;
}
