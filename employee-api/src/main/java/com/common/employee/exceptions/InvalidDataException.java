package com.common.employee.exceptions;

/** Exception to be thrown when an invalid data is encountered. */
public class InvalidDataException extends RuntimeException {
  /**
   * Create a new instance.
   *
   * @param message the message
   */
  public InvalidDataException(String message) {
    super(message);
  }
}
