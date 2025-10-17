Feature: User Follows

  Background:
    Given an empty system

  Scenario: Follow a user successfully
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And a user exists with username "bob" and email "bob@example.com"
    When I follow that user using that token
    Then the response status should be 201
    And the response contains a follow id

  Scenario: Unfollow a user successfully
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And a user exists with username "bob" and email "bob@example.com"
    And I follow that user using that token
    When I unfollow that user using that token
    Then the response status should be 204

  Scenario: Cannot follow yourself
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    When I follow myself using that token
    Then the response status should be 400