package com.employee.exceptions;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public class EmployeeServiceConsumerException extends ResponseStatusException {

  public EmployeeServiceConsumerException(HttpStatusCode status, String reason) {
    super(status, reason);
  }
}
