Feature: Users Authorization (RBAC)
  As an API consumer
  I want role-based access control on user management endpoints
  So that only authorized users can manage user accounts

  Scenario: Basic user cannot create users (403 Forbidden)
    Given I am authenticated as "mike@other.com" with password "123"
    When I create a user with:
      | username   | unauth        |
      | firstName  | Unauthorized  |
      | lastName   | User          |
      | email      | unauth@example.com |
      | password   | password123   |
    Then the response status should be 403

  Scenario: Unauthenticated user cannot list users (401 Unauthorized)
    Given I am not authenticated
    When I request users page 0 with size 10
    Then the response status should be 401
