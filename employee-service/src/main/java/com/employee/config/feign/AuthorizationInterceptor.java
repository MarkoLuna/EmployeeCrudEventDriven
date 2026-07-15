package com.employee.config.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.util.Optional;
import java.util.function.Predicate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Interceptor to add Authorization header to Feign requests
 */
@Component
public class AuthorizationInterceptor implements RequestInterceptor {

  /**
   * Applies the interceptor to the request template
   * 
   * @param template The request template
   */
  @Override
  public void apply(RequestTemplate template) {
    getAuth().ifPresent(auth -> template.header(HttpHeaders.AUTHORIZATION, auth));
  }

  /**
   * Gets the Authorization header from the current request
   * 
   * @return Optional containing the Authorization header
   */
  private Optional<String> getAuth() {
    ServletRequestAttributes requestAttributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    return Optional.ofNullable(requestAttributes)
        .map(ServletRequestAttributes::getRequest)
        .map(req -> req.getHeader(HttpHeaders.AUTHORIZATION))
        .filter(Predicate.not(String::isEmpty));
  }
}
