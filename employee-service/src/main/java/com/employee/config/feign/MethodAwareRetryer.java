package com.employee.config.feign;

import feign.Request;
import feign.RetryableException;
import feign.Retryer;
import java.util.concurrent.TimeUnit;

/**
 * Retryer that only retries GET requests
 */
public class MethodAwareRetryer implements Retryer {

  private final Retryer.Default delegate;
  private final int maxAttempts;

  /**
   * Default constructor with 100ms period, 1ms max period, and 3 attempts
   */
  public MethodAwareRetryer() {
    this(100, TimeUnit.MILLISECONDS.toMillis(1), 3);
  }

  public MethodAwareRetryer(long period, long maxPeriod, int maxAttempts) {
    this.delegate = new Retryer.Default(period, maxPeriod, maxAttempts);
    this.maxAttempts = maxAttempts;
  }

  @Override
  public void continueOrPropagate(RetryableException e) {
    if (e.method() == Request.HttpMethod.GET) {
      delegate.continueOrPropagate(e);
    } else {
      throw e;
    }
  }

  @Override
  public Retryer clone() {
    return new MethodAwareRetryer();
  }
}
