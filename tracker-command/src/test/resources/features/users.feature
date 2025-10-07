Feature: Users

  Background:
    Given an empty system

  Scenario: Create a user successfully
    When I create a user with username "alice" and email "alice@example.com"
    Then the response status should be 201
    And the response contains a user id

