Feature: Trips Query

  Background:
    Given an empty system

  Scenario: Get all trips
    Given a user exists with username "bob" and email "bob@example.com"
    And I have a valid token for that user with roles "ADMIN"
    When I get all trips
    Then the response status should be 200

  Scenario: Get a specific trip
    Given a user exists with username "alice" and email "alice@example.com"
    And a trip exists with name "Test Trip"
    And I have a valid token for that user with roles "USER"
    When I get the last created trip
    Then the response status should be 200
    And the response contains a trip id

  Scenario: Get my trips
    Given a user exists with username "carol" and email "carol@example.com"
    And a trip exists with name "My Trip"
    And I have a valid token for that user with roles "USER"
    When I get my trips
    Then the response status should be 200
    And the response contains at least one trip id
    And the response owner id should match user "carol"

  Scenario: Get my trips unauthorized
    Given a user exists with username "eve" and email "eve@example.com"
    And a trip exists with name "Eve's Trip"
    When I get my trips
    Then the response status should be 401

  Scenario: Get my trips as wrong owner
    Given a user exists with username "carol" and email "carol@example.com"
    And a trip exists with name "My Trip"
    And a user exists with username "dave" and email "dave@example.com"
    And I have a valid token for that user with roles "USER"
    When I get my trips
    Then the response status should be 200
    And the response should be empty
