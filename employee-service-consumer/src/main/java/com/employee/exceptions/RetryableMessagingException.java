package com.employee.exceptions;

import lombok.Getter;
import org.apache.kafka.common.errors.RetriableException;

@Getter
public class RetryableMessagingException extends RetriableException {

  public RetryableMessagingException(String message, Throwable cause) {
    super(message, cause);
  }

  public RetryableMessagingException(String message) {
    super(message);
  }

  public RetryableMessagingException(Throwable cause) {
    super(cause);
  }

  public RetryableMessagingException() {}

  /**
   * Creates an RetryableMessagingException with the given .
   *
   * @param e the exception
   * @return a RetryableMessagingException with the given event exception as cause.
   */
  public static RetryableMessagingException fromException(Exception e) {
    return new RetryableMessagingException(e.getMessage(), e);
  }
}
