package com.tomassirio.wanderer.query.cucumber;

import com.tomassirio.wanderer.commons.cucumber.BaseCucumberSpringConfiguration;
import com.tomassirio.wanderer.query.TrackerQueryApplication;
import com.tomassirio.wanderer.query.repository.CommentRepository;
import com.tomassirio.wanderer.query.repository.FriendRequestRepository;
import com.tomassirio.wanderer.query.repository.FriendshipRepository;
import com.tomassirio.wanderer.query.repository.TripPlanRepository;
import com.tomassirio.wanderer.query.repository.TripRepository;
import com.tomassirio.wanderer.query.repository.TripUpdateRepository;
import com.tomassirio.wanderer.query.repository.UserFollowRepository;
import com.tomassirio.wanderer.query.repository.UserRepository;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@CucumberContextConfiguration
@SpringBootTest(
        classes = TrackerQueryApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfiguration extends BaseCucumberSpringConfiguration {

    @MockitoBean public TripRepository tripRepository;

    @MockitoBean public UserRepository userRepository;

    @MockitoBean public CommentRepository commentRepository;

    @MockitoBean public TripPlanRepository tripPlanRepository;

    @MockitoBean public FriendshipRepository friendshipRepository;

    @MockitoBean public UserFollowRepository userFollowRepository;

    @MockitoBean public FriendRequestRepository friendRequestRepository;

    @MockitoBean public TripUpdateRepository tripUpdateRepository;
}
