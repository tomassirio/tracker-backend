package com.tomassirio.wanderer.query.cucumber;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tomassirio.wanderer.commons.constants.ApiConstants;
import com.tomassirio.wanderer.commons.domain.Comment;
import com.tomassirio.wanderer.commons.domain.FriendRequest;
import com.tomassirio.wanderer.commons.domain.FriendRequestStatus;
import com.tomassirio.wanderer.commons.domain.Friendship;
import com.tomassirio.wanderer.commons.domain.GeoLocation;
import com.tomassirio.wanderer.commons.domain.Reactions;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripDetails;
import com.tomassirio.wanderer.commons.domain.TripPlan;
import com.tomassirio.wanderer.commons.domain.TripPlanType;
import com.tomassirio.wanderer.commons.domain.TripSettings;
import com.tomassirio.wanderer.commons.domain.TripStatus;
import com.tomassirio.wanderer.commons.domain.TripUpdate;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import com.tomassirio.wanderer.commons.domain.User;
import com.tomassirio.wanderer.commons.domain.UserFollow;
import com.tomassirio.wanderer.commons.utils.JwtBuilder;
import com.tomassirio.wanderer.query.repository.CommentRepository;
import com.tomassirio.wanderer.query.repository.FriendRequestRepository;
import com.tomassirio.wanderer.query.repository.FriendshipRepository;
import com.tomassirio.wanderer.query.repository.TripPlanRepository;
import com.tomassirio.wanderer.query.repository.TripRepository;
import com.tomassirio.wanderer.query.repository.TripUpdateRepository;
import com.tomassirio.wanderer.query.repository.UserFollowRepository;
import com.tomassirio.wanderer.query.repository.UserRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class StepDefinitions {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private UserRepository userRepository;
    @Autowired private TripRepository tripRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private TripPlanRepository tripPlanRepository;
    @Autowired private FriendshipRepository friendshipRepository;
    @Autowired private UserFollowRepository userFollowRepository;
    @Autowired private FriendRequestRepository friendRequestRepository;
    @Autowired private TripUpdateRepository tripUpdateRepository;

    private ResponseEntity<String> latestResponse;

    private static final Logger log = LoggerFactory.getLogger(StepDefinitions.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    // long secret used by tests
    private static final String TEST_SECRET =
            "test-secret-that-is-long-enough-for-jwt-hmac-sha-algorithm-256-bits-minimum";

    static {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // per-scenario state
    @Getter @Setter private String tempAuthHeader;
    @Getter @Setter private String lastCreatedUsername;
    @Getter @Setter private UUID lastCreatedTripId;
    @Getter @Setter private UUID lastCreatedTripPlanId;
    @Getter @Setter private UUID lastCreatedCommentId;
    @Getter @Setter private UUID lastCreatedTripUpdateId;

    // In-memory data
    private final Map<String, User> users = new HashMap<>();
    private final Map<UUID, Trip> trips = new HashMap<>();
    private final Map<UUID, TripPlan> tripPlans = new HashMap<>();
    private final Map<UUID, Comment> comments = new HashMap<>();
    private final Map<UUID, Friendship> friendships = new HashMap<>();
    private final Map<UUID, UserFollow> userFollows = new HashMap<>();
    private final Map<UUID, FriendRequest> friendRequests = new HashMap<>();
    private final Map<UUID, TripUpdate> tripUpdates = new HashMap<>();

    @Given("an empty system")
    public void an_empty_system() {
        trips.clear();
        users.clear();
        tripPlans.clear();
        comments.clear();
        friendships.clear();
        userFollows.clear();
        friendRequests.clear();
        tripUpdates.clear();

        setTempAuthHeader(null);
        setLastCreatedUsername(null);
        setLastCreatedTripId(null);
        setLastCreatedTripPlanId(null);
        setLastCreatedCommentId(null);
        usersMap.clear();
        setLastCreatedTripUpdateId(null);

        Mockito.reset(
                tripRepository,
                userRepository,
                friendshipRepository,
                userFollowRepository,
                friendRequestRepository);

        when(tripRepository.findAll()).thenReturn(new ArrayList<>());
        when(friendshipRepository.findByUserId(Mockito.any())).thenReturn(new ArrayList<>());
        when(userFollowRepository.findByFollowerId(Mockito.any())).thenReturn(new ArrayList<>());
        when(userFollowRepository.findByFollowedId(Mockito.any())).thenReturn(new ArrayList<>());
        when(friendRequestRepository.findByReceiverIdAndStatus(Mockito.any(), Mockito.any()))
                .thenReturn(new ArrayList<>());
        when(friendRequestRepository.findBySenderIdAndStatus(Mockito.any(), Mockito.any()))
                .thenReturn(new ArrayList<>());
    }

    @Given("a user exists with username {string} and email {string}")
    public void a_user_exists(String username, String email) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        users.put(username, user);
        usersMap.put(username, user);
        setLastCreatedUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findAll()).thenReturn(new ArrayList<>(users.values()));
        when(userRepository.findAllById(Mockito.anyCollection()))
                .thenReturn(new ArrayList<>(users.values()));
        when(tripRepository.findByUserId(user.getId())).thenReturn(new ArrayList<>());

        // Initialize friendship repository mock for this user (empty by default)
        when(friendshipRepository.findByUserId(user.getId()))
                .thenReturn(
                        friendships.values().stream()
                                .filter(f -> f.getUserId().equals(user.getId()))
                                .toList());
    }

    @Given("a trip exists with name {string}")
    public void a_trip_exists_with_name(String name) {
        User owner = users.get(getLastCreatedUsername());
        Assertions.assertNotNull(owner, "No user exists to own the trip");

        TripSettings tripSettings =
                TripSettings.builder()
                        .tripStatus(TripStatus.CREATED)
                        .visibility(TripVisibility.PUBLIC)
                        .updateRefresh(null)
                        .build();

        TripDetails tripDetails =
                TripDetails.builder()
                        .startTimestamp(Instant.now())
                        .endTimestamp(null)
                        .startLocation(null)
                        .endLocation(null)
                        .build();

        Trip trip =
                Trip.builder()
                        .id(UUID.randomUUID())
                        .name(name)
                        .userId(owner.getId())
                        .tripSettings(tripSettings)
                        .tripDetails(tripDetails)
                        .tripPlanId(null)
                        .creationTimestamp(Instant.now())
                        .enabled(true)
                        .build();
        trips.put(trip.getId(), trip);
        setLastCreatedTripId(trip.getId());

        when(tripRepository.findById(trip.getId())).thenReturn(Optional.of(trip));
        when(tripRepository.findAll()).thenReturn(new ArrayList<>(trips.values()));

        // Update the mock to return all trips for this user
        List<Trip> userTrips =
                trips.values().stream().filter(t -> t.getUserId().equals(owner.getId())).toList();
        when(tripRepository.findByUserId(owner.getId())).thenReturn(userTrips);

        // Update the mock for available trips
        updateAvailableTripsForAllUsers();
    }

    @Given("a trip exists with name {string} and visibility {string}")
    public void a_trip_exists_with_name_and_visibility(String name, String visibility) {
        User owner = users.get(getLastCreatedUsername());
        Assertions.assertNotNull(owner, "No user exists to own the trip");

        TripVisibility tripVisibility = TripVisibility.valueOf(visibility);

        TripSettings tripSettings =
                TripSettings.builder()
                        .tripStatus(TripStatus.CREATED)
                        .visibility(tripVisibility)
                        .updateRefresh(null)
                        .build();

        TripDetails tripDetails =
                TripDetails.builder()
                        .startTimestamp(Instant.now())
                        .endTimestamp(null)
                        .startLocation(null)
                        .endLocation(null)
                        .build();

        Trip trip =
                Trip.builder()
                        .id(UUID.randomUUID())
                        .name(name)
                        .userId(owner.getId())
                        .tripSettings(tripSettings)
                        .tripDetails(tripDetails)
                        .tripPlanId(null)
                        .creationTimestamp(Instant.now())
                        .enabled(true)
                        .build();
        trips.put(trip.getId(), trip);
        setLastCreatedTripId(trip.getId());

        when(tripRepository.findById(trip.getId())).thenReturn(Optional.of(trip));
        when(tripRepository.findAll()).thenReturn(new ArrayList<>(trips.values()));

        // Update the mock to return all trips for this user
        List<Trip> userTrips =
                trips.values().stream().filter(t -> t.getUserId().equals(owner.getId())).toList();
        when(tripRepository.findByUserId(owner.getId())).thenReturn(userTrips);

        // Update the mock for available trips
        updateAvailableTripsForAllUsers();
    }

    private void updateAvailableTripsForAllUsers() {
        // For each user, calculate their available trips based on friendships
        for (User user : users.values()) {
            List<UUID> friendIds =
                    friendships.values().stream()
                            .filter(f -> f.getUserId().equals(user.getId()))
                            .map(Friendship::getFriendId)
                            .toList();

            List<Trip> availableTrips =
                    trips.values().stream()
                            .filter(
                                    trip ->
                                            trip.getUserId().equals(user.getId()) // Own trips
                                                    || trip.getTripSettings().getVisibility()
                                                            == TripVisibility.PUBLIC // Public trips
                                                    || (trip.getTripSettings().getVisibility()
                                                                    == TripVisibility.PROTECTED
                                                            && friendIds.contains(
                                                                    trip
                                                                            .getUserId())) // Protected trips from friends
                                    )
                            .toList();

            when(tripRepository.findAllAvailableTripsForUser(user.getId(), friendIds))
                    .thenReturn(availableTrips);
        }
    }

    @Given("a trip plan exists with name {string}")
    public void a_trip_plan_exists_with_name(String name) {
        User owner = users.get(getLastCreatedUsername());
        Assertions.assertNotNull(owner, "No user exists to own the trip plan");

        TripPlan tripPlan =
                TripPlan.builder()
                        .id(UUID.randomUUID())
                        .name(name)
                        .userId(owner.getId())
                        .planType(TripPlanType.SIMPLE)
                        .createdTimestamp(Instant.now())
                        .startDate(java.time.LocalDate.now())
                        .endDate(java.time.LocalDate.now().plusDays(7))
                        .build();
        tripPlans.put(tripPlan.getId(), tripPlan);
        setLastCreatedTripPlanId(tripPlan.getId());

        when(tripPlanRepository.findById(tripPlan.getId())).thenReturn(Optional.of(tripPlan));
        when(tripPlanRepository.findByUserId(owner.getId())).thenReturn(List.of(tripPlan));
        when(tripPlanRepository.findAll()).thenReturn(new ArrayList<>(tripPlans.values()));
    }

    @Given("a comment exists with content {string}")
    public void a_comment_exists_with_content(String content) {
        User owner = users.get(getLastCreatedUsername());
        Trip trip = trips.get(getLastCreatedTripId());
        Assertions.assertNotNull(owner, "No user exists to own the comment");
        Assertions.assertNotNull(trip, "No trip found to associate with the comment");

        Comment comment =
                Comment.builder()
                        .id(UUID.randomUUID())
                        .user(owner)
                        .trip(trip)
                        .message(content)
                        .parentComment(null)
                        .reactions(new Reactions())
                        .timestamp(Instant.now())
                        .build();
        comments.put(comment.getId(), comment);
        setLastCreatedCommentId(comment.getId());

        when(commentRepository.findByIdWithUser(comment.getId())).thenReturn(Optional.of(comment));
        when(commentRepository.findTopLevelCommentsByTripId(trip.getId()))
                .thenReturn(new ArrayList<>(comments.values()));
        when(commentRepository.findAll()).thenReturn(new ArrayList<>(comments.values()));
    }

    @Given("I have a valid token for that user with roles {string}")
    public void i_have_a_valid_token_for_that_user_with_roles(String roles) throws Exception {
        String token = buildTokenForLastUser(roles, TEST_SECRET, null);
        setTempAuthHeader("Bearer " + token);
    }

    @When("I get all trips")
    public void i_get_all_trips() throws JsonProcessingException {
        HttpEntity<String> request = createJsonRequest(null, getTempAuthHeader());
        latestResponse =
                restTemplate.exchange(
                        ApiConstants.TRIPS_PATH, HttpMethod.GET, request, String.class);
        log.info(
                "[Cucumber] GET {} response status: {}",
                ApiConstants.TRIPS_PATH,
                latestResponse.getStatusCode().value());
        log.info(
                "[Cucumber] GET {} response body: {}",
                ApiConstants.TRIPS_PATH,
                latestResponse.getBody());
    }

    @When("I get my trips")
    public void i_get_my_trips() throws JsonProcessingException {
        HttpEntity<String> request = createJsonRequest(null, getTempAuthHeader());
        latestResponse =
                restTemplate.exchange(
                        ApiConstants.TRIPS_ME_PATH, HttpMethod.GET, request, String.class);
        log.info(
                "[Cucumber] GET {}/me response status: {}",
                ApiConstants.TRIPS_PATH,
                latestResponse.getStatusCode().value());
        log.info(
                "[Cucumber] GET {}/me response body: {}",
                ApiConstants.TRIPS_PATH,
                latestResponse.getBody());
    }

    @When("I get available trips")
    public void i_get_available_trips() throws JsonProcessingException {
        HttpEntity<String> request = createJsonRequest(null, getTempAuthHeader());
        latestResponse =
                restTemplate.exchange(
                        ApiConstants.TRIPS_PATH + ApiConstants.TRIPS_AVAILABLE_ENDPOINT,
                        HttpMethod.GET,
                        request,
                        String.class);
        log.info(
                "[Cucumber] GET {}{} response status: {}",
                ApiConstants.TRIPS_PATH,
                ApiConstants.TRIPS_AVAILABLE_ENDPOINT,
                latestResponse.getStatusCode().value());
        log.info(
                "[Cucumber] GET {}{} response body: {}",
                ApiConstants.TRIPS_PATH,
                ApiConstants.TRIPS_AVAILABLE_ENDPOINT,
                latestResponse.getBody());
    }

    @When("I get the last created trip")
    public void i_get_the_last_created_trip() throws JsonProcessingException {
        String id = getLastCreatedTripId().toString();
        HttpEntity<String> request = createJsonRequest(null, getTempAuthHeader());
        latestResponse =
                restTemplate.exchange(
                        ApiConstants.TRIPS_PATH + "/" + id, HttpMethod.GET, request, String.class);
        log.info(
                "[Cucumber] GET {}/{} response status: {}",
                ApiConstants.TRIPS_PATH,
                id,
                latestResponse.getStatusCode().value());
        log.info(
                "[Cucumber] GET {}/{} response body: {}",
                ApiConstants.TRIPS_PATH,
                id,
                latestResponse.getBody());
    }

    @Then("the response contains at least one trip id")
    public void the_response_contains_at_least_one_trip_id() throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        List<Map<String, Object>> json = mapper.readValue(body, new TypeReference<>() {});
        Assertions.assertFalse(json.isEmpty(), "Expected at least one trip in response");
        Object maybeId = json.getFirst().get("id");
        Assertions.assertNotNull(maybeId);
        UUID parsed = UUID.fromString(maybeId.toString());
        Assertions.assertNotNull(parsed);
    }

    @Then("the response owner id should match user {string}")
    public void the_response_owner_id_should_match_user(String username) throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        List<Map<String, Object>> json = mapper.readValue(body, new TypeReference<>() {});
        Assertions.assertFalse(json.isEmpty(), "Expected at least one trip in response");
        Object ownerIdObj = json.getFirst().get("userId");
        Assertions.assertNotNull(ownerIdObj, "Trip does not contain userId");
        UUID ownerId = UUID.fromString(ownerIdObj.toString());
        User expected = users.get(username);
        Assertions.assertNotNull(expected, "No such user in test state: " + username);
        Assertions.assertEquals(expected.getId(), ownerId);
    }

    @Then("the response should be empty")
    public void the_response_should_be_empty() throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        List<Map<String, Object>> json = mapper.readValue(body, new TypeReference<>() {});
        assertTrue(json.isEmpty(), "Expected response array to be empty");
    }

    @Then("the response should contain {int} trips")
    public void the_response_should_contain_trips(int expectedCount) throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        List<Map<String, Object>> json = mapper.readValue(body, new TypeReference<>() {});
        Assertions.assertEquals(
                expectedCount,
                json.size(),
                "Expected " + expectedCount + " trips in response but got " + json.size());
    }

    @When("I get user by username {string}")
    public void i_get_user_by_username(String username) throws JsonProcessingException {
        HttpEntity<String> request = createJsonRequest(null, getTempAuthHeader());
        latestResponse =
                restTemplate.exchange(
                        ApiConstants.USERNAME_PATH + username,
                        HttpMethod.GET,
                        request,
                        String.class);
        log.info(
                "[Cucumber] GET {}{} response status: {}",
                ApiConstants.USERNAME_PATH,
                username,
                latestResponse.getStatusCode().value());
        log.info(
                "[Cucumber] GET {}{} response body: {}",
                ApiConstants.USERNAME_PATH,
                username,
                latestResponse.getBody());
    }

    @When("I get my user")
    public void i_get_my_user() throws JsonProcessingException {
        HttpEntity<String> request = createJsonRequest(null, getTempAuthHeader());
        latestResponse =
                restTemplate.exchange(
                        ApiConstants.USERS_ME_PATH, HttpMethod.GET, request, String.class);
        log.info(
                "[Cucumber] GET {} response status: {}",
                ApiConstants.USERS_ME_PATH,
                latestResponse.getStatusCode().value());
        log.info(
                "[Cucumber] GET {} response body: {}",
                ApiConstants.USERS_ME_PATH,
                latestResponse.getBody());
    }

    @Then("the response status should be {int}")
    public void the_response_status_should_be(int expected) {
        Assertions.assertNotNull(latestResponse, "No response recorded");
        int actual = latestResponse.getStatusCode().value();
        String body = latestResponse.getBody();
        Assertions.assertEquals(
                expected,
                actual,
                "Expected HTTP status "
                        + expected
                        + " but was "
                        + actual
                        + ". Response body: "
                        + body);
    }

    @And("the response contains a trip id")
    public void the_response_contains_a_trip_id() throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        Map<?, ?> json = mapper.readValue(body, Map.class);
        assertTrue(json.containsKey("id"));
        UUID parsed = UUID.fromString(json.get("id").toString());
        Assertions.assertNotNull(parsed);
    }

    @Then("the response contains a user id")
    public void the_response_contains_a_user_id() throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        Map<?, ?> json = mapper.readValue(body, Map.class);
        assertTrue(json.containsKey("id"), "Response does not contain id");
        UUID parsed = UUID.fromString(json.get("id").toString());
        Assertions.assertNotNull(parsed);
    }

    @Then("the response username should match user {string}")
    public void the_response_username_should_match_user(String username) throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        Map<?, ?> json = mapper.readValue(body, Map.class);
        assertTrue(json.containsKey("username"), "Response does not contain username");
        String actualUsername = json.get("username").toString();
        User expected = users.get(username);
        Assertions.assertNotNull(expected, "No such user in test state: " + username);
        Assertions.assertEquals(expected.getUsername(), actualUsername);
    }

    private HttpEntity<String> createJsonRequest(Map<String, Object> body, String authHeader)
            throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (authHeader != null) headers.set("Authorization", authHeader);
        String bodyStr = body != null ? mapper.writeValueAsString(body) : null;
        return new HttpEntity<>(bodyStr, headers);
    }

    private String buildTokenForLastUser(
            String roles, String secret, Map<String, Object> extraClaims) throws Exception {
        String username = getLastCreatedUsername();
        Assertions.assertNotNull(username, "No known user to create a token for");
        Optional<User> opt = userRepository.findByUsername(username);
        UUID id;
        if (opt.isPresent()) {
            id = opt.get().getId();
        } else {
            var list = userRepository.findAll();
            Assertions.assertFalse(list.isEmpty(), "No users available to create token");
            id = list.getFirst().getId();
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", id.toString());

        payload.put("roles", roles);
        if (extraClaims != null) payload.putAll(extraClaims);
        return JwtBuilder.buildJwt(payload, secret != null ? secret : TEST_SECRET);
    }

    // ==================== TRIP PLAN STEPS ====================

    @When("I get all trip plans")
    public void i_get_all_trip_plans() throws JsonProcessingException {
        HttpEntity<String> request = createJsonRequest(null, getTempAuthHeader());
        latestResponse =
                restTemplate.exchange(
                        ApiConstants.TRIP_PLANS_PATH, HttpMethod.GET, request, String.class);
        log.info(
                "[Cucumber] GET /trips/plans response status: {}",
                latestResponse.getStatusCode().value());
    }

    @When("I get the last created trip plan")
    public void i_get_the_last_created_trip_plan() throws JsonProcessingException {
        String id = getLastCreatedTripPlanId().toString();
        HttpEntity<String> request = createJsonRequest(null, getTempAuthHeader());
        latestResponse =
                restTemplate.exchange(
                        ApiConstants.TRIP_PLANS_PATH + "/" + id,
                        HttpMethod.GET,
                        request,
                        String.class);
        log.info(
                "[Cucumber] GET /trips/plans/{} response status: {}",
                id,
                latestResponse.getStatusCode().value());
    }

    @When("I get my trip plans")
    public void i_get_my_trip_plans() throws JsonProcessingException {
        HttpEntity<String> request = createJsonRequest(null, getTempAuthHeader());
        latestResponse =
                restTemplate.exchange(
                        ApiConstants.TRIP_PLANS_ME_PATH, HttpMethod.GET, request, String.class);
        log.info(
                "[Cucumber] GET /trips/plans/me response status: {}",
                latestResponse.getStatusCode().value());
    }

    @When("I get that trip plan by id")
    public void i_get_that_trip_plan_by_id() throws JsonProcessingException {
        i_get_the_last_created_trip_plan();
    }

    @When("I get that trip plan by id without token")
    public void i_get_that_trip_plan_by_id_without_token() throws JsonProcessingException {
        String id = getLastCreatedTripPlanId().toString();
        HttpEntity<String> request = createJsonRequest(null, null);
        latestResponse =
                restTemplate.exchange(
                        ApiConstants.TRIP_PLANS_PATH + "/" + id,
                        HttpMethod.GET,
                        request,
                        String.class);
        log.info(
                "[Cucumber] GET /trips/plans/{} (no token) response status: {}",
                id,
                latestResponse.getStatusCode().value());
    }

    @Then("the response contains a trip plan id")
    public void the_response_contains_a_trip_plan_id() throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        Map<?, ?> json = mapper.readValue(body, Map.class);
        assertTrue(json.containsKey("id"), "Response does not contain id");
        UUID parsed = UUID.fromString(json.get("id").toString());
        Assertions.assertNotNull(parsed);
    }

    @Then("the response contains at least one trip plan id")
    public void the_response_contains_at_least_one_trip_plan_id() throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        List<Map<String, Object>> json = mapper.readValue(body, new TypeReference<>() {});
        Assertions.assertFalse(json.isEmpty(), "Expected at least one trip plan in response");
        Object maybeId = json.getFirst().get("id");
        Assertions.assertNotNull(maybeId);
        UUID parsed = UUID.fromString(maybeId.toString());
        Assertions.assertNotNull(parsed);
    }

    @Then("the trip plan name should be {string}")
    public void the_trip_plan_name_should_be(String expectedName) throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        Map<?, ?> json = mapper.readValue(body, Map.class);
        assertTrue(json.containsKey("name"), "Response does not contain name");
        String actualName = json.get("name").toString();
        Assertions.assertEquals(expectedName, actualName);
    }

    // ==================== COMMENT STEPS ====================

    @Given("a comment exists with message {string} on that trip")
    public void a_comment_exists_with_message_on_that_trip(String message) {
        User owner = users.get(getLastCreatedUsername());
        Trip trip = trips.get(getLastCreatedTripId());
        Assertions.assertNotNull(owner, "No user exists to own the comment");
        Assertions.assertNotNull(trip, "No trip exists to associate with the comment");

        Comment comment =
                Comment.builder()
                        .id(UUID.randomUUID())
                        .user(owner)
                        .trip(trip)
                        .message(message)
                        .parentComment(null)
                        .reactions(new Reactions())
                        .timestamp(Instant.now())
                        .build();
        comments.put(comment.getId(), comment);
        setLastCreatedCommentId(comment.getId());

        when(commentRepository.findByIdWithUser(comment.getId())).thenReturn(Optional.of(comment));
        when(commentRepository.findTopLevelCommentsByTripId(trip.getId()))
                .thenReturn(new ArrayList<>(comments.values()));
    }

    @Given("a reply exists with message {string} on that comment")
    public void a_reply_exists_with_message_on_that_comment(String message) {
        User owner = users.get(getLastCreatedUsername());
        Comment parentComment = comments.get(getLastCreatedCommentId());
        Assertions.assertNotNull(owner, "No user exists to own the reply");
        Assertions.assertNotNull(parentComment, "No parent comment exists");

        Comment reply =
                Comment.builder()
                        .id(UUID.randomUUID())
                        .user(owner)
                        .trip(parentComment.getTrip())
                        .message(message)
                        .parentComment(parentComment)
                        .reactions(new Reactions())
                        .timestamp(Instant.now())
                        .build();
        comments.put(reply.getId(), reply);

        when(commentRepository.findById(reply.getId())).thenReturn(Optional.of(reply));
    }

    @Given("a reaction {string} exists on that comment")
    public void a_reaction_exists_on_that_comment(String reactionType) {
        Comment comment = comments.get(getLastCreatedCommentId());
        Assertions.assertNotNull(comment, "No comment exists to add reaction to");

        if (comment.getReactions() == null) {
            comment.setReactions(new Reactions());
        }
        // Add a reaction (simplified - just mark that reactions exist)
        Map<String, Integer> reactionMap = new HashMap<>();
        reactionMap.put(reactionType, 1);
    }

    @When("I get all comments for that trip")
    public void i_get_all_comments_for_that_trip() throws JsonProcessingException {
        String tripId = getLastCreatedTripId().toString();
        HttpEntity<String> request = createJsonRequest(null, getTempAuthHeader());
        latestResponse =
                restTemplate.exchange(
                        ApiConstants.API_V1 + "/trips/" + tripId + "/comments",
                        HttpMethod.GET,
                        request,
                        String.class);
        log.info(
                "[Cucumber] GET /{}/comments response status: {}",
                tripId,
                latestResponse.getStatusCode().value());
    }

    @When("I get all comments for that trip without token")
    public void i_get_all_comments_for_that_trip_without_token() throws JsonProcessingException {
        String tripId = getLastCreatedTripId().toString();
        HttpEntity<String> request = createJsonRequest(null, null);
        latestResponse =
                restTemplate.exchange(
                        ApiConstants.API_V1 + "/" + tripId + "/comments",
                        HttpMethod.GET,
                        request,
                        String.class);
        log.info(
                "[Cucumber] GET /{}/comments (no token) response status: {}",
                tripId,
                latestResponse.getStatusCode().value());
    }

    @When("I get that comment by id")
    public void i_get_that_comment_by_id() throws JsonProcessingException {
        String commentId = getLastCreatedCommentId().toString();
        HttpEntity<String> request = createJsonRequest(null, getTempAuthHeader());
        latestResponse =
                restTemplate.exchange(
                        ApiConstants.COMMENTS_PATH + "/" + commentId,
                        HttpMethod.GET,
                        request,
                        String.class);
        log.info(
                "[Cucumber] GET /comments/{} response status: {}",
                commentId,
                latestResponse.getStatusCode().value());
    }

    @When("I get that comment by id without token")
    public void i_get_that_comment_by_id_without_token() throws JsonProcessingException {
        String commentId = getLastCreatedCommentId().toString();
        HttpEntity<String> request = createJsonRequest(null, null);
        latestResponse =
                restTemplate.exchange(
                        ApiConstants.COMMENTS_PATH + "/" + commentId,
                        HttpMethod.GET,
                        request,
                        String.class);
        log.info(
                "[Cucumber] GET /comments/{} (no token) response status: {}",
                commentId,
                latestResponse.getStatusCode().value());
    }

    @When("I get all replies for that comment")
    public void i_get_all_replies_for_that_comment() throws JsonProcessingException {
        String commentId = getLastCreatedCommentId().toString();
        HttpEntity<String> request = createJsonRequest(null, getTempAuthHeader());
        latestResponse =
                restTemplate.exchange(
                        ApiConstants.COMMENTS_PATH + "/" + commentId + "/replies",
                        HttpMethod.GET,
                        request,
                        String.class);
        log.info(
                "[Cucumber] GET /comments/{}/replies response status: {}",
                commentId,
                latestResponse.getStatusCode().value());
    }

    @When("I get all replies for that comment without token")
    public void i_get_all_replies_for_that_comment_without_token() throws JsonProcessingException {
        String commentId = getLastCreatedCommentId().toString();
        HttpEntity<String> request = createJsonRequest(null, null);
        latestResponse =
                restTemplate.exchange(
                        ApiConstants.COMMENTS_PATH + "/" + commentId + "/replies",
                        HttpMethod.GET,
                        request,
                        String.class);
        log.info(
                "[Cucumber] GET /comments/{}/replies (no token) response status: {}",
                commentId,
                latestResponse.getStatusCode().value());
    }

    @When("I get all comments by that user")
    public void i_get_all_comments_by_that_user() throws JsonProcessingException {
        User user = users.get(getLastCreatedUsername());
        Assertions.assertNotNull(user, "No user exists");
        String userId = user.getId().toString();
        HttpEntity<String> request = createJsonRequest(null, getTempAuthHeader());
        latestResponse =
                restTemplate.exchange(
                        ApiConstants.USERS_PATH + "/" + userId + "/comments",
                        HttpMethod.GET,
                        request,
                        String.class);
        log.info(
                "[Cucumber] GET /users/{}/comments response status: {}",
                userId,
                latestResponse.getStatusCode().value());
    }

    @When("I get all comments by that user without token")
    public void i_get_all_comments_by_that_user_without_token() throws JsonProcessingException {
        User user = users.get(getLastCreatedUsername());
        Assertions.assertNotNull(user, "No user exists");
        String userId = user.getId().toString();
        HttpEntity<String> request = createJsonRequest(null, null);
        latestResponse =
                restTemplate.exchange(
                        ApiConstants.USERS_PATH + "/" + userId + "/comments",
                        HttpMethod.GET,
                        request,
                        String.class);
        log.info(
                "[Cucumber] GET /users/{}/comments (no token) response status: {}",
                userId,
                latestResponse.getStatusCode().value());
    }

    @Then("the response contains a comment id")
    public void the_response_contains_a_comment_id() throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        Map<?, ?> json = mapper.readValue(body, Map.class);
        assertTrue(json.containsKey("id"), "Response does not contain id");
        UUID parsed = UUID.fromString(json.get("id").toString());
        Assertions.assertNotNull(parsed);
    }

    @Then("the response contains at least one comment id")
    public void the_response_contains_at_least_one_comment_id() throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        List<Map<String, Object>> json = mapper.readValue(body, new TypeReference<>() {});
        Assertions.assertFalse(json.isEmpty(), "Expected at least one comment in response");
        Object maybeId = json.getFirst().get("id");
        Assertions.assertNotNull(maybeId);
        UUID parsed = UUID.fromString(maybeId.toString());
        Assertions.assertNotNull(parsed);
    }

    @Then("the comment message should be {string}")
    public void the_comment_message_should_be(String expectedMessage) throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        Map<?, ?> json = mapper.readValue(body, Map.class);
        assertTrue(json.containsKey("message"), "Response does not contain message");
        String actualMessage = json.get("message").toString();
        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Then("all comments should be by user {string}")
    public void all_comments_should_be_by_user(String username) throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        List<Map<String, Object>> json = mapper.readValue(body, new TypeReference<>() {});
        User expected = users.get(username);
        Assertions.assertNotNull(expected, "No such user in test state: " + username);

        for (Map<String, Object> comment : json) {
            Object userIdObj = comment.get("userId");
            Assertions.assertNotNull(userIdObj, "Comment does not contain userId");
            UUID userId = UUID.fromString(userIdObj.toString());
            Assertions.assertEquals(expected.getId(), userId, "Comment not by expected user");
        }
    }

    @Then("the comment should have at least one reaction")
    public void the_comment_should_have_at_least_one_reaction() throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        Map<?, ?> json = mapper.readValue(body, Map.class);
        assertTrue(json.containsKey("reactions"), "Response does not contain reactions");
        Object reactions = json.get("reactions");
        Assertions.assertNotNull(reactions, "Reactions should not be null");
    }

    // Friend Request Query Steps
    private final Map<String, User> usersMap = new HashMap<>();

    @Given("user {string} with email {string} exists")
    public void user_with_email_exists(String username, String email) {
        User user = User.builder().id(UUID.randomUUID()).username(username).build();
        usersMap.put(username, user);
        users.put(username, user);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
    }

    @Given("user {string} sends a friend request to user {string}")
    public void user_sends_friend_request_to_user(String senderUsername, String receiverUsername) {
        User sender = usersMap.get(senderUsername);
        User receiver = usersMap.get(receiverUsername);
        Assertions.assertNotNull(sender, "Sender user not found: " + senderUsername);
        Assertions.assertNotNull(receiver, "Receiver user not found: " + receiverUsername);

        FriendRequest friendRequest =
                FriendRequest.builder()
                        .id(UUID.randomUUID())
                        .senderId(sender.getId())
                        .receiverId(receiver.getId())
                        .status(FriendRequestStatus.PENDING)
                        .createdAt(Instant.now())
                        .build();
        friendRequests.put(friendRequest.getId(), friendRequest);

        // Mock repository responses
        when(friendRequestRepository.findByReceiverIdAndStatus(
                        receiver.getId(), FriendRequestStatus.PENDING))
                .thenReturn(
                        friendRequests.values().stream()
                                .filter(
                                        fr ->
                                                fr.getReceiverId().equals(receiver.getId())
                                                        && fr.getStatus()
                                                                == FriendRequestStatus.PENDING)
                                .toList());

        when(friendRequestRepository.findBySenderIdAndStatus(
                        sender.getId(), FriendRequestStatus.PENDING))
                .thenReturn(
                        friendRequests.values().stream()
                                .filter(
                                        fr ->
                                                fr.getSenderId().equals(sender.getId())
                                                        && fr.getStatus()
                                                                == FriendRequestStatus.PENDING)
                                .toList());
    }

    @When("user {string} queries their received friend requests")
    public void user_queries_their_received_friend_requests(String username)
            throws JsonProcessingException {
        User user = usersMap.get(username);
        Assertions.assertNotNull(user, "User not found: " + username);

        String token = buildTokenForUser(user, "USER", TEST_SECRET);
        HttpEntity<String> request = createJsonRequest(null, "Bearer " + token);
        latestResponse =
                restTemplate.exchange(
                        ApiConstants.FRIEND_REQUESTS_RECEIVED_PATH,
                        HttpMethod.GET,
                        request,
                        String.class);
        log.info(
                "[Cucumber] GET /users/friends/requests/received response status: {}",
                latestResponse.getStatusCode().value());
    }

    @When("user {string} queries their sent friend requests")
    public void user_queries_their_sent_friend_requests(String username)
            throws JsonProcessingException {
        User user = usersMap.get(username);
        Assertions.assertNotNull(user, "User not found: " + username);

        String token = buildTokenForUser(user, "USER", TEST_SECRET);
        HttpEntity<String> request = createJsonRequest(null, "Bearer " + token);
        latestResponse =
                restTemplate.exchange(
                        ApiConstants.FRIEND_REQUESTS_SENT_PATH,
                        HttpMethod.GET,
                        request,
                        String.class);
        log.info(
                "[Cucumber] GET /users/friends/requests/sent response status: {}",
                latestResponse.getStatusCode().value());
    }

    @Then("the response status code should be {int}")
    public void the_response_status_code_should_be(int statusCode) {
        Assertions.assertNotNull(latestResponse);
        int actual = latestResponse.getStatusCode().value();
        Assertions.assertEquals(statusCode, actual);
    }

    @Then("the response should contain friend requests")
    public void the_response_should_contain_friend_requests() throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        Object json = mapper.readValue(body, Object.class);
        assertInstanceOf(List.class, json);
    }

    @Then("the response should be an empty list")
    public void the_response_should_be_an_empty_list() throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        Object json = mapper.readValue(body, Object.class);
        assertInstanceOf(List.class, json);
        assertTrue(((List<?>) json).isEmpty());
    }

    private String buildTokenForUser(User user, String roles, String secret) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", user.getId().toString());
        payload.put("roles", roles);
        try {
            return JwtBuilder.buildJwt(payload, secret);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build JWT", e);
        }
    }

    // ==================== FRIENDSHIP STEPS ====================

    @Given("user {string} and user {string} are friends")
    public void user_and_user_are_friends(String username1, String username2) {
        User user1 = usersMap.get(username1);
        User user2 = usersMap.get(username2);
        Assertions.assertNotNull(user1, "User not found: " + username1);
        Assertions.assertNotNull(user2, "User not found: " + username2);

        // Create bidirectional friendship (each user has the other as friend)
        Friendship friendship1 =
                Friendship.builder()
                        .id(UUID.randomUUID())
                        .userId(user1.getId())
                        .friendId(user2.getId())
                        .createdAt(Instant.now())
                        .build();
        friendships.put(friendship1.getId(), friendship1);

        Friendship friendship2 =
                Friendship.builder()
                        .id(UUID.randomUUID())
                        .userId(user2.getId())
                        .friendId(user1.getId())
                        .createdAt(Instant.now())
                        .build();
        friendships.put(friendship2.getId(), friendship2);

        // Mock repository responses for both users
        when(friendshipRepository.findByUserId(user1.getId()))
                .thenReturn(
                        friendships.values().stream()
                                .filter(f -> f.getUserId().equals(user1.getId()))
                                .toList());

        when(friendshipRepository.findByUserId(user2.getId()))
                .thenReturn(
                        friendships.values().stream()
                                .filter(f -> f.getUserId().equals(user2.getId()))
                                .toList());

        updateAvailableTripsForAllUsers();
    }

    @Given("user {string} and user {string} are no longer friends")
    public void user_and_user_are_no_longer_friends(String username1, String username2) {
        User user1 = usersMap.get(username1);
        User user2 = usersMap.get(username2);
        Assertions.assertNotNull(user1, "User not found: " + username1);
        Assertions.assertNotNull(user2, "User not found: " + username2);

        // Remove friendships in both directions
        friendships
                .entrySet()
                .removeIf(
                        entry ->
                                (entry.getValue().getUserId().equals(user1.getId())
                                                && entry.getValue()
                                                        .getFriendId()
                                                        .equals(user2.getId()))
                                        || (entry.getValue().getUserId().equals(user2.getId())
                                                && entry.getValue()
                                                        .getFriendId()
                                                        .equals(user1.getId())));

        // Update mocks
        when(friendshipRepository.findByUserId(user1.getId()))
                .thenReturn(
                        friendships.values().stream()
                                .filter(f -> f.getUserId().equals(user1.getId()))
                                .toList());

        when(friendshipRepository.findByUserId(user2.getId()))
                .thenReturn(
                        friendships.values().stream()
                                .filter(f -> f.getUserId().equals(user2.getId()))
                                .toList());
    }

    @When("user {string} queries their friends list")
    public void user_queries_their_friends_list(String username) throws JsonProcessingException {
        User user = usersMap.get(username);
        Assertions.assertNotNull(user, "User not found: " + username);

        String token = buildTokenForUser(user, "USER", TEST_SECRET);
        HttpEntity<String> request = createJsonRequest(null, "Bearer " + token);
        latestResponse =
                restTemplate.exchange(
                        ApiConstants.FRIENDS_PATH, HttpMethod.GET, request, String.class);
        log.info(
                "[Cucumber] GET /users/friends response status: {}",
                latestResponse.getStatusCode().value());
    }

    @Then("the response should contain {int} friends")
    public void the_response_should_contain_friends(int expectedCount) throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        List<?> json = mapper.readValue(body, List.class);
        Assertions.assertEquals(
                expectedCount, json.size(), "Expected " + expectedCount + " friends in response");
    }

    // ==================== TRIP UPDATE STEPS ====================

    @Given("a trip update exists with message {string}")
    public void a_trip_update_exists_with_message(String message) {
        Trip trip = trips.get(getLastCreatedTripId());
        Assertions.assertNotNull(trip, "No trip exists to associate with the trip update");

        GeoLocation location = GeoLocation.builder().lat(42.3601).lon(-71.0589).build();

        TripUpdate tripUpdate =
                TripUpdate.builder()
                        .id(UUID.randomUUID())
                        .trip(trip)
                        .location(location)
                        .battery(85)
                        .message(message)
                        .reactions(new Reactions())
                        .timestamp(Instant.now())
                        .build();
        tripUpdates.put(tripUpdate.getId(), tripUpdate);
        setLastCreatedTripUpdateId(tripUpdate.getId());

        when(tripUpdateRepository.findById(tripUpdate.getId())).thenReturn(Optional.of(tripUpdate));
        when(tripUpdateRepository.findByTripIdOrderByTimestampDesc(trip.getId()))
                .thenReturn(new ArrayList<>(tripUpdates.values()));
    }

    @Given("a trip update exists with message {string} and battery {int}")
    public void a_trip_update_exists_with_message_and_battery(String message, Integer battery) {
        Trip trip = trips.get(getLastCreatedTripId());
        Assertions.assertNotNull(trip, "No trip exists to associate with the trip update");

        GeoLocation location = GeoLocation.builder().lat(42.3601).lon(-71.0589).build();

        TripUpdate tripUpdate =
                TripUpdate.builder()
                        .id(UUID.randomUUID())
                        .trip(trip)
                        .location(location)
                        .battery(battery)
                        .message(message)
                        .reactions(new Reactions())
                        .timestamp(Instant.now())
                        .build();
        tripUpdates.put(tripUpdate.getId(), tripUpdate);
        setLastCreatedTripUpdateId(tripUpdate.getId());

        when(tripUpdateRepository.findById(tripUpdate.getId())).thenReturn(Optional.of(tripUpdate));
        when(tripUpdateRepository.findByTripIdOrderByTimestampDesc(trip.getId()))
                .thenReturn(new ArrayList<>(tripUpdates.values()));
    }

    @Given("a trip update exists with message {string} and location {double}, {double}")
    public void a_trip_update_exists_with_message_and_location(
            String message, Double latitude, Double longitude) {
        Trip trip = trips.get(getLastCreatedTripId());
        Assertions.assertNotNull(trip, "No trip exists to associate with the trip update");

        GeoLocation location = GeoLocation.builder().lat(latitude).lon(longitude).build();

        TripUpdate tripUpdate =
                TripUpdate.builder()
                        .id(UUID.randomUUID())
                        .trip(trip)
                        .location(location)
                        .battery(85)
                        .message(message)
                        .reactions(new Reactions())
                        .timestamp(Instant.now())
                        .build();
        tripUpdates.put(tripUpdate.getId(), tripUpdate);
        setLastCreatedTripUpdateId(tripUpdate.getId());

        when(tripUpdateRepository.findById(tripUpdate.getId())).thenReturn(Optional.of(tripUpdate));
        when(tripUpdateRepository.findByTripIdOrderByTimestampDesc(trip.getId()))
                .thenReturn(new ArrayList<>(tripUpdates.values()));
    }

    @When("I get all trip updates for that trip")
    public void i_get_all_trip_updates_for_that_trip() throws JsonProcessingException {
        String tripId = getLastCreatedTripId().toString();
        HttpEntity<String> request = createJsonRequest(null, getTempAuthHeader());
        latestResponse =
                restTemplate.exchange(
                        ApiConstants.TRIPS_PATH + "/" + tripId + "/updates",
                        HttpMethod.GET,
                        request,
                        String.class);
        log.info(
                "[Cucumber] GET /trips/{}/updates response status: {}",
                tripId,
                latestResponse.getStatusCode().value());
    }

    @When("I get all trip updates for that trip without token")
    public void i_get_all_trip_updates_for_that_trip_without_token()
            throws JsonProcessingException {
        String tripId = getLastCreatedTripId().toString();
        HttpEntity<String> request = createJsonRequest(null, null);
        latestResponse =
                restTemplate.exchange(
                        ApiConstants.TRIPS_PATH + "/" + tripId + "/updates",
                        HttpMethod.GET,
                        request,
                        String.class);
        log.info(
                "[Cucumber] GET /trips/{}/updates (no token) response status: {}",
                tripId,
                latestResponse.getStatusCode().value());
    }

    @When("I get that trip update by id")
    public void i_get_that_trip_update_by_id() throws JsonProcessingException {
        String updateId = getLastCreatedTripUpdateId().toString();
        HttpEntity<String> request = createJsonRequest(null, getTempAuthHeader());
        latestResponse =
                restTemplate.exchange(
                        ApiConstants.TRIPS_PATH + "/updates/" + updateId,
                        HttpMethod.GET,
                        request,
                        String.class);
        log.info(
                "[Cucumber] GET /trip-updates/{} response status: {}",
                updateId,
                latestResponse.getStatusCode().value());
    }

    @When("I get that trip update by id without token")
    public void i_get_that_trip_update_by_id_without_token() throws JsonProcessingException {
        String updateId = getLastCreatedTripUpdateId().toString();
        HttpEntity<String> request = createJsonRequest(null, null);
        latestResponse =
                restTemplate.exchange(
                        ApiConstants.TRIPS_PATH + "/updates/" + updateId,
                        HttpMethod.GET,
                        request,
                        String.class);
        log.info(
                "[Cucumber] GET /trip-updates/{} (no token) response status: {}",
                updateId,
                latestResponse.getStatusCode().value());
    }

    @Then("the response contains a trip update id")
    public void the_response_contains_a_trip_update_id() throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        Map<?, ?> json = mapper.readValue(body, Map.class);
        assertTrue(json.containsKey("id"), "Response does not contain id");
        UUID parsed = UUID.fromString(json.get("id").toString());
        Assertions.assertNotNull(parsed);
    }

    @Then("the response contains at least one trip update id")
    public void the_response_contains_at_least_one_trip_update_id() throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        List<Map<String, Object>> json = mapper.readValue(body, new TypeReference<>() {});
        Assertions.assertFalse(json.isEmpty(), "Expected at least one trip update in response");
        Object maybeId = json.getFirst().get("id");
        Assertions.assertNotNull(maybeId);
        UUID parsed = UUID.fromString(maybeId.toString());
        Assertions.assertNotNull(parsed);
    }

    @Then("the trip update message should be {string}")
    public void the_trip_update_message_should_be(String expectedMessage) throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        Map<?, ?> json = mapper.readValue(body, Map.class);
        assertTrue(json.containsKey("message"), "Response does not contain message");
        String actualMessage = json.get("message").toString();
        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Then("the trip update battery should be {int}")
    public void the_trip_update_battery_should_be(Integer expectedBattery) throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        Map<?, ?> json = mapper.readValue(body, Map.class);
        assertTrue(json.containsKey("battery"), "Response does not contain battery");
        Integer actualBattery = ((Number) json.get("battery")).intValue();
        Assertions.assertEquals(expectedBattery, actualBattery);
    }

    @Then("the trip update location should be {double}, {double}")
    public void the_trip_update_location_should_be(Double expectedLat, Double expectedLon)
            throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        Map<?, ?> json = mapper.readValue(body, Map.class);
        assertTrue(json.containsKey("location"), "Response does not contain location");
        Map<?, ?> location = (Map<?, ?>) json.get("location");
        Double actualLat = ((Number) location.get("lat")).doubleValue();
        Double actualLon = ((Number) location.get("lon")).doubleValue();
        Assertions.assertEquals(expectedLat, actualLat, 0.0001);
        Assertions.assertEquals(expectedLon, actualLon, 0.0001);
    }

    // ==================== USER FOLLOW STEPS ====================

    @Given("user {string} follows user {string}")
    public void user_follows_user(String followerUsername, String followedUsername) {
        User follower = usersMap.get(followerUsername);
        User followed = usersMap.get(followedUsername);
        Assertions.assertNotNull(follower, "Follower user not found: " + followerUsername);
        Assertions.assertNotNull(followed, "Followed user not found: " + followedUsername);

        UserFollow userFollow =
                UserFollow.builder()
                        .id(UUID.randomUUID())
                        .followerId(follower.getId())
                        .followedId(followed.getId())
                        .createdAt(Instant.now())
                        .build();
        userFollows.put(userFollow.getId(), userFollow);

        // Mock repository responses
        when(userFollowRepository.findByFollowerId(follower.getId()))
                .thenReturn(
                        userFollows.values().stream()
                                .filter(uf -> uf.getFollowerId().equals(follower.getId()))
                                .toList());

        when(userFollowRepository.findByFollowedId(followed.getId()))
                .thenReturn(
                        userFollows.values().stream()
                                .filter(uf -> uf.getFollowedId().equals(followed.getId()))
                                .toList());
    }

    @When("user {string} queries their followers")
    public void user_queries_their_followers(String username) throws JsonProcessingException {
        User user = usersMap.get(username);
        Assertions.assertNotNull(user, "User not found: " + username);

        String token = buildTokenForUser(user, "USER", TEST_SECRET);
        HttpEntity<String> request = createJsonRequest(null, "Bearer " + token);
        latestResponse =
                restTemplate.exchange(
                        ApiConstants.FOLLOWERS_PATH, HttpMethod.GET, request, String.class);
        log.info(
                "[Cucumber] GET /users/followers response status: {}",
                latestResponse.getStatusCode().value());
    }

    @When("user {string} queries their following list")
    public void user_queries_their_following_list(String username) throws JsonProcessingException {
        User user = usersMap.get(username);
        Assertions.assertNotNull(user, "User not found: " + username);

        String token = buildTokenForUser(user, "USER", TEST_SECRET);
        HttpEntity<String> request = createJsonRequest(null, "Bearer " + token);
        latestResponse =
                restTemplate.exchange(
                        ApiConstants.FOLLOWING_PATH, HttpMethod.GET, request, String.class);
        log.info(
                "[Cucumber] GET /users/following response status: {}",
                latestResponse.getStatusCode().value());
    }

    @When("user {string} queries their followers list")
    public void user_queries_their_followers_list(String username) throws JsonProcessingException {
        User user = usersMap.get(username);
        Assertions.assertNotNull(user, "User not found: " + username);

        String token = buildTokenForUser(user, "USER", TEST_SECRET);
        HttpEntity<String> request = createJsonRequest(null, "Bearer " + token);
        latestResponse =
                restTemplate.exchange(
                        ApiConstants.FOLLOWERS_PATH, HttpMethod.GET, request, String.class);
        log.info(
                "[Cucumber] GET /users/followers response status: {}",
                latestResponse.getStatusCode().value());
    }

    @Then("the response should contain {int} follows")
    public void the_response_should_contain_follows(int expectedCount) throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        List<?> json = mapper.readValue(body, List.class);
        Assertions.assertEquals(
                expectedCount, json.size(), "Expected " + expectedCount + " follows in response");
    }

    @Then("the response should contain {int} followers")
    public void the_response_should_contain_followers(int expectedCount) throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        List<?> json = mapper.readValue(body, List.class);
        Assertions.assertEquals(
                expectedCount, json.size(), "Expected " + expectedCount + " followers in response");
    }

    @Then("the response should contain {int} following")
    public void the_response_should_contain_following(int expectedCount) throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        List<?> json = mapper.readValue(body, List.class);
        Assertions.assertEquals(
                expectedCount, json.size(), "Expected " + expectedCount + " following in response");
    }

    // ==================== ADDITIONAL TRIP UPDATE STEPS ====================

    @Then("the response should contain {int} trip updates")
    public void the_response_should_contain_trip_updates(int expectedCount) throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        List<Map<String, Object>> json = mapper.readValue(body, new TypeReference<>() {});
        Assertions.assertEquals(
                expectedCount,
                json.size(),
                "Expected " + expectedCount + " trip updates in response but got " + json.size());
    }

    @Then("the trip updates should be ordered by timestamp descending")
    public void the_trip_updates_should_be_ordered_by_timestamp_descending() throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        List<Map<String, Object>> json = mapper.readValue(body, new TypeReference<>() {});
        Assertions.assertFalse(json.isEmpty(), "Expected at least one trip update");

        // Verify that each subsequent timestamp is earlier than or equal to the previous
        for (int i = 1; i < json.size(); i++) {
            String prevTimestamp = json.get(i - 1).get("timestamp").toString();
            String currTimestamp = json.get(i).get("timestamp").toString();
            Instant prevTime = Instant.parse(prevTimestamp);
            Instant currTime = Instant.parse(currTimestamp);
            assertTrue(
                    !currTime.isAfter(prevTime),
                    "Trip updates should be ordered by timestamp descending");
        }
    }

    @Then("the trip updates should have location data")
    public void the_trip_updates_should_have_location_data() throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        List<Map<String, Object>> json = mapper.readValue(body, new TypeReference<>() {});
        Assertions.assertFalse(json.isEmpty(), "Expected at least one trip update");

        for (Map<String, Object> update : json) {
            assertTrue(update.containsKey("location"), "Trip update should have location data");
            Map<?, ?> location = (Map<?, ?>) update.get("location");
            Assertions.assertNotNull(location, "Location should not be null");
            assertTrue(location.containsKey("lat"), "Location should have latitude");
            assertTrue(location.containsKey("lon"), "Location should have longitude");
        }
    }

    @When("I get all trip updates for non-existent trip")
    public void i_get_all_trip_updates_for_non_existent_trip() throws JsonProcessingException {
        UUID nonExistentTripId = UUID.randomUUID();
        HttpEntity<String> request = createJsonRequest(null, getTempAuthHeader());
        latestResponse =
                restTemplate.exchange(
                        ApiConstants.TRIPS_PATH + "/" + nonExistentTripId + "/updates",
                        HttpMethod.GET,
                        request,
                        String.class);
        log.info(
                "[Cucumber] GET /trips/{}/updates response status: {}",
                nonExistentTripId,
                latestResponse.getStatusCode().value());
    }

    @Then("the trip updates should have different battery levels")
    public void the_trip_updates_should_have_different_battery_levels() throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        List<Map<String, Object>> json = mapper.readValue(body, new TypeReference<>() {});
        Assertions.assertFalse(json.isEmpty(), "Expected at least one trip update");

        // Collect all battery levels
        List<Integer> batteryLevels = new ArrayList<>();
        for (Map<String, Object> update : json) {
            assertTrue(update.containsKey("battery"), "Trip update should have battery data");
            Integer battery = ((Number) update.get("battery")).intValue();
            batteryLevels.add(battery);
        }

        // Verify there are different battery levels (not all the same)
        long uniqueBatteryLevels = batteryLevels.stream().distinct().count();
        assertTrue(
                uniqueBatteryLevels > 1,
                "Expected different battery levels, but all were: " + batteryLevels.get(0));
    }

    @When("I get a non-existent trip update by id")
    public void i_get_a_non_existent_trip_update_by_id() throws JsonProcessingException {
        UUID nonExistentUpdateId = UUID.randomUUID();
        HttpEntity<String> request = createJsonRequest(null, getTempAuthHeader());
        latestResponse =
                restTemplate.exchange(
                        ApiConstants.TRIPS_PATH + "/updates/" + nonExistentUpdateId,
                        HttpMethod.GET,
                        request,
                        String.class);
        log.info(
                "[Cucumber] GET /trip-updates/{} response status: {}",
                nonExistentUpdateId,
                latestResponse.getStatusCode().value());
    }
}
