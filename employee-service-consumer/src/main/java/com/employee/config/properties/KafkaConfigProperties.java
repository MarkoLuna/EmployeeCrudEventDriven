package com.employee.config.properties;

import java.time.Duration;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "kafka.consumer")
public class KafkaConfigProperties {

  private boolean enabled;
  private String groupId;
  private String bootstrapServers;
  private Integer concurrency;
  private String employeeUpsertTopic;
  private String employeeDeletionTopic;
  private EventNonBlockingRetry eventNonBlockingRetry;

  /** Non-blocking retry properties */
  @Getter
  @Setter
  @ToString
  public static class EventNonBlockingRetry {

    private int maxAttempts;

    /** The time to wait before trying to process the message again. */
    private Duration period;

    /** The maximum amount of time a message can be kept in the retry topic. */
    private Duration maxPeriod;
  }
}
