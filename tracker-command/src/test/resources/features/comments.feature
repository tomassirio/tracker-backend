Feature: Comments

  Background:
    Given an empty system

  Scenario: Create a top-level comment on a trip with valid token
    Given a user exists with username "bob" and email "bob@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip with name "Test Trip" using that token
    When I create a comment with message "Great trip!" on that trip using that token
    Then the response status should be 201
    And the response contains a comment id

  Scenario: Create a top-level comment without token results in 401
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip with name "Test Trip" using that token
    When I create a comment with message "Nice!" on that trip without token
    Then the response status should be 401

  Scenario: Create a top-level comment with token missing scope results in 403
    Given a user exists with username "dave" and email "dave@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip with name "Test Trip" using that token
    And I have a valid token for that user with roles "NOT_USER"
    When I create a comment with message "Cool!" on that trip using that token
    Then the response status should be 403

  Scenario: Create a top-level comment with invalid signature token results in 401
    Given a user exists with username "eve" and email "eve@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip with name "Test Trip" using that token
    And I have an invalidly signed token for that user with roles "USER"
    When I create a comment with message "Amazing!" on that trip using that token
    Then the response status should be 401

  Scenario: Create a reply to a comment with valid token
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip with name "Test Trip" using that token
    And I create a comment with message "First comment!" on that trip using that token
    When I create a reply with message "Reply to first" on that comment using that token
    Then the response status should be 201
    And the response contains a comment id

  Scenario: Create a reply without token results in 401
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip with name "Test Trip" using that token
    And I create a comment with message "First comment!" on that trip using that token
    When I create a reply with message "Reply" on that comment without token
    Then the response status should be 401

  Scenario: Add a reaction to a comment with valid token
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip with name "Test Trip" using that token
    And I create a comment with message "First comment!" on that trip using that token
    When I add a reaction "HEART" to that comment using that token
    Then the response status should be 200

  Scenario: Add a reaction to a comment without token results in 401
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip with name "Test Trip" using that token
    And I create a comment with message "First comment!" on that trip using that token
    When I add a reaction "HEART" to that comment without token
    Then the response status should be 401

  Scenario: Add a reaction with token missing scope results in 403
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip with name "Test Trip" using that token
    And I create a comment with message "First comment!" on that trip using that token
    And I have a valid token for that user with roles "NOT_USER"
    When I add a reaction "HEART" to that comment using that token
    Then the response status should be 403

  Scenario: Remove a reaction from a comment with valid token
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip with name "Test Trip" using that token
    And I create a comment with message "First comment!" on that trip using that token
    And I add a reaction "HEART" to that comment using that token
    When I remove a reaction "HEART" from that comment using that token
    Then the response status should be 200

  Scenario: Remove a reaction from a comment without token results in 401
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip with name "Test Trip" using that token
    And I create a comment with message "First comment!" on that trip using that token
    And I add a reaction "HEART" to that comment using that token
    When I remove a reaction "HEART" from that comment without token
    Then the response status should be 401

  Scenario: Multiple users can comment on the same trip
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip with name "Shared Trip" using that token
    And I create a comment with message "Alice's comment" on that trip using that token
    And a user exists with username "bob" and email "bob@example.com"
    And I have a valid token for that user with roles "USER"
    When I create a comment with message "Bob's comment" on that trip using that token
    Then the response status should be 201
    And the response contains a comment id
