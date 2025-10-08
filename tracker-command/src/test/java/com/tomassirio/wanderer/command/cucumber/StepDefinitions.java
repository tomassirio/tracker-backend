package com.tomassirio.wanderer.command.cucumber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class StepDefinitions {

    // obtain scenario-scoped TestScenarioContext lazily from the Spring application context
    private TestScenarioContext ctx() {
        return SpringTestContextProvider.getBean(TestScenarioContext.class);
    }

    private ResponseEntity<String> latestResponse;

    private static final Logger log = LoggerFactory.getLogger(StepDefinitions.class);

    private final ObjectMapper mapper = new ObjectMapper();

    public StepDefinitions() {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Given("an empty system")
    public void an_empty_system() {
        // clear repositories to ensure clean state
        ctx().trips().deleteAll();
        ctx().users().deleteAll();
        ctx().setTempAuthHeader(null);
        ctx().setLastCreatedUsername(null);
    }

    @When("I create a user with username {string} and email {string}")
    public void i_create_a_user_with_username_and_email(String username, String email)
            throws Exception {
        ctx().setLastCreatedUsername(username);
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("email", email);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(mapper.writeValueAsString(body), headers);
        latestResponse = ctx().rest().postForEntity("/api/1/users", request, String.class);
        log.info(
                "[Cucumber] POST /api/1/users response status: {}",
                latestResponse.getStatusCode().value());
        log.info("[Cucumber] POST /api/1/users response body: {}", latestResponse.getBody());
    }

    @When("I create a trip with name {string} using that token")
    public void i_create_a_trip_with_name_using_that_token(String name) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("startDate", LocalDate.now());
        body.put("endDate", LocalDate.now().plusDays(5));
        body.put("totalDistance", 100.0);
        body.put("visibility", "PUBLIC");
        Map<String, Object> loc = new HashMap<>();
        loc.put("latitude", 10.0);
        loc.put("longitude", 20.0);
        loc.put("altitude", 0.0);
        body.put("startingLocation", loc);
        body.put("endingLocation", loc);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (ctx().getTempAuthHeader() != null)
            headers.set("Authorization", ctx().getTempAuthHeader());
        HttpEntity<String> request = new HttpEntity<>(mapper.writeValueAsString(body), headers);

        log.info("[Cucumber] POST /api/1/trips request: {}", mapper.writeValueAsString(body));
        latestResponse = ctx().rest().postForEntity("/api/1/trips", request, String.class);

        log.info(
                "[Cucumber] POST /api/1/trips response status: {}",
                latestResponse.getStatusCode().value());
        log.info("[Cucumber] POST /api/1/trips response body: {}", latestResponse.getBody());
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

    @Given("I have a valid token for that user with scopes {string}")
    public void i_have_a_valid_token_for_that_user_with_scopes(String scopes) throws Exception {
        String username = ctx().getLastCreatedUsername();
        Assertions.assertNotNull(username, "No known user to create a token for");
        var opt = ctx().users().findByUsername(username);
        UUID id;
        if (opt.isPresent()) {
            id = opt.get().getId();
        } else {
            var list = ctx().users().findAll();
            Assertions.assertFalse(list.isEmpty(), "No users available to create token");
            id = list.getLast().getId();
        }
        String token = ctx().buildJwt(id.toString(), scopes, "test-secret", null);
        ctx().setTempAuthHeader("Bearer " + token);
    }

    @Given("I have an invalidly signed token for that user with scopes {string}")
    public void i_have_an_invalidly_signed_token_for_that_user_with_scopes(String scopes)
            throws Exception {
        String username = ctx().getLastCreatedUsername();
        Assertions.assertNotNull(username, "No known user to create a token for");
        var opt = ctx().users().findByUsername(username);
        UUID id;
        if (opt.isPresent()) {
            id = opt.get().getId();
        } else {
            var list = ctx().users().findAll();
            Assertions.assertFalse(list.isEmpty(), "No users available to create token");
            id = list.getLast().getId();
        }
        String token = ctx().buildJwt(id.toString(), scopes, "bad-secret", null);
        ctx().setTempAuthHeader("Bearer " + token);
    }

    @Given("I have a token for that user that has an expired exp claim and scopes {string}")
    public void i_have_a_token_for_that_user_that_has_an_expired_exp_claim_and_scopes(String scopes)
            throws Exception {
        String username = ctx().getLastCreatedUsername();
        Assertions.assertNotNull(username, "No known user to create a token for");
        var opt = ctx().users().findByUsername(username);
        UUID id;
        if (opt.isPresent()) {
            id = opt.get().getId();
        } else {
            var list = ctx().users().findAll();
            Assertions.assertFalse(list.isEmpty(), "No users available to create token");
            id = list.getLast().getId();
        }

        long past = LocalDate.now().minusDays(10).atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        Map<String, Object> extra = Map.of("exp", past);
        String token = ctx().buildJwt(id.toString(), scopes, "test-secret", extra);
        ctx().setTempAuthHeader("Bearer " + token);
    }

    @When("I create a trip with name {string} without token")
    public void i_create_a_trip_without_token(String name) throws Exception {
        ctx().setTempAuthHeader(null);
        i_create_a_trip_with_name_using_that_token(name);
    }
}
