package com.employee.entities;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Processed Message class.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "processed_messages")
public class ProcessedMessage {

  @Id private String dedupKey;

  @Indexed(expireAfterSeconds = 86400)
  private LocalDateTime processedAt;
}
