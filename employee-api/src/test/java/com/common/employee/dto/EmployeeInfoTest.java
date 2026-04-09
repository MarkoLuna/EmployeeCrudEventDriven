package com.common.employee.dto;

import com.common.employee.enums.EmployeeStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static jakarta.validation.Validation.buildDefaultValidatorFactory;
import static org.assertj.core.api.Assertions.assertThat;

class EmployeeInfoTest {

  private static final LocalDate BASIC_DATE = LocalDate.of(2012, 9, 17);
  private static Validator validator;

  @BeforeAll
  static void setUpValidator() {
      try (ValidatorFactory factory = buildDefaultValidatorFactory()) {
          validator = factory.getValidator();
      }
  }

  @Test
  void employeeIsEmpty() {
    EmployeeInfo employeeInfo = EmployeeInfo.builder().build();

    Set<ConstraintViolation<EmployeeInfo>> constraintViolations =
        validator.validate(employeeInfo);

    assertThat(constraintViolations).hasSize(5);
  }

  @Test
  void employeeFirstNameIsEmpty() {
    EmployeeInfo employeeInfo = new EmployeeInfo("", "J", "Luna", BASIC_DATE, BASIC_DATE, EmployeeStatus.ACTIVE);

    Set<ConstraintViolation<EmployeeInfo>> constraintViolations =
        validator.validate(employeeInfo);

    assertThat(constraintViolations)
            .as("it should have only one violation for invalid first name")
            .hasSize(1)
            .first()
            .extracting(ConstraintViolation::getMessage)
            .isEqualTo("First Name cannot be empty");
  }

  @Test
  void employeeLastNameIsEmpty() {
    EmployeeInfo employeeInfo =
        new EmployeeInfo("Marcos", "J", "", BASIC_DATE, BASIC_DATE, EmployeeStatus.ACTIVE);

    Set<ConstraintViolation<EmployeeInfo>> constraintViolations =
        validator.validate(employeeInfo);

    assertThat(constraintViolations)
            .as("it should have only one violation for invalid last name")
            .hasSize(1)
            .first()
            .extracting(ConstraintViolation::getMessage)
            .isEqualTo("Last Name cannot be empty");
  }

  @Test
  void employeeMiddleInitialIsEmpty() {
    EmployeeInfo employeeInfo =
        new EmployeeInfo("Marcos", "", "Luna", BASIC_DATE, BASIC_DATE, EmployeeStatus.ACTIVE);

    Set<ConstraintViolation<EmployeeInfo>> constraintViolations =
        validator.validate(employeeInfo);

    assertThat(constraintViolations)
            .as("it should have only one violation for invalid middle initial")
            .hasSize(1)
            .first()
            .extracting(ConstraintViolation::getMessage)
            .isEqualTo("Middle Initial cannot be empty");
  }

  @Test
  void employeeDateOfEmploymentIsNull() {
    EmployeeInfo employeeInfo = new EmployeeInfo("Gerardo", "J", "Luna", BASIC_DATE, null, EmployeeStatus.ACTIVE);

    Set<ConstraintViolation<EmployeeInfo>> constraintViolations =
        validator.validate(employeeInfo);

    assertThat(constraintViolations)
            .as("it should have only one violation for date of employment")
            .hasSize(1)
            .first()
            .extracting(ConstraintViolation::getMessage)
            .isEqualTo("must not be null");
  }

  @Test
  void employeeDateOfBirthIsNull() {
    EmployeeInfo employeeInfo = new EmployeeInfo("Gerardo", "J", "Luna", null, BASIC_DATE, EmployeeStatus.ACTIVE);

    Set<ConstraintViolation<EmployeeInfo>> constraintViolations =
        validator.validate(employeeInfo);

    assertThat(constraintViolations)
            .as("it should have only one violation for date of birth")
            .hasSize(1)
            .first()
            .extracting(ConstraintViolation::getMessage)
            .isEqualTo("must not be null");
  }

  @Test
  void employeeIsValid() {
    EmployeeInfo employeeInfo =
        new EmployeeInfo("Gerardo", "J", "Luna", BASIC_DATE, BASIC_DATE, EmployeeStatus.ACTIVE);

    Set<ConstraintViolation<EmployeeInfo>> constraintViolations =
        validator.validate(employeeInfo);

    assertThat(constraintViolations).isEmpty();
  }
}
