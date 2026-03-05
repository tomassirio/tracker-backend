Feature: Friendship Query

  Background:
    Given an empty system

  Scenario: Query friends list
    Given user "alice" with email "alice@example.com" exists
    And user "bob" with email "bob@example.com" exists
    And user "charlie" with email "charlie@example.com" exists
    And user "alice" and user "bob" are friends
    And user "alice" and user "charlie" are friends
    When user "alice" queries their friends list
    Then the response status code should be 200
    And the response should contain 2 friends

  Scenario: Query friends list with no friends
    Given user "alice" with email "alice@example.com" exists
    When user "alice" queries their friends list
    Then the response status code should be 200
    And the response should be an empty list

  Scenario: Friends list is bidirectional
    Given user "alice" with email "alice@example.com" exists
    And user "bob" with email "bob@example.com" exists
    And user "alice" and user "bob" are friends
    When user "alice" queries their friends list
    Then the response status code should be 200
    And the response should contain 1 friends
    When user "bob" queries their friends list
    Then the response status code should be 200
    And the response should contain 1 friends

  Scenario: Friends list only shows accepted friend requests
    Given user "alice" with email "alice@example.com" exists
    And user "bob" with email "bob@example.com" exists
    And user "charlie" with email "charlie@example.com" exists
    And user "alice" sends a friend request to user "bob"
    And user "alice" and user "charlie" are friends
    When user "alice" queries their friends list
    Then the response status code should be 200
    And the response should contain 1 friends

  Scenario: Unfriending removes from friends list
    Given user "alice" with email "alice@example.com" exists
    And user "bob" with email "bob@example.com" exists
    And user "alice" and user "bob" are friends
    And user "alice" and user "bob" are no longer friends
    When user "alice" queries their friends list
    Then the response status code should be 200
    And the response should be an empty list

