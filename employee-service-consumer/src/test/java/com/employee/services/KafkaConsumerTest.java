package com.employee.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.common.employee.dto.EmployeeInfo;
import com.common.employee.dto.EmployeeMessage;
import com.common.employee.enums.EmployeeOperationType;
import com.common.employee.enums.EmployeeStatus;
import com.employee.entities.DeadLetterMessage;
import com.employee.repositories.DeadLetterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerTest {

  private static final String UPSERT_TOPIC = "employee-upsert.v1";
  private static final String DELETION_TOPIC = "employee-deletion.v1";
  private static final int PARTITION = 0;
  private static final long OFFSET = 42;
  private static final String EMPLOYEE_ID = "e26b1ed4-a8d0-11e9-a2a3-2a2ae2dbcce4";
  private static final LocalDate BASIC_DATE = LocalDate.of(2012, 9, 17);

  private final ObjectMapper realObjectMapper =
      new ObjectMapper().registerModule(new JavaTimeModule()).registerModule(new Jdk8Module());

  private EmployeeInfo employeeInfo;

  @Mock private EmployeeService employeeService;

  @Mock private DeduplicationService deduplicationService;

  @Mock private DeadLetterRepository deadLetterRepository;

  private KafkaConsumer kafkaConsumer;

  @Captor private ArgumentCaptor<DeadLetterMessage> deadLetterCaptor;

  @BeforeEach
  void setUp() {
    employeeInfo = createEmployeeInfo();
    kafkaConsumer =
        new KafkaConsumer(
            employeeService, deduplicationService, deadLetterRepository, realObjectMapper);
  }

  @DisplayName("Upsert listener skips processing when dedup key already exists")
  @Test
  void upsert_dedupExists_skipsProcessing() {
    var message =
        EmployeeMessage.builder()
            .employeeId(EMPLOYEE_ID)
            .employeeInfo(employeeInfo)
            .operationType(EmployeeOperationType.CREATE)
            .build();
    when(deduplicationService.exists(anyString())).thenReturn(true);

    kafkaConsumer.listenEmployeeUpsert(message, null, UPSERT_TOPIC, PARTITION, OFFSET);

    verify(employeeService, never()).createEmployee(employeeInfo);
    verify(deduplicationService, never()).markProcessed(anyString());
  }

  @DisplayName("Upsert listener processes and marks dedup when key is absent")
  @Test
  void upsert_dedupAbsent_processesAndMarks() {
    var message =
        EmployeeMessage.builder()
            .employeeId(EMPLOYEE_ID)
            .employeeInfo(employeeInfo)
            .operationType(EmployeeOperationType.CREATE)
            .build();
    when(deduplicationService.exists(anyString())).thenReturn(false);

    kafkaConsumer.listenEmployeeUpsert(message, null, UPSERT_TOPIC, PARTITION, OFFSET);

    verify(employeeService).createEmployee(employeeInfo);
    verify(deduplicationService).markProcessed(anyString());
  }

  @DisplayName("Upsert listener does not mark processed when service throws")
  @Test
  void upsert_exception_doesNotMarkProcessed() {
    var message =
        EmployeeMessage.builder()
            .employeeId(EMPLOYEE_ID)
            .employeeInfo(employeeInfo)
            .operationType(EmployeeOperationType.CREATE)
            .build();
    when(deduplicationService.exists(anyString())).thenReturn(false);
    when(employeeService.createEmployee(employeeInfo)).thenThrow(new RuntimeException("kaboom"));

    assertThatThrownBy(
            () ->
                kafkaConsumer.listenEmployeeUpsert(message, null, UPSERT_TOPIC, PARTITION, OFFSET))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("kaboom");

    verify(deduplicationService, never()).markProcessed(anyString());
  }

  @DisplayName("Upsert listener with retry header skips when employee data matches")
  @Test
  void upsert_retryHeader_match_skipsProcessing() {
    var message =
        EmployeeMessage.builder()
            .employeeId(EMPLOYEE_ID)
            .employeeInfo(employeeInfo)
            .operationType(EmployeeOperationType.CREATE)
            .build();
    when(employeeService.employeeMatch(eq(EMPLOYEE_ID), eq(employeeInfo))).thenReturn(true);

    kafkaConsumer.listenEmployeeUpsert(message, 1, UPSERT_TOPIC, PARTITION, OFFSET);

    verify(deduplicationService, never()).exists(anyString());
    verify(employeeService, never()).createEmployee(employeeInfo);
    verify(deduplicationService, never()).markProcessed(anyString());
  }

  @DisplayName("Upsert listener with retry header proceeds to dedup when no match")
  @Test
  void upsert_retryHeader_noMatch_proceedsToDedup() {
    var message =
        EmployeeMessage.builder()
            .employeeId(EMPLOYEE_ID)
            .employeeInfo(employeeInfo)
            .operationType(EmployeeOperationType.CREATE)
            .build();
    when(employeeService.employeeMatch(eq(EMPLOYEE_ID), eq(employeeInfo))).thenReturn(false);
    when(deduplicationService.exists(anyString())).thenReturn(false);

    kafkaConsumer.listenEmployeeUpsert(message, 1, UPSERT_TOPIC, PARTITION, OFFSET);

    verify(employeeService).createEmployee(employeeInfo);
    verify(deduplicationService).markProcessed(anyString());
  }

  @DisplayName("Upsert listener with UPDATE operation processes and marks")
  @Test
  void upsert_update_operation_processesAndMarks() {
    var message =
        EmployeeMessage.builder()
            .employeeId(EMPLOYEE_ID)
            .employeeInfo(employeeInfo)
            .operationType(EmployeeOperationType.UPDATE)
            .build();
    when(deduplicationService.exists(anyString())).thenReturn(false);

    kafkaConsumer.listenEmployeeUpsert(message, null, UPSERT_TOPIC, PARTITION, OFFSET);

    verify(employeeService).updateEmployee(EMPLOYEE_ID, employeeInfo);
    verify(deduplicationService).markProcessed(anyString());
  }

  @DisplayName("Deletion listener skips processing when dedup key already exists")
  @Test
  void deletion_dedupExists_skipsProcessing() {
    var message =
        EmployeeMessage.builder()
            .employeeId(EMPLOYEE_ID)
            .operationType(EmployeeOperationType.DELETE)
            .build();
    when(deduplicationService.exists(anyString())).thenReturn(true);

    kafkaConsumer.listenEmployeeDeletion(message, null, DELETION_TOPIC, PARTITION, OFFSET);

    verify(employeeService, never()).deleteEmployee(EMPLOYEE_ID);
    verify(deduplicationService, never()).markProcessed(anyString());
  }

  @DisplayName("Deletion listener processes and marks dedup when key is absent")
  @Test
  void deletion_dedupAbsent_processesAndMarks() {
    var message =
        EmployeeMessage.builder()
            .employeeId(EMPLOYEE_ID)
            .operationType(EmployeeOperationType.DELETE)
            .build();
    when(deduplicationService.exists(anyString())).thenReturn(false);

    kafkaConsumer.listenEmployeeDeletion(message, null, DELETION_TOPIC, PARTITION, OFFSET);

    verify(employeeService).deleteEmployee(EMPLOYEE_ID);
    verify(deduplicationService).markProcessed(anyString());
  }

  @DisplayName("Deletion listener with retry header skips when employee is INACTIVE")
  @Test
  void deletion_retryHeader_match_skipsProcessing() {
    var message =
        EmployeeMessage.builder()
            .employeeId(EMPLOYEE_ID)
            .operationType(EmployeeOperationType.DELETE)
            .build();
    when(employeeService.employeeMatch(eq(EMPLOYEE_ID), eq(EmployeeStatus.INACTIVE)))
        .thenReturn(true);

    kafkaConsumer.listenEmployeeDeletion(message, 1, DELETION_TOPIC, PARTITION, OFFSET);

    verify(deduplicationService, never()).exists(anyString());
    verify(employeeService, never()).deleteEmployee(EMPLOYEE_ID);
    verify(deduplicationService, never()).markProcessed(anyString());
  }

  @DisplayName("Deletion listener with retry header proceeds to dedup when not INACTIVE")
  @Test
  void deletion_retryHeader_noMatch_proceedsToDedup() {
    var message =
        EmployeeMessage.builder()
            .employeeId(EMPLOYEE_ID)
            .operationType(EmployeeOperationType.DELETE)
            .build();
    when(employeeService.employeeMatch(eq(EMPLOYEE_ID), eq(EmployeeStatus.INACTIVE)))
        .thenReturn(false);
    when(deduplicationService.exists(anyString())).thenReturn(false);

    kafkaConsumer.listenEmployeeDeletion(message, 1, DELETION_TOPIC, PARTITION, OFFSET);

    verify(employeeService).deleteEmployee(EMPLOYEE_ID);
    verify(deduplicationService).markProcessed(anyString());
  }

  @DisplayName("Deletion listener does not mark processed when service throws")
  @Test
  void deletion_exception_doesNotMarkProcessed() {
    var message =
        EmployeeMessage.builder()
            .employeeId(EMPLOYEE_ID)
            .operationType(EmployeeOperationType.DELETE)
            .build();
    when(deduplicationService.exists(anyString())).thenReturn(false);
    doThrow(new RuntimeException("kaboom")).when(employeeService).deleteEmployee(EMPLOYEE_ID);

    assertThatThrownBy(
            () ->
                kafkaConsumer.listenEmployeeDeletion(
                    message, null, DELETION_TOPIC, PARTITION, OFFSET))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("kaboom");

    verify(deduplicationService, never()).markProcessed(anyString());
  }

  @DisplayName("DLT handler for upsert persists dead letter message")
  @Test
  void handleDltForEmployeeUpsert_shouldPersistDeadLetter() {
    var payload =
        EmployeeMessage.builder()
            .employeeId(EMPLOYEE_ID)
            .employeeInfo(employeeInfo)
            .operationType(EmployeeOperationType.CREATE)
            .build();
    Message<?> message = MessageBuilder.withPayload(payload).build();

    kafkaConsumer.handleDltForEmployeeUpsert(message);

    verify(deadLetterRepository).insert(deadLetterCaptor.capture());
    var deadLetter = deadLetterCaptor.getValue();
    assertThat(deadLetter.getOperationType()).isEqualTo("CREATE");
    assertThat(deadLetter.getEmployeeId()).isEqualTo(EMPLOYEE_ID);
    assertThat(deadLetter.getFailedAt()).isNotNull();
  }

  @DisplayName("DLT handler for deletion persists dead letter message")
  @Test
  void handleDltForEmployeeDeletion_shouldPersistDeadLetter() {
    var payload =
        EmployeeMessage.builder()
            .employeeId(EMPLOYEE_ID)
            .operationType(EmployeeOperationType.DELETE)
            .build();
    Message<?> message = MessageBuilder.withPayload(payload).build();

    kafkaConsumer.handleDltForEmployeeDeletion(message);

    verify(deadLetterRepository).insert(deadLetterCaptor.capture());
    var deadLetter = deadLetterCaptor.getValue();
    assertThat(deadLetter.getOperationType()).isEqualTo("DELETE");
    assertThat(deadLetter.getEmployeeId()).isEqualTo(EMPLOYEE_ID);
    assertThat(deadLetter.getFailedAt()).isNotNull();
  }

  private EmployeeInfo createEmployeeInfo() {
    return EmployeeInfo.builder()
        .firstName("Gerardo")
        .middleInitial("J")
        .lastName("Luna")
        .dateOfBirth(BASIC_DATE)
        .dateOfEmployment(BASIC_DATE)
        .status(EmployeeStatus.ACTIVE)
        .build();
  }
}
