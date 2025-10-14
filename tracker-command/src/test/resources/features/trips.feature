Feature: Trips

  Background:
    Given an empty system

  Scenario: Create a trip with valid token
    Given a user exists with username "bob" and email "bob@example.com"
    And I have a valid token for that user with roles "USER"
    When I create a trip with name "Test Trip" using that token
    Then the response status should be 201

  Scenario: Create a trip without token results in 401
    Given a user exists with username "carol" and email "carol@example.com"
    When I create a trip with name "No Token Trip" without token
    Then the response status should be 401

  Scenario: Create a trip with token missing scope results in 403
    Given a user exists with username "dave" and email "dave@example.com"
    And I have a valid token for that user with roles "NOT_USER"
    When I create a trip with name "No Scope Trip" using that token
    Then the response status should be 403

  Scenario: Create a trip with invalid signature token results in 401
    Given a user exists with username "eve" and email "eve@example.com"
    And I have an invalidly signed token for that user with roles "USER"
    When I create a trip with name "BadSig Trip" using that token
    Then the response status should be 401

  Scenario: Update a trip owned by the user succeeds
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip with name "My Trip" using that token
    When I update that trip with name "Updated Trip" using that token
    Then the response status should be 200
    And the trip name should be "Updated Trip"

  Scenario: Update a trip owned by another user fails with 403
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip with name "Alice's Trip" using that token
    And a user exists with username "bob" and email "bob@example.com"
    And I have a valid token for that user with roles "USER"
    When I update that trip with name "Bob's Trip" using that token
    Then the response status should be 403

  Scenario: Update a trip without token results in 401
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip with name "My Trip" using that token
    When I update that trip with name "Updated Trip" without token
    Then the response status should be 401

  Scenario: Delete a trip owned by the user succeeds
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip with name "My Trip" using that token
    When I delete that trip using that token
    Then the response status should be 204

  Scenario: Delete a trip owned by another user fails with 403
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip with name "Alice's Trip" using that token
    And a user exists with username "bob" and email "bob@example.com"
    And I have a valid token for that user with roles "USER"
    When I delete that trip using that token
    Then the response status should be 403

  Scenario: Delete a trip without token results in 401
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip with name "My Trip" using that token
    When I delete that trip without token
    Then the response status should be 401

  Scenario: Change trip visibility owned by the user succeeds
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip with name "My Trip" using that token
    When I change that trip visibility to "PRIVATE" using that token
    Then the response status should be 200

  Scenario: Change trip visibility owned by another user fails with 403
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip with name "Alice's Trip" using that token
    And a user exists with username "bob" and email "bob@example.com"
    And I have a valid token for that user with roles "USER"
    When I change that trip visibility to "PRIVATE" using that token
    Then the response status should be 403

  Scenario: Change trip status owned by the user succeeds
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip with name "My Trip" using that token
    When I change that trip status to "IN_PROGRESS" using that token
    Then the response status should be 200

  Scenario: Change trip status owned by another user fails with 403
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip with name "Alice's Trip" using that token
    And a user exists with username "bob" and email "bob@example.com"
    And I have a valid token for that user with roles "USER"
    When I change that trip status to "IN_PROGRESS" using that token
    Then the response status should be 403

  Scenario: Add trip update to owned trip succeeds
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip with name "My Trip" using that token
    When I add a trip update with message "Checkpoint 1" using that token
    Then the response status should be 201

  Scenario: Add trip update to another user's trip fails with 403
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip with name "Alice's Trip" using that token
    And a user exists with username "bob" and email "bob@example.com"
    And I have a valid token for that user with roles "USER"
    When I add a trip update with message "Bob's update" using that token
    Then the response status should be 403
