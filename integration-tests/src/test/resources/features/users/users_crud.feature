Feature: Users CRUD Operations
  As an authorized user with manage-users role
  I want to perform CRUD operations on users
  So that I can manage user accounts through the users service

  Background:
    Given I am authenticated as "john@test.com" with password "123"

  Scenario: Create a new user
    When I create a user with:
      | username   | e2etest       |
      | firstName  | Test          |
      | lastName   | User          |
      | email      | e2etest@example.com |
      | password   | password123   |
    Then the response status should be 201

  Scenario: Retrieve a user by ID
    Given a user exists
    When I retrieve the user by its ID
    Then the response status should be 200
    And the user response should contain username "e2eexisting"

  Scenario: Retrieve a user by username
    Given a user exists
    When I retrieve the user by username
    Then the response status should be 200

  Scenario: List users with pagination
    When I request users page 0 with size 10
    Then the response status should be 200
    And the response should contain a user page

  Scenario: Update a user
    Given a user exists
    When I update the user with:
      | firstName | UpdatedE2E |
    Then the response status should be 204

  Scenario: Delete a user
    Given a user exists
    When I delete the user
    Then the response status should be 204
