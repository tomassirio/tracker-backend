package com.tomassirio.wanderer.auth.cucumber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Map;
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

public class StepDefinitions {

    @Autowired private TestRestTemplate restTemplate;

    private ResponseEntity<String> latestResponse;

    private static final Logger log = LoggerFactory.getLogger(StepDefinitions.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // per-scenario state
    @Getter @Setter private String tempAuthToken;

    @Given("an empty auth system")
    public void an_empty_auth_system() {
        setTempAuthToken(null);
    }

    @When("I register a user with username {string}, email {string}, and password {string}")
    public void i_register_a_user_with_username_email_and_password(
            String username, String email, String password) throws Exception {
        Map<String, Object> body =
                Map.of("username", username, "email", email, "password", password);
        HttpEntity<String> request = createJsonRequest(body);
        latestResponse = restTemplate.postForEntity("/api/1/auth/register", request, String.class);
        log.info(
                "[Cucumber] POST /api/1/auth/register response status: {}",
                latestResponse.getStatusCode().value());
        log.info(
                "[Cucumber] POST /api/1/auth/register response body: {}", latestResponse.getBody());
    }

    @When("I login with username {string} and password {string}")
    public void i_login_with_username_and_password(String username, String password)
            throws Exception {
        Map<String, Object> body = Map.of("username", username, "password", password);
        HttpEntity<String> request = createJsonRequest(body);
        latestResponse = restTemplate.postForEntity("/api/1/auth/login", request, String.class);
        log.info(
                "[Cucumber] POST /api/1/auth/login response status: {}",
                latestResponse.getStatusCode().value());
        log.info("[Cucumber] POST /api/1/auth/login response body: {}", latestResponse.getBody());
    }

    @Then("the response status should be {int}")
    public void the_response_status_should_be(int expected) {
        Assertions.assertNotNull(latestResponse, "No response recorded");
        int actual = latestResponse.getStatusCode().value();
        Assertions.assertEquals(
                expected,
                actual,
                String.format(
                        "Expected status %d but got %d. Response body: %s",
                        expected, actual, latestResponse.getBody()));
    }

    @SuppressWarnings("unchecked")
    @And("the response should contain a JWT token")
    public void the_response_should_contain_a_jwt_token() throws Exception {
        Assertions.assertNotNull(latestResponse.getBody(), "Response body is null");
        Map<String, Object> responseBody =
                (Map<String, Object>) mapper.readValue(latestResponse.getBody(), Map.class);
        Assertions.assertTrue(
                responseBody.containsKey("accessToken"), "Response does not contain accessToken");
        Assertions.assertNotNull(responseBody.get("accessToken"), "accessToken is null");
        setTempAuthToken((String) responseBody.get("accessToken"));
    }

    @And("the response should contain an error message")
    public void the_response_should_contain_an_error_message() {
        Assertions.assertNotNull(latestResponse.getBody(), "Response body is null");
        // For error responses, the body might be a simple string or JSON
        log.info("Error response body: {}", latestResponse.getBody());
    }

    private HttpEntity<String> createJsonRequest(Map<String, Object> body)
            throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String jsonBody = mapper.writeValueAsString(body);
        return new HttpEntity<>(jsonBody, headers);
    }
}
