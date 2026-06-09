package com.users.exceptions;

import org.springframework.http.HttpStatus;

public class IamServiceException extends RuntimeException {

  private final HttpStatus statusCode;

  public IamServiceException(HttpStatus statusCode, String reason) {
    super(reason);
    this.statusCode = statusCode;
  }

  public HttpStatus getStatusCode() {
    return statusCode;
  }

  public String getReason() {
    return getMessage();
  }
}
