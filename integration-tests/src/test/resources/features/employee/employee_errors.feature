Feature: Employee Error Handling
  As an API consumer
  I want to receive appropriate error responses
  So that I can handle error cases correctly

  Background:
    Given I am authenticated as "john@test.com" with password "123"

  Scenario: Create employee with invalid data returns 400
    When I create an employee with:
      | firstName       |           |
      | lastName        |           |
      | dateOfBirth     | 17-09-2012 |
      | dateOfEmployment | 17-09-2014 |
    Then the response status should be 400

  Scenario: Retrieve non-existent employee returns 404
    When I retrieve employee with ID "00000000-0000-0000-0000-000000000000"
    Then the response status should be 404

  Scenario: Unauthenticated request returns 401
    Given I am not authenticated
    When I request employees page 0 with size 10
    Then the response status should be 401
