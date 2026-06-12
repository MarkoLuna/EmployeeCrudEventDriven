package com.employee.exceptions;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

/** Exception to be thrown when an error occurs while consuming the employee service. */
public class EmployeeServiceConsumerException extends ResponseStatusException {

  /**
   * Create a new instance.
   *
   * @param status the status
   * @param reason the reason
   */
  public EmployeeServiceConsumerException(HttpStatusCode status, String reason) {
    super(status, reason);
  }
}
