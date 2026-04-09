package com.employee.config.feign;

import com.employee.config.ApplicationExceptionHandler;
import com.employee.exceptions.EmployeeServiceConsumerException;
import feign.Response;
import feign.codec.ErrorDecoder;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import org.springframework.http.HttpStatus;

public class MessageErrorDecoder implements ErrorDecoder {

  private final ErrorDecoder errorDecoder = new Default();

  @Override
  public Exception decode(String methodKey, Response response) {
    String error = Optional.ofNullable(response.headers().get(ApplicationExceptionHandler.RESPONSE_ENTITY_ERROR_HEADER))
            .filter(Predicate.not(Collection::isEmpty))
            .flatMap(errors -> Optional.of(String.join(",", errors)))
            .orElse("Error while calling kafka producer, please try again later.");

    var status = HttpStatus.valueOf(response.status()); 
    if (status.is4xxClientError() || status.is5xxServerError()) {
      return new EmployeeServiceConsumerException(status, error);
    } else {
      return errorDecoder.decode(methodKey, response);
    }
  }
}
