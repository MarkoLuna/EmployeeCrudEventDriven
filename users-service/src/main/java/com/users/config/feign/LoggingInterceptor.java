package com.users.config.feign;

import feign.Request;
import feign.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class LoggingInterceptor extends feign.Logger {

  private static final int MAX_BODY_LOG_LENGTH = 2000;

  @Override
  protected void log(String configKey, String format, Object... args) {
    log.info(String.format(format, args));
  }

  @Override
  protected void logRequest(String configKey, Level logLevel, Request request) {
    log.info(
        "Feign Request: {} {} | headers: {} | body: {}",
        request.httpMethod(),
        request.url(),
        sanitizeHeaders(request.headers()),
        sanitizeBody(request.body()));
  }

  @Override
  protected Response logAndRebufferResponse(
      String configKey, Level logLevel, Response response, long elapsedTime) throws IOException {
    byte[] bodyBytes = readBodyBytes(response);

    log.info(
        "Feign Response: {} {} | status: {} | headers: {} | body: {} | time: {}ms",
        response.request().httpMethod(),
        response.request().url(),
        response.status(),
        response.headers(),
        new String(bodyBytes, StandardCharsets.UTF_8),
        elapsedTime / 1_000_000f);

    return response.toBuilder().body(bodyBytes).build();
  }

  private Map<String, Collection<String>> sanitizeHeaders(Map<String, Collection<String>> headers) {
    var sanitized = new LinkedHashMap<>(headers);
    sanitized.keySet().removeIf(k -> k.equalsIgnoreCase("authorization"));
    return sanitized;
  }

  private String sanitizeBody(byte[] body) {
    if (body == null || body.length == 0) {
      return "<empty>";
    }
    var str = new String(body, StandardCharsets.UTF_8);
    if (str.length() > MAX_BODY_LOG_LENGTH) {
      return str.substring(0, MAX_BODY_LOG_LENGTH) + "... (truncated)";
    }
    return str;
  }

  private byte[] readBodyBytes(Response response) {
    if (response.body() == null) {
      return new byte[0];
    }
    try (var is = response.body().asInputStream()) {
      return is.readAllBytes();
    } catch (IOException e) {
      log.warn("Failed to read response body bytes", e);
      return new byte[0];
    }
  }
}
