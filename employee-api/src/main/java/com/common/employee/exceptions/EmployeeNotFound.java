package com.common.employee.exceptions;

public class EmployeeNotFound extends RuntimeException {
  public EmployeeNotFound(String message) {
    super(message);
  }
}
