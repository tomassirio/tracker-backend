Feature: Trips Query

  Background:
    Given an empty system

  Scenario: Get all trips
    Given a user exists with username "bob" and email "bob@example.com"
    And I have a valid token for that user with roles "USER"
    When I get all trips
    Then the response status should be 200

  Scenario: Get a specific trip
    Given a user exists with username "alice" and email "alice@example.com"
    And a trip exists with name "Test Trip"
    And I have a valid token for that user with roles "USER"
    When I get the last created trip
    Then the response status should be 200
    And the response contains a trip id
