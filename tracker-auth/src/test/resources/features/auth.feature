Feature: Authentication

  Scenario: Successful user registration
    Given an empty auth system
    When I register a user with username "testuser", email "test@example.com", and password "password123"
    Then the response status should be 201
    And the response should contain a JWT token

  Scenario: Successful user login
    Given an empty auth system
    When I register a user with username "testuser", email "test@example.com", and password "password123"
    Then the response status should be 201
    When I login with username "testuser" and password "password123"
    Then the response status should be 200
    And the response should contain a JWT token

  Scenario: Login with invalid credentials
    Given an empty auth system
    When I login with username "nonexistent" and password "wrongpassword"
    Then the response status should be 400
    And the response should contain an error message

  Scenario: Register with invalid data
    Given an empty auth system
    When I register a user with username "tu", email "invalid-email", and password "short"
    Then the response status should be 400
    And the response should contain an error message
