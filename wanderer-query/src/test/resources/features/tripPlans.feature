Feature: Trip Plans Query

  Background:
    Given an empty system

  Scenario: Get a specific trip plan
    Given a user exists with username "alice" and email "alice@example.com"
    And a trip plan exists with name "Test Trip Plan"
    And I have a valid token for that user with roles "USER"
    When I get the last created trip plan
    Then the response status should be 200
    And the response contains a trip plan id

  Scenario: Get my trip plans
    Given a user exists with username "carol" and email "carol@example.com"
    And a trip plan exists with name "My Trip Plan"
    And I have a valid token for that user with roles "USER"
    When I get my trip plans
    Then the response status should be 200
    And the response contains at least one trip plan id
    And the response owner id should match user "carol"

  Scenario: Get my trip plans unauthorized
    Given a user exists with username "eve" and email "eve@example.com"
    And a trip plan exists with name "Eve's Trip Plan"
    When I get my trip plans
    Then the response status should be 401

  Scenario: Get my trip plans as wrong owner
    Given a user exists with username "carol" and email "carol@example.com"
    And a trip plan exists with name "My Trip Plan"
    And a user exists with username "dave" and email "dave@example.com"
    And I have a valid token for that user with roles "USER"
    When I get my trip plans
    Then the response status should be 200
    And the response should be empty

  Scenario: Get trip plan by id
    Given a user exists with username "alice" and email "alice@example.com"
    And a trip plan exists with name "Specific Plan"
    And I have a valid token for that user with roles "USER"
    When I get that trip plan by id
    Then the response status should be 200
    And the response contains a trip plan id
    And the trip plan name should be "Specific Plan"

  Scenario: Get trip plan by id without token results in 401
    Given a user exists with username "bob" and email "bob@example.com"
    And a trip plan exists with name "Bob's Plan"
    When I get that trip plan by id without token
    Then the response status should be 401
