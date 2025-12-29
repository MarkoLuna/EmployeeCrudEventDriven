package com.employee.config.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.util.Optional;
import java.util.function.Predicate;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class AuthorizationInterceptor implements RequestInterceptor {

  @Override
  public void apply(RequestTemplate template) {
    getAuth().ifPresent(auth -> template.header(HttpHeaders.AUTHORIZATION, auth));
  }

  private Optional<String> getAuth() {
    ServletRequestAttributes requestAttributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    return Optional.ofNullable(requestAttributes)
        .map(ServletRequestAttributes::getRequest)
        .map(req -> req.getHeader(HttpHeaders.AUTHORIZATION))
        .filter(Predicate.not(String::isEmpty));
  }
}
