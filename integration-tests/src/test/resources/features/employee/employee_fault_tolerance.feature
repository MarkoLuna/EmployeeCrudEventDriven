Feature: Employee Fault Tolerance
  As a user of the employee system
  I want the system to handle service failures gracefully
  So that I get degraded responses instead of errors

  Background:
    Given I am authenticated as "john@test.com" with password "123"

  Scenario: List employees returns empty page when consumer is unavailable
    Given the employee consumer service is unavailable
    When I request employees page 0 with size 10
    Then the response status should be 200
    And the response should contain an empty employee list

  Scenario: Get employee returns 503 when consumer is unavailable
    Given the employee consumer service is unavailable
    When I retrieve employee with ID "non-existent"
    Then the response status should be 503

  Scenario: Create employee still works when consumer is unavailable
    Given the employee consumer service is unavailable
    When I create an employee with:
      | firstName       | Mark      |
      | middleInitial   | L         |
      | lastName        | Luna      |
      | dateOfBirth     | 17-09-2012 |
      | dateOfEmployment | 17-09-2014 |
    Then the response status should be 202
