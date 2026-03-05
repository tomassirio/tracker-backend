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

  Scenario: Get available trips - user with own trips
    Given a user exists with username "alice" and email "alice@example.com"
    And a trip exists with name "Alice's Public Trip" and visibility "PUBLIC"
    And a trip exists with name "Alice's Private Trip" and visibility "PRIVATE"
    And I have a valid token for that user with roles "USER"
    When I get available trips
    Then the response status should be 200
    And the response should contain 2 trips

  Scenario: Get available trips - includes public trips from other users
    Given a user exists with username "alice" and email "alice@example.com"
    And a trip exists with name "Alice's Trip" and visibility "PUBLIC"
    And a user exists with username "bob" and email "bob@example.com"
    And a trip exists with name "Bob's Public Trip" and visibility "PUBLIC"
    And I have a valid token for that user with roles "USER"
    When I get available trips
    Then the response status should be 200
    And the response should contain 2 trips

  Scenario: Get available trips - excludes protected trips from non-friends
    Given a user exists with username "alice" and email "alice@example.com"
    And a trip exists with name "Alice's Protected Trip" and visibility "PROTECTED"
    And a user exists with username "bob" and email "bob@example.com"
    And I have a valid token for that user with roles "USER"
    When I get available trips
    Then the response status should be 200
    And the response should be empty

  Scenario: Get available trips - includes protected trips from friends
    Given a user exists with username "alice" and email "alice@example.com"
    And a trip exists with name "Alice's Protected Trip" and visibility "PROTECTED"
    And a user exists with username "bob" and email "bob@example.com"
    And user "alice" and user "bob" are friends
    And I have a valid token for that user with roles "USER"
    When I get available trips
    Then the response status should be 200
    And the response should contain 1 trips

  Scenario: Get available trips - complex scenario with multiple users and visibilities
    Given a user exists with username "alice" and email "alice@example.com"
    And a trip exists with name "Alice's Public Trip" and visibility "PUBLIC"
    And a trip exists with name "Alice's Protected Trip" and visibility "PROTECTED"
    And a trip exists with name "Alice's Private Trip" and visibility "PRIVATE"
    And a user exists with username "bob" and email "bob@example.com"
    And a trip exists with name "Bob's Public Trip" and visibility "PUBLIC"
    And a trip exists with name "Bob's Protected Trip" and visibility "PROTECTED"
    And a user exists with username "carol" and email "carol@example.com"
    And a trip exists with name "Carol's Own Trip" and visibility "PRIVATE"
    And user "carol" and user "alice" are friends
    And I have a valid token for that user with roles "USER"
    When I get available trips
    Then the response status should be 200
    And the response should contain 4 trips

  Scenario: Get available trips - unauthorized
    Given a user exists with username "alice" and email "alice@example.com"
    And a trip exists with name "Alice's Trip"
    When I get available trips
    Then the response status should be 401

  Scenario: Get available trips - no trips available
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    When I get available trips
    Then the response status should be 200
    And the response should be empty
