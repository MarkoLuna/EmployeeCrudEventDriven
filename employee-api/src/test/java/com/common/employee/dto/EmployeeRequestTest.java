package com.common.employee.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EmployeeRequestTest {

  private static final LocalDate BASIC_DATE = LocalDate.of(2012, 9, 17);
  private static Validator validator;

  @BeforeAll
  static void setUpValidator() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void employeeIsEmpty() {
    EmployeeRequest employeeRequest = EmployeeRequest.builder().build();

    Set<ConstraintViolation<EmployeeRequest>> constraintViolations =
        validator.validate(employeeRequest);

    assertEquals(5, constraintViolations.size());
    //        assertEquals( "must not be null",
    // constraintViolations.iterator().next().getMessage());
  }

  @Test
  void employeeFirstNameIsEmpty() {
    EmployeeRequest employeeRequest = new EmployeeRequest("", "J", "Luna", BASIC_DATE, BASIC_DATE);

    Set<ConstraintViolation<EmployeeRequest>> constraintViolations =
        validator.validate(employeeRequest);

    assertEquals(1, constraintViolations.size());
    assertEquals("First Name cannot be empty", constraintViolations.iterator().next().getMessage());
  }

  @Test
  void employeeLastNameIsEmpty() {
    EmployeeRequest employeeRequest =
        new EmployeeRequest("Marcos", "J", "", BASIC_DATE, BASIC_DATE);

    Set<ConstraintViolation<EmployeeRequest>> constraintViolations =
        validator.validate(employeeRequest);

    assertEquals(1, constraintViolations.size());
    assertEquals("Last Name cannot be empty", constraintViolations.iterator().next().getMessage());
  }

  @Test
  void employeeMiddleInitialIsEmpty() {
    EmployeeRequest employeeRequest =
        new EmployeeRequest("Marcos", "", "Luna", BASIC_DATE, BASIC_DATE);

    Set<ConstraintViolation<EmployeeRequest>> constraintViolations =
        validator.validate(employeeRequest);

    assertEquals(1, constraintViolations.size());
    assertEquals(
        "Middle Initial cannot be empty", constraintViolations.iterator().next().getMessage());
  }

  @Test
  void employeeDateOfEmploymentIsNull() {
    EmployeeRequest employeeRequest = new EmployeeRequest("Gerardo", "J", "Luna", BASIC_DATE, null);

    Set<ConstraintViolation<EmployeeRequest>> constraintViolations =
        validator.validate(employeeRequest);

    assertEquals(1, constraintViolations.size());
    assertEquals("must not be null", constraintViolations.iterator().next().getMessage());
  }

  @Test
  void employeeDateOfBirthIsNull() {
    EmployeeRequest employeeRequest = new EmployeeRequest("Gerardo", "J", "Luna", null, BASIC_DATE);

    Set<ConstraintViolation<EmployeeRequest>> constraintViolations =
        validator.validate(employeeRequest);

    assertEquals(1, constraintViolations.size());
    assertEquals("must not be null", constraintViolations.iterator().next().getMessage());
  }

  @Test
  void employeeIsValid() {
    EmployeeRequest employeeRequest =
        new EmployeeRequest("Gerardo", "J", "Luna", BASIC_DATE, BASIC_DATE);

    Set<ConstraintViolation<EmployeeRequest>> constraintViolations =
        validator.validate(employeeRequest);

    assertEquals(0, constraintViolations.size());
  }
}
