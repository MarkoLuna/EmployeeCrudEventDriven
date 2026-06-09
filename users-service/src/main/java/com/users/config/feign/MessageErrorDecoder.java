package com.users.config.feign;

import com.users.config.ApplicationExceptionHandler;
import com.users.exceptions.IamServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;

public class MessageErrorDecoder implements ErrorDecoder {

  private final ErrorDecoder errorDecoder = new Default();

  @Override
  public Exception decode(String methodKey, Response response) {
    String error =
        Optional.ofNullable(
                response.headers().get(ApplicationExceptionHandler.RESPONSE_ENTITY_ERROR_HEADER))
            .filter(Predicate.not(Collection::isEmpty))
            .flatMap(errors -> Optional.of(String.join(",", errors)))
            .or(() -> getBodyAsString(response))
            .orElse("Error while calling IAM service, please try again later.");

    var status = HttpStatus.valueOf(response.status());
    if (status.is4xxClientError() || status.is5xxServerError()) {
      return new IamServiceException(status, error);
    } else {
      return errorDecoder.decode(methodKey, response);
    }
  }

  @SneakyThrows
  private Optional<String> getBodyAsString(Response response) {
    return Optional.ofNullable(response.body())
        .flatMap(
            body -> {
              try {
                return Optional.ofNullable(body.asInputStream());
              } catch (IOException e) {
                return Optional.empty();
              }
            })
        .map(
            stream -> {
              try {
                return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
              } catch (IOException e) {
                return "";
              }
            })
        .filter(Predicate.not(String::isEmpty));
  }
}
