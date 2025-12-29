package com.employee.config;

import com.employee.clients.EmployeeClient;
import com.employee.config.feign.AuthorizationInterceptor;
import com.employee.config.feign.MessageErrorDecoder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Feign;
import feign.Logger;
import feign.codec.ErrorDecoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
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
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .configure(SerializationFeature.INDENT_OUTPUT, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  @Bean
  public ErrorDecoder errorDecoder() {
    return new MessageErrorDecoder();
  }

  @Bean
  public EmployeeClient employeeClient(
      @Value("${services.employee-service-consumer.base-url}")
          String employeeServiceConsumerBaseUrl,
      ObjectMapper feignObjectMapper,
      ErrorDecoder errorDecoder) {
    return Feign.builder()
        .encoder(new JacksonEncoder(feignObjectMapper))
        .decoder(new JacksonDecoder(feignObjectMapper))
        .errorDecoder(errorDecoder)
        .requestInterceptor(new AuthorizationInterceptor())
        .logLevel(Logger.Level.FULL)
        .target(EmployeeClient.class, employeeServiceConsumerBaseUrl);
  }
}
