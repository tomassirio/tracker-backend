Feature: Friend Requests Query

  Background:
    Given an empty system

  Scenario: Query received friend requests
    Given user "alice" with email "alice@example.com" exists
    And user "bob" with email "bob@example.com" exists  
    And user "bob" sends a friend request to user "alice"
    When user "alice" queries their received friend requests
    Then the response status code should be 200
    And the response should contain friend requests

  Scenario: Query sent friend requests
    Given user "alice" with email "alice@example.com" exists
    And user "bob" with email "bob@example.com" exists
    And user "alice" sends a friend request to user "bob"
    When user "alice" queries their sent friend requests
    Then the response status code should be 200
    And the response should contain friend requests

  Scenario: Query with no received requests
    Given user "alice" with email "alice@example.com" exists
    When user "alice" queries their received friend requests
    Then the response status code should be 200
    And the response should be an empty list

  Scenario: Query with no sent requests
    Given user "alice" with email "alice@example.com" exists
    When user "alice" queries their sent friend requests
    Then the response status code should be 200
    And the response should be an empty list
