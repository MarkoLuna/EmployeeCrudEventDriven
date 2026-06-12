package com.common.employee.exceptions;

/** Exception to be thrown when an employee is not found. */
public class EmployeeNotFound extends RuntimeException {

  /**
   * Create a new instance.
   *
   * @param message the message
   */
  public EmployeeNotFound(String message) {
    super(message);
  }
}
