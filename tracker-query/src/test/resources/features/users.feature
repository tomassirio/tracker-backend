Feature: Users Query

  Background:
    Given an empty system

  Scenario: Get user by username
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    When I get user by username "alice"
    Then the response status should be 200
