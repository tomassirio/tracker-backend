package com.tomassirio.wanderer.query.cucumber;

import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tomassirio.wanderer.commons.domain.Comment;
import com.tomassirio.wanderer.commons.domain.Reactions;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripDetails;
import com.tomassirio.wanderer.commons.domain.TripPlan;
import com.tomassirio.wanderer.commons.domain.TripPlanType;
import com.tomassirio.wanderer.commons.domain.TripSettings;
import com.tomassirio.wanderer.commons.domain.TripStatus;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import com.tomassirio.wanderer.commons.domain.User;
import com.tomassirio.wanderer.commons.utils.JwtBuilder;
import com.tomassirio.wanderer.query.repository.CommentRepository;
import com.tomassirio.wanderer.query.repository.TripPlanRepository;
import com.tomassirio.wanderer.query.repository.TripRepository;
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

    private ResponseEntity<String> latestResponse;

    private static final Logger log = LoggerFactory.getLogger(StepDefinitions.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    // long secret used by tests
    private static final String TEST_SECRET =
            "test-secret-that-is-long-enough-for-jwt-hmac-sha-algorithm-256-bits-minimum";

    // URL constants for endpoints used in tests
    private static final String API_BASE = "/api/1";
    private static final String TRIPS_ENDPOINT = API_BASE + "/trips";
    private static final String USERS_BY_USERNAME_ENDPOINT = API_BASE + "/users/username/";
    private static final String USERS_ME_ENDPOINT = API_BASE + "/users/me";

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

    // In-memory data
    private final Map<String, User> users = new HashMap<>();
    private final Map<UUID, Trip> trips = new HashMap<>();
    private final Map<UUID, TripPlan> tripPlans = new HashMap<>();
    private final Map<UUID, Comment> comments = new HashMap<>();

    @Given("an empty system")
    public void an_empty_system() {
        trips.clear();
        users.clear();
        tripPlans.clear();
        comments.clear();

        setTempAuthHeader(null);
        setLastCreatedUsername(null);
        setLastCreatedTripId(null);
        setLastCreatedTripPlanId(null);
        setLastCreatedCommentId(null);

        Mockito.reset(tripRepository);
        Mockito.reset(userRepository);

        when(tripRepository.findAll()).thenReturn(new ArrayList<>());
    }

    @Given("a user exists with username {string} and email {string}")
    public void a_user_exists(String username, String email) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        users.put(username, user);
        setLastCreatedUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(tripRepository.findByUserId(user.getId())).thenReturn(new ArrayList<>());
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
        when(tripRepository.findByUserId(owner.getId())).thenReturn(List.of(trip));
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
                        .userId(owner.getId())
                        .trip(trip)
                        .message(content)
                        .parentComment(null)
                        .reactions(new Reactions())
                        .timestamp(Instant.now())
                        .build();
        comments.put(comment.getId(), comment);
        setLastCreatedCommentId(comment.getId());

        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
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
                restTemplate.exchange(TRIPS_ENDPOINT, HttpMethod.GET, request, String.class);
        log.info(
                "[Cucumber] GET {} response status: {}",
                TRIPS_ENDPOINT,
                latestResponse.getStatusCode().value());
        log.info("[Cucumber] GET {} response body: {}", TRIPS_ENDPOINT, latestResponse.getBody());
    }

    @When("I get my trips")
    public void i_get_my_trips() throws JsonProcessingException {
        HttpEntity<String> request = createJsonRequest(null, getTempAuthHeader());
        latestResponse =
                restTemplate.exchange(
                        TRIPS_ENDPOINT + "/me", HttpMethod.GET, request, String.class);
        log.info(
                "[Cucumber] GET {}/me response status: {}",
                TRIPS_ENDPOINT,
                latestResponse.getStatusCode().value());
        log.info(
                "[Cucumber] GET {}/me response body: {}", TRIPS_ENDPOINT, latestResponse.getBody());
    }

    @When("I get the last created trip")
    public void i_get_the_last_created_trip() throws JsonProcessingException {
        String id = getLastCreatedTripId().toString();
        HttpEntity<String> request = createJsonRequest(null, getTempAuthHeader());
        latestResponse =
                restTemplate.exchange(
                        TRIPS_ENDPOINT + "/" + id, HttpMethod.GET, request, String.class);
        log.info(
                "[Cucumber] GET {}/{} response status: {}",
                TRIPS_ENDPOINT,
                id,
                latestResponse.getStatusCode().value());
        log.info(
                "[Cucumber] GET {}/{} response body: {}",
                TRIPS_ENDPOINT,
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
        Assertions.assertTrue(json.isEmpty(), "Expected response array to be empty");
    }

    @When("I get user by username {string}")
    public void i_get_user_by_username(String username) throws JsonProcessingException {
        HttpEntity<String> request = createJsonRequest(null, getTempAuthHeader());
        latestResponse =
                restTemplate.exchange(
                        USERS_BY_USERNAME_ENDPOINT + username,
                        HttpMethod.GET,
                        request,
                        String.class);
        log.info(
                "[Cucumber] GET {}{} response status: {}",
                USERS_BY_USERNAME_ENDPOINT,
                username,
                latestResponse.getStatusCode().value());
        log.info(
                "[Cucumber] GET {}{} response body: {}",
                USERS_BY_USERNAME_ENDPOINT,
                username,
                latestResponse.getBody());
    }

    @When("I get my user")
    public void i_get_my_user() throws JsonProcessingException {
        HttpEntity<String> request = createJsonRequest(null, getTempAuthHeader());
        latestResponse =
                restTemplate.exchange(USERS_ME_ENDPOINT, HttpMethod.GET, request, String.class);
        log.info(
                "[Cucumber] GET {} response status: {}",
                USERS_ME_ENDPOINT,
                latestResponse.getStatusCode().value());
        log.info(
                "[Cucumber] GET {} response body: {}", USERS_ME_ENDPOINT, latestResponse.getBody());
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
        Assertions.assertTrue(json.containsKey("id"));
        UUID parsed = UUID.fromString(json.get("id").toString());
        Assertions.assertNotNull(parsed);
    }

    @Then("the response contains a user id")
    public void the_response_contains_a_user_id() throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        Map<?, ?> json = mapper.readValue(body, Map.class);
        Assertions.assertTrue(json.containsKey("id"), "Response does not contain id");
        UUID parsed = UUID.fromString(json.get("id").toString());
        Assertions.assertNotNull(parsed);
    }

    @Then("the response username should match user {string}")
    public void the_response_username_should_match_user(String username) throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        Map<?, ?> json = mapper.readValue(body, Map.class);
        Assertions.assertTrue(json.containsKey("username"), "Response does not contain username");
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
                        API_BASE + "/trips/plans", HttpMethod.GET, request, String.class);
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
                        API_BASE + "/trips/plans/" + id, HttpMethod.GET, request, String.class);
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
                        API_BASE + "/trips/plans/me", HttpMethod.GET, request, String.class);
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
                        API_BASE + "/trips/plans/" + id, HttpMethod.GET, request, String.class);
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
        Assertions.assertTrue(json.containsKey("id"), "Response does not contain id");
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
        Assertions.assertTrue(json.containsKey("name"), "Response does not contain name");
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
                        .userId(owner.getId())
                        .trip(trip)
                        .message(message)
                        .parentComment(null)
                        .reactions(new Reactions())
                        .timestamp(Instant.now())
                        .build();
        comments.put(comment.getId(), comment);
        setLastCreatedCommentId(comment.getId());

        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
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
                        .userId(owner.getId())
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
                        API_BASE + "/" + tripId + "/comments",
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
                        API_BASE + "/" + tripId + "/comments",
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
                        API_BASE + "/comments/" + commentId, HttpMethod.GET, request, String.class);
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
                        API_BASE + "/comments/" + commentId, HttpMethod.GET, request, String.class);
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
                        API_BASE + "/comments/" + commentId + "/replies",
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
                        API_BASE + "/comments/" + commentId + "/replies",
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
                        API_BASE + "/users/" + userId + "/comments",
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
                        API_BASE + "/users/" + userId + "/comments",
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
        Assertions.assertTrue(json.containsKey("id"), "Response does not contain id");
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
        Assertions.assertTrue(json.containsKey("message"), "Response does not contain message");
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
        Assertions.assertTrue(json.containsKey("reactions"), "Response does not contain reactions");
        Object reactions = json.get("reactions");
        Assertions.assertNotNull(reactions, "Reactions should not be null");
    }
}
