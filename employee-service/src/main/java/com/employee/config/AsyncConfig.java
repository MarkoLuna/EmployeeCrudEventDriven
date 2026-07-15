package com.employee.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuration for asynchronous operations
 */
@Configuration
@EnableAsync
public class AsyncConfig {

  /**
   * Creates a task executor for asynchronous operations
   * @return Task executor with 5 core threads, 10 max threads, and 25 queue capacity
   */
  @Bean(name = "employeeProducerTaskExecutor")
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(25);
    executor.setThreadNamePrefix("EmployeeProducer-");
    executor.initialize();
    return executor;
  }
}
