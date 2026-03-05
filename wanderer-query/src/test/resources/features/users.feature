Feature: Users Query

  Background:
    Given an empty system

  Scenario: Get user by username
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    When I get user by username "alice"
    Then the response status should be 200

  Scenario: Get my user
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    When I get my user
    Then the response status should be 200
    And the response contains a user id
    And the response username should match user "alice"

  Scenario: Get my user unauthorized
    Given a user exists with username "bob" and email "bob@example.com"
    When I get my user
    Then the response status should be 401
