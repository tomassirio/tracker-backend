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

  Scenario: Followed users trips appear first in public trips
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And a user exists with username "bob" and email "bob@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip with name "Bob's Trip" using that token
    And a user exists with username "charlie" and email "charlie@example.com"
    And I have a valid token for that user with roles "USER"
    And I create a trip with name "Charlie's Trip" using that token
    And I have a valid token for the first user with roles "USER"
    And I follow user "bob" using that token
    When I get ongoing public trips using that token
    Then the response status should be 200
    And the first trip should be from followed user
