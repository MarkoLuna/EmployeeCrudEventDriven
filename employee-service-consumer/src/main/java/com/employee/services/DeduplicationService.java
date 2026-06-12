package com.employee.services;

import com.employee.entities.ProcessedMessage;
import com.employee.repositories.ProcessedMessageRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeduplicationService {

  private final ProcessedMessageRepository processedMessageRepository;

  public boolean exists(String dedupKey) {
    return processedMessageRepository.existsById(dedupKey);
  }

  public void markProcessed(String dedupKey) {
    processedMessageRepository.insert(
        ProcessedMessage.builder().dedupKey(dedupKey).processedAt(LocalDateTime.now()).build());
  }
}
