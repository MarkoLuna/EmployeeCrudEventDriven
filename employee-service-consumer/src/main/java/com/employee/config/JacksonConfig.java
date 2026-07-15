package com.employee.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Jackson ObjectMapper
 */
@Configuration
public class JacksonConfig {

  /**
   * Creates an ObjectMapper with Java 8 and Java 10+ modules
   * 
   * @return ObjectMapper configured with Java 8 and Java 10+ modules
   */
  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();

    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.registerModule(new Jdk8Module());
    return objectMapper;
  }
}
