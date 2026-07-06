package com.employee.entities;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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

  private LocalDateTime failedAt;
}
