package com.employee.config;

import com.employee.clients.EmployeeClient;
import com.employee.config.feign.AuthorizationInterceptor;
import com.employee.config.feign.MessageErrorDecoder;
import com.employee.config.feign.MethodAwareRetryer;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

/**
 * Configuration for Feign clients
 */
@Configuration
public class FeignClientsConfig {

  /**
   * Creates an ObjectMapper for Feign
   * @return ObjectMapper configured for Feign
   */
  @Bean
  public ObjectMapper feignObjectMapper() {
    return new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .registerModule(new Jdk8Module())
        .configure(SerializationFeature.INDENT_OUTPUT, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  /**
   * Creates an error decoder for Feign
   * @return ErrorDecoder for Feign
   */
  @Bean
  public ErrorDecoder errorDecoder() {
    return new MessageErrorDecoder();
  }

  /**
   * Creates a retryer for Feign
   * @return Retryer for Feign
   */
  @Bean
  public Retryer retryer() {
    return new MethodAwareRetryer(100L, TimeUnit.SECONDS.toMillis(1L), 3);
  }

  /**
   * Creates the Employee client
   * @param employeeServiceConsumerBaseUrl Base URL for employee service
   * @param feignObjectMapper ObjectMapper for Feign
   * @param errorDecoder ErrorDecoder for Feign
   * @param retryer Retryer for Feign
   * @return Employee client
   */
  @Bean
  public EmployeeClient employeeClient(
      @Value("${services.employee-service-consumer.base-url}")
          String employeeServiceConsumerBaseUrl,
      ObjectMapper feignObjectMapper,
      ErrorDecoder errorDecoder,
      Retryer retryer) {
    return Feign.builder()
        .encoder(new JacksonEncoder(feignObjectMapper))
        .decoder(new JacksonDecoder(feignObjectMapper))
        .errorDecoder(errorDecoder)
        .requestInterceptor(new AuthorizationInterceptor())
        .options(new Request.Options(5, TimeUnit.SECONDS, 10, TimeUnit.SECONDS, true))
        .retryer(retryer)
        .logLevel(Logger.Level.FULL)
        .target(EmployeeClient.class, employeeServiceConsumerBaseUrl);
  }
}
