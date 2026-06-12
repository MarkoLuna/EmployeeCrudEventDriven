package com.employee.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.employee.entities.ProcessedMessage;
import com.employee.repositories.ProcessedMessageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeduplicationServiceTest {

  private static final String DEDUP_KEY = "employee-upsert.v1:0:42";

  @Captor private ArgumentCaptor<ProcessedMessage> messageCaptor;

  @Mock private ProcessedMessageRepository processedMessageRepository;

  @InjectMocks private DeduplicationService deduplicationService;

  @DisplayName("Returns true when dedup key already exists")
  @Test
  void existsWhenPresent() {
    when(processedMessageRepository.existsById(DEDUP_KEY)).thenReturn(true);

    assertThat(deduplicationService.exists(DEDUP_KEY)).isTrue();
  }

  @DisplayName("Returns false when dedup key does not exist")
  @Test
  void existsWhenAbsent() {
    when(processedMessageRepository.existsById(DEDUP_KEY)).thenReturn(false);

    assertThat(deduplicationService.exists(DEDUP_KEY)).isFalse();
  }

  @DisplayName("Inserts a ProcessedMessage with the correct key and timestamp")
  @Test
  void markProcessed() {
    deduplicationService.markProcessed(DEDUP_KEY);

    verify(processedMessageRepository).insert(messageCaptor.capture());
    assertThat(messageCaptor.getValue())
        .isNotNull()
        .returns(DEDUP_KEY, ProcessedMessage::getDedupKey)
        .satisfies(msg -> assertThat(msg.getProcessedAt()).isNotNull());
  }
}
