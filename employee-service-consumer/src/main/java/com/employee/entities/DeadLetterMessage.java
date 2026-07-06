package com.employee.entities;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a Kafka message that failed all retry attempts and was routed to the Dead Letter Topic
 * (DLT). Each entry captures the original message metadata, the error that caused the failure, and
 * the timestamp of failure. Persisted to MongoDB for manual inspection and replay.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "dead_letter_queue")
public class DeadLetterMessage {

  @Id private String id;

  private String topic;

  private int partition;

  private long offset;

  private String employeeId;

  private String operationType;

  private String payload;

  private String errorMessage;

  private String stackTrace;

  private Instant failedAt;
}
