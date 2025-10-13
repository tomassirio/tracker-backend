package com.tomassirio.wanderer.query.cucumber;

import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tomassirio.wanderer.commons.domain.Location;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import com.tomassirio.wanderer.commons.domain.User;
import com.tomassirio.wanderer.commons.utils.JwtBuilder;
import com.tomassirio.wanderer.query.repository.TripRepository;
import com.tomassirio.wanderer.query.repository.UserRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
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

    static {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // per-scenario state
    @Getter @Setter private String tempAuthHeader;
    @Getter @Setter private String lastCreatedUsername;
    @Getter @Setter private UUID lastCreatedTripId;

    // In-memory data
    private Map<String, User> users = new HashMap<>();
    private Map<UUID, Trip> trips = new HashMap<>();

    @Given("an empty system")
    public void an_empty_system() {
        trips.clear();
        users.clear();

        setTempAuthHeader(null);
        setLastCreatedUsername(null);
        setLastCreatedTripId(null);

        Mockito.reset(tripRepository);
        Mockito.reset(userRepository);

        when(tripRepository.findAll()).thenReturn(new ArrayList<>());
    }

    @Given("a user exists with username {string} and email {string}")
    public void a_user_exists(String username, String email) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setEmail(email);
        users.put(username, user);
        setLastCreatedUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    }

    @Given("a trip exists with name {string}")
    public void a_trip_exists_with_name(String name) {
        User owner = users.get(getLastCreatedUsername());
        Assertions.assertNotNull(owner, "No user exists to own the trip");
        Location loc = Location.builder().latitude(10.0).longitude(20.0).altitude(0.0).build();
        Trip trip =
                Trip.builder()
                        .id(UUID.randomUUID())
                        .name(name)
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(5))
                        .totalDistance(100.0)
                        .startingLocation(loc)
                        .endingLocation(loc)
                        .visibility(TripVisibility.PUBLIC)
                        .owner(owner)
                        .build();
        trips.put(trip.getId(), trip);
        setLastCreatedTripId(trip.getId());

        when(tripRepository.findById(trip.getId())).thenReturn(Optional.of(trip));
        when(tripRepository.findAll()).thenReturn(new ArrayList<>(trips.values()));
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
}
