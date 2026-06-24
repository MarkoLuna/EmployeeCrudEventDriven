Feature: Employee CRUD Operations
  As an authorized user
  I want to perform CRUD operations on employees
  So that I can manage employee data through the event-driven system

  Background:
    Given I am authenticated as "john@test.com" with password "123"

  Scenario: Create a new employee
    When I create an employee with:
      | firstName       | Mark      |
      | middleInitial   | L         |
      | lastName        | Luna      |
      | dateOfBirth     | 17-09-2012 |
      | dateOfEmployment | 17-09-2014 |
    Then the response status should be 202
    And the employee response should contain firstName "Mark"

  Scenario: Retrieve an employee by ID
    Given an employee exists
    When I retrieve the employee by its ID
    Then the response status should be 200
    And the employee data should match the created employee

  Scenario: List employees with pagination
    When I request employees page 0 with size 10
    Then the response status should be 200
    And the response should contain an employee page

  Scenario: Update an existing employee
    Given an employee exists
    When I update the employee with:
      | firstName | UpdatedName |
    Then the response status should be 202

  Scenario: Delete an existing employee
    Given an employee exists
    When I delete the employee
    Then the response status should be 202
