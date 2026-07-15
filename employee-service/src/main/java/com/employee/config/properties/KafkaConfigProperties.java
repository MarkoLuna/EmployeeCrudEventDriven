package com.employee.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Kafka producer
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "kafka.producer")
public class KafkaConfigProperties {
  /** Bootstrap servers */
  private String bootstrapServers;
  /** Employee upsert topic */
  private String employeeUpsertTopic;
  /** Employee deletion topic */
  private String employeeDeletionTopic;
}
