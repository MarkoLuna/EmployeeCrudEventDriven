package com.users.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.users.clients.UserClient;
import com.users.config.feign.AuthorizationInterceptor;
import com.users.config.feign.LoggingInterceptor;
import com.users.config.feign.MessageErrorDecoder;
import com.users.config.feign.MethodAwareRetryer;
import feign.Feign;
import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientsConfig {

  @Bean
  public ObjectMapper feignObjectMapper() {
    return new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .registerModule(new Jdk8Module())
        .configure(SerializationFeature.INDENT_OUTPUT, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  @Bean
  public ErrorDecoder errorDecoder() {
    return new MessageErrorDecoder();
  }

  @Bean
  public Retryer retryer() {
    return new MethodAwareRetryer(100L, TimeUnit.SECONDS.toMillis(1L), 3);
  }

  @Bean
  public UserClient userClient(
      @Value("${services.iam-service.base-url}") String iamServiceBaseUrl,
      ObjectMapper feignObjectMapper,
      ErrorDecoder errorDecoder,
      Retryer retryer) {
    return Feign.builder()
        .encoder(new JacksonEncoder(feignObjectMapper))
        .decoder(new JacksonDecoder(feignObjectMapper))
        .errorDecoder(errorDecoder)
        .requestInterceptor(new AuthorizationInterceptor())
        .logger(new LoggingInterceptor())
        .options(new Request.Options(5, TimeUnit.SECONDS, 10, TimeUnit.SECONDS, true))
        .retryer(retryer)
        .logLevel(Logger.Level.FULL)
        .target(UserClient.class, iamServiceBaseUrl);
  }
}
