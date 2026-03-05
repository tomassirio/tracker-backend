Feature: Trip Plans

  Background:
    Given an empty system

  Scenario: Create a trip plan with valid token
    Given a user exists with username "bob" and email "bob@example.com"
    And I have a valid token for that user with roles "USER"
    When I create a trip plan with name "Test Trip Plan" using that token
    Then the response status should be 201

  Scenario: Create a trip plan without token results in 401
    Given a user exists with username "carol" and email "carol@example.com"
    When I create a trip plan with name "No Token Plan" without token
    Then the response status should be 401

  Scenario: Create a trip plan with token missing scope results in 403
    Given a user exists with username "dave" and email "dave@example.com"
    And I have a valid token for that user with roles "NOT_USER"
    When I create a trip plan with name "No Scope Plan" using that token
    Then the response status should be 403

  Scenario: Create a trip plan with invalid signature token results in 401
    Given a user exists with username "eve" and email "eve@example.com"
    And I have an invalidly signed token for that user with roles "USER"
    When I create a trip plan with name "BadSig Plan" using that token
    Then the response status should be 401

  Scenario: Update a trip plan owned by the user succeeds
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip plan with name "My Plan" using that token
    When I update that trip plan with name "Updated Plan" using that token
    Then the response status should be 200
    And the trip plan name should be "Updated Plan"

  Scenario: Update a trip plan owned by another user fails with 403
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip plan with name "Alice's Plan" using that token
    And a user exists with username "bob" and email "bob@example.com"
    And I have a valid token for that user with roles "USER"
    When I update that trip plan with name "Bob's Plan" using that token
    Then the response status should be 403

  Scenario: Update a trip plan without token results in 401
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip plan with name "My Plan" using that token
    When I update that trip plan with name "Updated Plan" without token
    Then the response status should be 401

  Scenario: Delete a trip plan owned by the user succeeds
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip plan with name "My Plan" using that token
    When I delete that trip plan using that token
    Then the response status should be 204

  Scenario: Delete a trip plan owned by another user fails with 403
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip plan with name "Alice's Plan" using that token
    And a user exists with username "bob" and email "bob@example.com"
    And I have a valid token for that user with roles "USER"
    When I delete that trip plan using that token
    Then the response status should be 403

  Scenario: Delete a trip plan without token results in 401
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip plan with name "My Plan" using that token
    When I delete that trip plan without token
    Then the response status should be 401

