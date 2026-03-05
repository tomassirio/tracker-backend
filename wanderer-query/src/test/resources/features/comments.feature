Feature: Comments Query

  Background:
    Given an empty system

  Scenario: Get all comments for a trip
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And a trip exists with name "Test Trip"
    And a comment exists with message "Great trip!" on that trip
    When I get all comments for that trip
    Then the response status should be 200
    And the response contains at least one comment id

  Scenario: Get all comments for a trip without token results in 401
    Given a user exists with username "bob" and email "bob@example.com"
    And a trip exists with name "Test Trip"
    And a comment exists with message "Nice trip!" on that trip
    When I get all comments for that trip without token
    Then the response status should be 401

  Scenario: Get a specific comment by id
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And a trip exists with name "Test Trip"
    And a comment exists with message "Specific comment" on that trip
    When I get that comment by id
    Then the response status should be 200
    And the response contains a comment id
    And the comment message should be "Specific comment"

  Scenario: Get a specific comment without token results in 401
    Given a user exists with username "bob" and email "bob@example.com"
    And a trip exists with name "Test Trip"
    And a comment exists with message "Hidden comment" on that trip
    When I get that comment by id without token
    Then the response status should be 401

  Scenario: Get comments with reactions
    Given a user exists with username "alice" and email "alice@example.com"
    And I have a valid token for that user with roles "USER"
    And a trip exists with name "Test Trip"
    And a comment exists with message "Popular comment" on that trip
    And a reaction "HEART" exists on that comment
    When I get that comment by id
    Then the response status should be 200
    And the response contains a comment id
    And the comment should have at least one reaction
