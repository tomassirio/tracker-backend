package com.tomassirio.wanderer.command.cucumber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tomassirio.wanderer.command.repository.CommentRepository;
import com.tomassirio.wanderer.command.repository.TripPlanRepository;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.repository.UserRepository;
import com.tomassirio.wanderer.commons.domain.User;
import com.tomassirio.wanderer.commons.utils.JwtBuilder;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class StepDefinitions {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private UserRepository userRepository;
    @Autowired private TripRepository tripRepository;
    @Autowired private TripPlanRepository tripPlanRepository;
    @Autowired private CommentRepository commentRepository;

    private ResponseEntity<String> latestResponse;

    private static final Logger log = LoggerFactory.getLogger(StepDefinitions.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String TEST_SECRET =
            "test-secret-that-is-long-enough-for-jwt-hmac-sha-algorithm-256-bits-minimum";

    // URL constants for endpoints used in tests
    private static final String API_BASE = "/api/1";
    private static final String USERS_ENDPOINT = API_BASE + "/users";
    private static final String TRIPS_ENDPOINT = API_BASE + "/trips";
    private static final String TRIP_PLANS_ENDPOINT = TRIPS_ENDPOINT + "/plans";

    @Getter @Setter private UUID lastCreatedTripId;
    @Getter @Setter private UUID lastCreatedTripPlanId;
    @Getter @Setter private UUID lastCreatedCommentId;

    static {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // per-scenario state
    @Getter @Setter private String tempAuthHeader;
    @Getter @Setter private String lastCreatedUsername;

    @Given("an empty system")
    public void an_empty_system() {
        // clear repositories to ensure clean state
        commentRepository.deleteAll();
        tripPlanRepository.deleteAll();
        tripRepository.deleteAll();
        userRepository.deleteAll();
        setTempAuthHeader(null);
        setLastCreatedUsername(null);
        setLastCreatedTripId(null);
        setLastCreatedTripPlanId(null);
        setLastCreatedCommentId(null);
    }

    @When("I create a user with username {string} and email {string}")
    public void i_create_a_user_with_username_and_email(String username, String email)
            throws Exception {
        setLastCreatedUsername(username);
        Map<String, Object> body = Map.of("username", username, "email", email);
        HttpEntity<String> request = createJsonRequest(body);
        latestResponse = restTemplate.postForEntity(USERS_ENDPOINT, request, String.class);
        log.info(
                "[Cucumber] POST {} response status: {}",
                USERS_ENDPOINT,
                latestResponse.getStatusCode().value());
        log.info("[Cucumber] POST {} response body: {}", USERS_ENDPOINT, latestResponse.getBody());
    }

    @When("I create a trip with name {string} using that token")
    public void i_create_a_trip_with_name_using_that_token(String name) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("startDate", LocalDate.now());
        body.put("endDate", LocalDate.now().plusDays(5));
        body.put("totalDistance", 100.0);
        body.put("visibility", "PUBLIC");
        Map<String, Object> loc = Map.of("latitude", 10.0, "longitude", 20.0, "altitude", 0.0);
        body.put("startingLocation", loc);
        body.put("endingLocation", loc);

        HttpEntity<String> request = createJsonRequest(body, getTempAuthHeader());
        log.info("[Cucumber] POST {} request: {}", TRIPS_ENDPOINT, mapper.writeValueAsString(body));
        latestResponse = restTemplate.postForEntity(TRIPS_ENDPOINT, request, String.class);

        log.info(
                "[Cucumber] POST {} response status: {}",
                TRIPS_ENDPOINT,
                latestResponse.getStatusCode().value());
        log.info("[Cucumber] POST {} response body: {}", TRIPS_ENDPOINT, latestResponse.getBody());

        // Store trip ID if creation was successful
        if (latestResponse.getStatusCode().is2xxSuccessful()) {
            String responseBody = latestResponse.getBody();
            Map<?, ?> json = mapper.readValue(responseBody, Map.class);
            if (json.containsKey("id")) {
                lastCreatedTripId = UUID.fromString(json.get("id").toString());
                log.info("[Cucumber] Stored trip ID: {}", lastCreatedTripId);
            }
        }
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

    @And("the response contains a user id")
    public void the_response_contains_a_user_id() throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        Map<?, ?> json = mapper.readValue(body, Map.class);
        Assertions.assertTrue(json.containsKey("id"));
        UUID parsed = UUID.fromString(json.get("id").toString());
        Assertions.assertNotNull(parsed);
    }

    @Given("a user exists with username {string} and email {string}")
    public void a_user_exists(String username, String email) throws Exception {
        i_create_a_user_with_username_and_email(username, email);
        Assertions.assertEquals(201, latestResponse.getStatusCode().value());
    }

    @Given("I have a valid token for that user with roles {string}")
    public void i_have_a_valid_token_for_that_user_with_roles(String roles) throws Exception {
        String token = buildTokenForLastUser(roles, TEST_SECRET, null);
        setTempAuthHeader("Bearer " + token);
    }

    @Given("I have an invalidly signed token for that user with roles {string}")
    public void i_have_an_invalidly_signed_token_for_that_user_with_roles(String roles)
            throws Exception {
        String token = buildTokenForLastUser(roles, "bad-secret", null);
        setTempAuthHeader("Bearer " + token);
    }

    @Given("I have a token for that user that has an expired exp claim and roles {string}")
    public void i_have_a_token_for_that_user_that_has_an_expired_exp_claim_and_roles(String roles)
            throws Exception {
        long past = LocalDate.now().minusDays(10).atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        Map<String, Object> extra = Map.of("exp", past);
        String token = buildTokenForLastUser(roles, TEST_SECRET, extra);
        setTempAuthHeader("Bearer " + token);
    }

    @When("I create a trip with name {string} without token")
    public void i_create_a_trip_without_token(String name) throws Exception {
        setTempAuthHeader(null);
        i_create_a_trip_with_name_using_that_token(name);
    }

    @When("I update that trip with name {string} using that token")
    public void i_update_that_trip_with_name_using_that_token(String name) throws Exception {
        Assertions.assertNotNull(lastCreatedTripId, "No trip ID stored to update");

        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("visibility", "PUBLIC");

        HttpEntity<String> request = createJsonRequest(body, getTempAuthHeader());
        String url = TRIPS_ENDPOINT + "/" + lastCreatedTripId;
        log.info("[Cucumber] PUT {} request: {}", url, mapper.writeValueAsString(body));

        latestResponse =
                restTemplate.exchange(
                        url, org.springframework.http.HttpMethod.PUT, request, String.class);

        log.info(
                "[Cucumber] PUT {} response status: {}",
                url,
                latestResponse.getStatusCode().value());
        log.info("[Cucumber] PUT {} response body: {}", url, latestResponse.getBody());
    }

    @When("I update that trip with name {string} without token")
    public void i_update_that_trip_with_name_without_token(String name) throws Exception {
        setTempAuthHeader(null);
        i_update_that_trip_with_name_using_that_token(name);
    }

    @When("I delete that trip using that token")
    public void i_delete_that_trip_using_that_token() {
        Assertions.assertNotNull(lastCreatedTripId, "No trip ID stored to delete");

        HttpHeaders headers = new HttpHeaders();
        if (getTempAuthHeader() != null) {
            headers.set("Authorization", getTempAuthHeader());
        }
        HttpEntity<String> request = new HttpEntity<>(headers);

        String url = TRIPS_ENDPOINT + "/" + lastCreatedTripId;
        log.info("[Cucumber] DELETE {}", url);

        latestResponse =
                restTemplate.exchange(
                        url, org.springframework.http.HttpMethod.DELETE, request, String.class);

        log.info(
                "[Cucumber] DELETE {} response status: {}",
                url,
                latestResponse.getStatusCode().value());
    }

    @When("I delete that trip without token")
    public void i_delete_that_trip_without_token() {
        setTempAuthHeader(null);
        i_delete_that_trip_using_that_token();
    }

    @When("I change that trip visibility to {string} using that token")
    public void i_change_that_trip_visibility_using_that_token(String visibility) throws Exception {
        Assertions.assertNotNull(lastCreatedTripId, "No trip ID stored to change visibility");

        Map<String, Object> body = Map.of("visibility", visibility);

        HttpEntity<String> request = createJsonRequest(body, getTempAuthHeader());
        String url = TRIPS_ENDPOINT + "/" + lastCreatedTripId + "/visibility";
        log.info("[Cucumber] PATCH {} request: {}", url, mapper.writeValueAsString(body));

        latestResponse =
                restTemplate.exchange(
                        url, org.springframework.http.HttpMethod.PATCH, request, String.class);

        log.info(
                "[Cucumber] PATCH {} response status: {}",
                url,
                latestResponse.getStatusCode().value());
        log.info("[Cucumber] PATCH {} response body: {}", url, latestResponse.getBody());
    }

    @When("I change that trip status to {string} using that token")
    public void i_change_that_trip_status_using_that_token(String status) throws Exception {
        Assertions.assertNotNull(lastCreatedTripId, "No trip ID stored to change status");

        Map<String, Object> body = Map.of("status", status);

        HttpEntity<String> request = createJsonRequest(body, getTempAuthHeader());
        String url = TRIPS_ENDPOINT + "/" + lastCreatedTripId + "/status";
        log.info("[Cucumber] PATCH {} request: {}", url, mapper.writeValueAsString(body));

        latestResponse =
                restTemplate.exchange(
                        url, org.springframework.http.HttpMethod.PATCH, request, String.class);

        log.info(
                "[Cucumber] PATCH {} response status: {}",
                url,
                latestResponse.getStatusCode().value());
        log.info("[Cucumber] PATCH {} response body: {}", url, latestResponse.getBody());
    }

    @When("I add a trip update with message {string} using that token")
    public void i_add_a_trip_update_with_message_using_that_token(String message) throws Exception {
        Assertions.assertNotNull(lastCreatedTripId, "No trip ID stored to add update");

        Map<String, Object> body = new HashMap<>();
        Map<String, Object> location = Map.of("latitude", 10.0, "longitude", 20.0, "altitude", 0.0);
        body.put("location", location);
        body.put("message", message);
        body.put("battery", 75);

        HttpEntity<String> request = createJsonRequest(body, getTempAuthHeader());
        String url = TRIPS_ENDPOINT + "/" + lastCreatedTripId + "/updates";
        log.info("[Cucumber] POST {} request: {}", url, mapper.writeValueAsString(body));

        latestResponse = restTemplate.postForEntity(url, request, String.class);

        log.info(
                "[Cucumber] POST {} response status: {}",
                url,
                latestResponse.getStatusCode().value());
        log.info("[Cucumber] POST {} response body: {}", url, latestResponse.getBody());
    }

    @Then("the trip name should be {string}")
    public void the_trip_name_should_be(String expectedName) throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        Map<?, ?> json = mapper.readValue(body, Map.class);
        Assertions.assertTrue(json.containsKey("name"), "Response does not contain 'name' field");
        String actualName = json.get("name").toString();
        Assertions.assertEquals(expectedName, actualName, "Trip name does not match");
    }

    // ========== Trip Plan Step Definitions ==========

    @When("I create a trip plan with name {string} using that token")
    public void i_create_a_trip_plan_with_name_using_that_token(String name) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("startDate", LocalDate.now());
        body.put("endDate", LocalDate.now().plusDays(5));
        body.put("planType", "SIMPLE");
        Map<String, Object> loc = Map.of("latitude", 10.0, "longitude", 20.0, "altitude", 0.0);
        body.put("startLocation", loc);
        body.put("endLocation", loc);

        HttpEntity<String> request = createJsonRequest(body, getTempAuthHeader());
        log.info(
                "[Cucumber] POST {} request: {}",
                TRIP_PLANS_ENDPOINT,
                mapper.writeValueAsString(body));
        latestResponse = restTemplate.postForEntity(TRIP_PLANS_ENDPOINT, request, String.class);

        log.info(
                "[Cucumber] POST {} response status: {}",
                TRIP_PLANS_ENDPOINT,
                latestResponse.getStatusCode().value());
        log.info(
                "[Cucumber] POST {} response body: {}",
                TRIP_PLANS_ENDPOINT,
                latestResponse.getBody());

        // Store trip plan ID if creation was successful
        if (latestResponse.getStatusCode().is2xxSuccessful()) {
            String responseBody = latestResponse.getBody();
            Map<?, ?> json = mapper.readValue(responseBody, Map.class);
            if (json.containsKey("id")) {
                lastCreatedTripPlanId = UUID.fromString(json.get("id").toString());
                log.info("[Cucumber] Stored trip plan ID: {}", lastCreatedTripPlanId);
            }
        }
    }

    @When("I create a trip plan with name {string} without token")
    public void i_create_a_trip_plan_without_token(String name) throws Exception {
        setTempAuthHeader(null);
        i_create_a_trip_plan_with_name_using_that_token(name);
    }

    @When("I update that trip plan with name {string} using that token")
    public void i_update_that_trip_plan_with_name_using_that_token(String name) throws Exception {
        Assertions.assertNotNull(lastCreatedTripPlanId, "No trip plan ID stored to update");

        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("startDate", LocalDate.now());
        body.put("endDate", LocalDate.now().plusDays(5));
        body.put("planType", "SIMPLE");
        Map<String, Object> loc = Map.of("latitude", 10.0, "longitude", 20.0, "altitude", 0.0);
        body.put("startLocation", loc);
        body.put("endLocation", loc);

        HttpEntity<String> request = createJsonRequest(body, getTempAuthHeader());
        String url = TRIP_PLANS_ENDPOINT + "/" + lastCreatedTripPlanId;
        log.info("[Cucumber] PUT {} request: {}", url, mapper.writeValueAsString(body));

        latestResponse =
                restTemplate.exchange(
                        url, org.springframework.http.HttpMethod.PUT, request, String.class);

        log.info(
                "[Cucumber] PUT {} response status: {}",
                url,
                latestResponse.getStatusCode().value());
        log.info("[Cucumber] PUT {} response body: {}", url, latestResponse.getBody());
    }

    @When("I update that trip plan with name {string} without token")
    public void i_update_that_trip_plan_with_name_without_token(String name) throws Exception {
        setTempAuthHeader(null);
        i_update_that_trip_plan_with_name_using_that_token(name);
    }

    @When("I delete that trip plan using that token")
    public void i_delete_that_trip_plan_using_that_token() {
        Assertions.assertNotNull(lastCreatedTripPlanId, "No trip plan ID stored to delete");

        HttpHeaders headers = new HttpHeaders();
        if (getTempAuthHeader() != null) {
            headers.set("Authorization", getTempAuthHeader());
        }
        HttpEntity<String> request = new HttpEntity<>(headers);

        String url = TRIP_PLANS_ENDPOINT + "/" + lastCreatedTripPlanId;
        log.info("[Cucumber] DELETE {}", url);

        latestResponse =
                restTemplate.exchange(
                        url, org.springframework.http.HttpMethod.DELETE, request, String.class);

        log.info(
                "[Cucumber] DELETE {} response status: {}",
                url,
                latestResponse.getStatusCode().value());
    }

    @When("I delete that trip plan without token")
    public void i_delete_that_trip_plan_without_token() {
        setTempAuthHeader(null);
        i_delete_that_trip_plan_using_that_token();
    }

    @Then("the trip plan name should be {string}")
    public void the_trip_plan_name_should_be(String expectedName) throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        Map<?, ?> json = mapper.readValue(body, Map.class);
        Assertions.assertTrue(json.containsKey("name"), "Response does not contain 'name' field");
        String actualName = json.get("name").toString();
        Assertions.assertEquals(expectedName, actualName, "Trip plan name does not match");
    }

    // ========== Comment Step Definitions ==========

    @When("I create a comment with message {string} on that trip using that token")
    public void i_create_a_comment_with_message_on_that_trip_using_that_token(String message)
            throws Exception {
        Assertions.assertNotNull(lastCreatedTripId, "No trip ID stored to add comment");

        Map<String, Object> body = new HashMap<>();
        body.put("message", message);

        HttpEntity<String> request = createJsonRequest(body, getTempAuthHeader());
        String url = TRIPS_ENDPOINT + "/" + lastCreatedTripId + "/comments";
        log.info("[Cucumber] POST {} request: {}", url, mapper.writeValueAsString(body));

        latestResponse = restTemplate.postForEntity(url, request, String.class);

        log.info(
                "[Cucumber] POST {} response status: {}",
                url,
                latestResponse.getStatusCode().value());
        log.info("[Cucumber] POST {} response body: {}", url, latestResponse.getBody());

        // Store comment ID if creation was successful
        if (latestResponse.getStatusCode().is2xxSuccessful()) {
            String responseBody = latestResponse.getBody();
            Map<?, ?> json = mapper.readValue(responseBody, Map.class);
            if (json.containsKey("id")) {
                lastCreatedCommentId = UUID.fromString(json.get("id").toString());
                log.info("[Cucumber] Stored comment ID: {}", lastCreatedCommentId);
            }
        }
    }

    @When("I create a comment with message {string} on that trip without token")
    public void i_create_a_comment_with_message_on_that_trip_without_token(String message)
            throws Exception {
        setTempAuthHeader(null);
        i_create_a_comment_with_message_on_that_trip_using_that_token(message);
    }

    @When("I create a reply with message {string} on that comment using that token")
    public void i_create_a_reply_with_message_on_that_comment_using_that_token(String message)
            throws Exception {
        Assertions.assertNotNull(lastCreatedTripId, "No trip ID stored to add reply");
        Assertions.assertNotNull(lastCreatedCommentId, "No comment ID stored to reply to");

        Map<String, Object> body = new HashMap<>();
        body.put("message", message);
        body.put("parentCommentId", lastCreatedCommentId.toString());

        HttpEntity<String> request = createJsonRequest(body, getTempAuthHeader());
        String url = TRIPS_ENDPOINT + "/" + lastCreatedTripId + "/comments";
        log.info("[Cucumber] POST {} request: {}", url, mapper.writeValueAsString(body));

        latestResponse = restTemplate.postForEntity(url, request, String.class);

        log.info(
                "[Cucumber] POST {} response status: {}",
                url,
                latestResponse.getStatusCode().value());
        log.info("[Cucumber] POST {} response body: {}", url, latestResponse.getBody());

        // Store reply ID if creation was successful
        if (latestResponse.getStatusCode().is2xxSuccessful()) {
            String responseBody = latestResponse.getBody();
            Map<?, ?> json = mapper.readValue(responseBody, Map.class);
            if (json.containsKey("id")) {
                UUID replyId = UUID.fromString(json.get("id").toString());
                log.info("[Cucumber] Stored reply ID: {}", replyId);
            }
        }
    }

    @When("I create a reply with message {string} on that comment without token")
    public void i_create_a_reply_with_message_on_that_comment_without_token(String message)
            throws Exception {
        setTempAuthHeader(null);
        i_create_a_reply_with_message_on_that_comment_using_that_token(message);
    }

    @When("I add a reaction {string} to that comment using that token")
    public void i_add_a_reaction_to_that_comment_using_that_token(String reactionType)
            throws Exception {
        Assertions.assertNotNull(lastCreatedCommentId, "No comment ID stored to add reaction");

        Map<String, Object> body = Map.of("reactionType", reactionType);

        HttpEntity<String> request = createJsonRequest(body, getTempAuthHeader());
        String url = API_BASE + "/comments/" + lastCreatedCommentId + "/reactions";
        log.info("[Cucumber] POST {} request: {}", url, mapper.writeValueAsString(body));

        latestResponse = restTemplate.postForEntity(url, request, String.class);

        log.info(
                "[Cucumber] POST {} response status: {}",
                url,
                latestResponse.getStatusCode().value());
        log.info("[Cucumber] POST {} response body: {}", url, latestResponse.getBody());
    }

    @When("I add a reaction {string} to that comment without token")
    public void i_add_a_reaction_to_that_comment_without_token(String reactionType)
            throws Exception {
        setTempAuthHeader(null);
        i_add_a_reaction_to_that_comment_using_that_token(reactionType);
    }

    @When("I remove a reaction {string} from that comment using that token")
    public void i_remove_a_reaction_from_that_comment_using_that_token(String reactionType)
            throws Exception {
        Assertions.assertNotNull(lastCreatedCommentId, "No comment ID stored to remove reaction");

        Map<String, Object> body = Map.of("reactionType", reactionType);

        HttpEntity<String> request = createJsonRequest(body, getTempAuthHeader());
        String url = API_BASE + "/comments/" + lastCreatedCommentId + "/reactions";
        log.info("[Cucumber] DELETE {} request: {}", url, mapper.writeValueAsString(body));

        latestResponse =
                restTemplate.exchange(
                        url, org.springframework.http.HttpMethod.DELETE, request, String.class);

        log.info(
                "[Cucumber] DELETE {} response status: {}",
                url,
                latestResponse.getStatusCode().value());
        log.info("[Cucumber] DELETE {} response body: {}", url, latestResponse.getBody());
    }

    @When("I remove a reaction {string} from that comment without token")
    public void i_remove_a_reaction_from_that_comment_without_token(String reactionType)
            throws Exception {
        setTempAuthHeader(null);
        i_remove_a_reaction_from_that_comment_using_that_token(reactionType);
    }

    @And("the response contains a comment id")
    public void the_response_contains_a_comment_id() throws Exception {
        Assertions.assertNotNull(latestResponse);
        String body = latestResponse.getBody();
        Map<?, ?> json = mapper.readValue(body, Map.class);
        Assertions.assertTrue(json.containsKey("id"), "Response does not contain 'id' field");
        UUID parsed = UUID.fromString(json.get("id").toString());
        Assertions.assertNotNull(parsed);
    }

    private HttpEntity<String> createJsonRequest(Map<String, Object> body)
            throws JsonProcessingException {
        return createJsonRequest(body, null);
    }

    private HttpEntity<String> createJsonRequest(Map<String, Object> body, String authHeader)
            throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (authHeader != null) headers.set("Authorization", authHeader);
        return new HttpEntity<>(mapper.writeValueAsString(body), headers);
    }

    private String buildTokenForLastUser(
            String roles, String secret, Map<String, Object> extraClaims) throws Exception {
        String username = getLastCreatedUsername();
        Assertions.assertNotNull(username, "No known user to create a token for");
        Optional<?> opt = userRepository.findByUsername(username);
        UUID id;
        if (opt.isPresent()) {
            id = ((User) opt.get()).getId();
        } else {
            var list = userRepository.findAll();
            Assertions.assertFalse(list.isEmpty(), "No users available to create token");
            id = list.getLast().getId();
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", id.toString());
        payload.put("roles", roles);
        if (extraClaims != null) payload.putAll(extraClaims);
        return JwtBuilder.buildJwt(payload, secret);
    }
}
