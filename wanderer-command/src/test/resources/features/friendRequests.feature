Feature: Friend Requests

  Background:
    Given an empty system

  Scenario: Send a friend request successfully
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And a user exists with username "bob" and email "bob@example.com"
    When I send a friend request to that user using that token
    Then the response status should be 201
    And the response contains a friend request id
    And the friend request status should be "PENDING"

  Scenario: Accept a friend request successfully
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And a user exists with username "bob" and email "bob@example.com"
    And I send a friend request to that user using that token
    And I have a valid token for that user with roles "USER"
    When I accept that friend request using that token
    Then the response status should be 200
    And the friend request status should be "ACCEPTED"

  Scenario: Decline a friend request successfully
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And a user exists with username "bob" and email "bob@example.com"
    And I send a friend request to that user using that token
    And I have a valid token for that user with roles "USER"
    When I decline that friend request using that token
    Then the response status should be 200
    And the friend request status should be "DECLINED"

  Scenario: Cannot send friend request to yourself
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    When I send a friend request to myself using that token
    Then the response status should be 400