package com.employee.services;

import com.employee.entities.ProcessedMessage;
import com.employee.repositories.ProcessedMessageRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service for deduplication
 */
@Service
@RequiredArgsConstructor
public class DeduplicationService {

  private final ProcessedMessageRepository processedMessageRepository;

  /**
   * Checks if a message has been processed
   * 
   * @param dedupKey Deduplication key
   * @return True if the message has been processed, false otherwise
   */
  public boolean exists(String dedupKey) {
    return processedMessageRepository.existsById(dedupKey);
  }

  /**
   * Marks a message as processed
   * 
   * @param dedupKey Deduplication key
   */
  public void markProcessed(String dedupKey) {
    processedMessageRepository.insert(
        ProcessedMessage.builder().dedupKey(dedupKey).processedAt(LocalDateTime.now()).build());
  }
}
