Feature: User Follows Query

  Background:
    Given an empty system

  Scenario: Query following list
    Given user "alice" with email "alice@example.com" exists
    And user "bob" with email "bob@example.com" exists
    And user "charlie" with email "charlie@example.com" exists
    And user "alice" follows user "bob"
    And user "alice" follows user "charlie"
    When user "alice" queries their following list
    Then the response status code should be 200
    And the response should contain 2 follows

  Scenario: Query followers list
    Given user "alice" with email "alice@example.com" exists
    And user "bob" with email "bob@example.com" exists
    And user "charlie" with email "charlie@example.com" exists
    And user "bob" follows user "alice"
    And user "charlie" follows user "alice"
    When user "alice" queries their followers list
    Then the response status code should be 200
    And the response should contain 2 follows

  Scenario: Query following list with no follows
    Given user "alice" with email "alice@example.com" exists
    When user "alice" queries their following list
    Then the response status code should be 200
    And the response should be an empty list

  Scenario: Query followers list with no followers
    Given user "alice" with email "alice@example.com" exists
    When user "alice" queries their followers list
    Then the response status code should be 200
    And the response should be an empty list

  Scenario: Following list only shows current user's follows
    Given user "alice" with email "alice@example.com" exists
    And user "bob" with email "bob@example.com" exists
    And user "charlie" with email "charlie@example.com" exists
    And user "alice" follows user "bob"
    And user "charlie" follows user "bob"
    When user "alice" queries their following list
    Then the response status code should be 200
    And the response should contain 1 follows

  Scenario: Followers list only shows users following current user
    Given user "alice" with email "alice@example.com" exists
    And user "bob" with email "bob@example.com" exists
    And user "charlie" with email "charlie@example.com" exists
    And user "bob" follows user "alice"
    And user "alice" follows user "charlie"
    When user "alice" queries their followers list
    Then the response status code should be 200
    And the response should contain 1 follows

