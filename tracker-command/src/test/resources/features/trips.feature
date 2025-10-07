Feature: Trips

  Background:
    Given an empty system

  Scenario: Create a trip with valid token
    Given a user exists with username "bob" and email "bob@example.com"
    And I have a valid token for that user with scopes "login"
    When I create a trip with name "Test Trip" using that token
    Then the response status should be 201

  Scenario: Create a trip without token results in 401
    Given a user exists with username "carol" and email "carol@example.com"
    When I create a trip with name "No Token Trip" without token
    Then the response status should be 401

  Scenario: Create a trip with token missing scope results in 403
    Given a user exists with username "dave" and email "dave@example.com"
    And I have a valid token for that user with scopes "read"
    When I create a trip with name "No Scope Trip" using that token
    Then the response status should be 403

  Scenario: Create a trip with invalid signature token results in 401
    Given a user exists with username "eve" and email "eve@example.com"
    And I have an invalidly signed token for that user with scopes "login"
    When I create a trip with name "BadSig Trip" using that token
    Then the response status should be 401

  Scenario: Expired token (current behaviour) - included to document expectations
    Given a user exists with username "frank" and email "frank@example.com"
    And I have a token for that user that has an expired exp claim and scopes "login"
    When I create a trip with name "Expired Trip" using that token
    Then the response status should be 201
