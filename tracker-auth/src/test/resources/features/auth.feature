Feature: Authentication

  Scenario: Successful user registration
    Given an empty auth system
    When I register a user with username "testuser", email "test@example.com", and password "password123"
    Then the response status should be 201
    And the response should contain a JWT token
    And the response should contain a refresh token

  Scenario: Successful user login
    Given an empty auth system
    When I register a user with username "testuser", email "test@example.com", and password "password123"
    Then the response status should be 201
    When I login with username "testuser" and password "password123"
    Then the response status should be 200
    And the response should contain a JWT token
    And the response should contain a refresh token

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

  Scenario: Refresh access token
    Given an empty auth system
    When I register a user with username "testuser", email "test@example.com", and password "password123"
    Then the response status should be 201
    And I save the refresh token
    When I refresh the access token
    Then the response status should be 200
    And the response should contain a JWT token
    And the response should contain a refresh token

  Scenario: Logout successfully
    Given an empty auth system
    When I register a user with username "testuser", email "test@example.com", and password "password123"
    Then the response status should be 201
    And I save the access token
    When I logout
    Then the response status should be 200

  Scenario: Password reset flow
    Given an empty auth system
    When I register a user with username "testuser", email "test@example.com", and password "password123"
    Then the response status should be 201
    When I request password reset for email "test@example.com"
    Then the response status should be 200
    And I save the reset token
    When I reset password with new password "newPassword123"
    Then the response status should be 200

  Scenario: Change password
    Given an empty auth system
    When I register a user with username "testuser", email "test@example.com", and password "password123"
    Then the response status should be 201
    And I save the access token
    When I change password from "password123" to "newPassword456"
    Then the response status should be 200
