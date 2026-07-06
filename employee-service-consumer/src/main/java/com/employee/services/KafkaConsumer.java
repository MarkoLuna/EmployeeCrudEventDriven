package com.employee.services;

import static java.util.Objects.isNull;

import com.common.employee.dto.EmployeeMessage;
import com.common.employee.enums.EmployeeStatus;
import com.common.employee.exceptions.EmployeeNotFound;
import com.employee.entities.DeadLetterMessage;
import com.employee.exceptions.RetryableMessagingException;
import com.employee.repositories.DeadLetterRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.retrytopic.RetryTopicHeaders;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class KafkaConsumer {

  private final EmployeeService employeeService;

  private final DeduplicationService deduplicationService;

  private final DeadLetterRepository deadLetterRepository;

  private final ObjectMapper objectMapper;

  @KafkaListener(
      topics = "${kafka.consumer.employee-upsert-topic}",
      autoStartup = "${kafka.consumer.enabled:false}",
      containerFactory = "employeeKafkaListenerContainerFactory")
  public void listenEmployeeUpsert(
      @Payload EmployeeMessage message,
      @Header(name = RetryTopicHeaders.DEFAULT_HEADER_ATTEMPTS, required = false)
          Integer nonBlockingAttempts,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
      @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
      @Header(KafkaHeaders.OFFSET) long offset) {
    var entry = log.traceEntry("listenEmployeeUpsertMessage: {}", message);
    try {
      if (recordAlreadyProcessed(nonBlockingAttempts, message)) {
        return;
      }
      String dedupKey = topic + ":" + partition + ":" + offset;
      if (deduplicationService.exists(dedupKey)) {
        log.info("Record already processed, skipping. dedupKey: {}", dedupKey);
        return;
      }
      switch (message.operationType()) {
        case CREATE -> employeeService.createEmployee(message.employeeInfo());
        case UPDATE -> employeeService.updateEmployee(message.employeeId(), message.employeeInfo());
        default ->
            log.warn("unable to process record due to the operation type is invalid {}", message);
      }
      deduplicationService.markProcessed(dedupKey);
      log.traceExit(entry);
    } catch (EmployeeNotFound ex) {
      log.error("Unable to process employee update message", ex);
      throw RetryableMessagingException.fromException(ex);
    } catch (Exception exception) {
      log.error("Unable to process employee upsert message", exception);
      throw exception;
    }
  }

  @KafkaListener(
      topics = "${kafka.consumer.employee-deletion-topic}",
      autoStartup = "${kafka.consumer.enabled:false}",
      containerFactory = "employeeKafkaListenerContainerFactory")
  public void listenEmployeeDeletion(
      @Payload EmployeeMessage message,
      @Header(name = RetryTopicHeaders.DEFAULT_HEADER_ATTEMPTS, required = false)
          Integer nonBlockingAttempts,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
      @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
      @Header(KafkaHeaders.OFFSET) long offset) {
    var entry = log.traceEntry("listenEmployeeDeletionMessage: {}", message);
    try {
      int deliveryAttempt = isNull(nonBlockingAttempts) ? 0 : nonBlockingAttempts;
      if (deliveryAttempt > 0) {
        log.info(
            "Getting the ms365 event again. delivery attempt: [{}] the employeeId: [{}]",
            () -> deliveryAttempt,
            message::employeeId);
        if (employeeService.employeeMatch(message.employeeId(), EmployeeStatus.INACTIVE)) {
          log.info(
              "Record Already found, wont process.. delivery attempt: [{}] the employeeId: [{}]",
              () -> deliveryAttempt,
              message::employeeId);
          return;
        }
      }
      String dedupKey = topic + ":" + partition + ":" + offset;
      if (deduplicationService.exists(dedupKey)) {
        log.info("Record already processed, skipping. dedupKey: {}", dedupKey);
        return;
      }
      employeeService.deleteEmployee(message.employeeId());
      deduplicationService.markProcessed(dedupKey);
      log.traceExit(entry);
    } catch (EmployeeNotFound ex) {
      log.error("Unable to process employee deletion message", ex);
      throw RetryableMessagingException.fromException(ex);
    } catch (Exception exception) {
      log.error("Unable to process employee deletion message", exception);
      throw exception;
    }
  }

  private boolean recordAlreadyProcessed(Integer nonBlockingAttempts, EmployeeMessage message) {
    int deliveryAttempt = isNull(nonBlockingAttempts) ? 0 : nonBlockingAttempts;
    if (deliveryAttempt > 0) {
      // If it comes from retries then we verify first if the record exists
      log.info(
          "Getting the ms365 event again. delivery attempt: [{}] the employeeId: [{}]",
          () -> deliveryAttempt,
          message::employeeId);
      if (employeeService.employeeMatch(message.employeeId(), message.employeeInfo())) {
        log.info(
            "Record Already found, wont process.. delivery attempt: [{}] the employeeId: [{}]",
            () -> deliveryAttempt,
            message::employeeId);
        return true;
      }
    }
    return false;
  }

  /**
   * Handles DLT messages for employee deletion.
   *
   * @param errorMessage the error message to handle.
   */
  public void handleDltForEmployeeDeletion(Message<?> errorMessage) {
    log.warn("Employee Deletion kafka DLT listener reached, max retrys: {}", errorMessage);
    persistDeadLetter(errorMessage);
  }

  /**
   * Handles DLT messages for employee upsert.
   *
   * @param errorMessage the error message to handle.
   */
  public void handleDltForEmployeeUpsert(Message<?> errorMessage) {
    log.warn("Employee Upsert kafka DLT listener reached, max retrys: {}", errorMessage);
    persistDeadLetter(errorMessage);
  }

  private void persistDeadLetter(Message<?> errorMessage) {
    try {
      var headers = errorMessage.getHeaders();
      var payload = extractPayload(errorMessage);
      var exception = headers.get(KafkaHeaders.DLT_EXCEPTION_MESSAGE, String.class);
      var originalTopic = headers.get(KafkaHeaders.DLT_ORIGINAL_TOPIC, String.class);
      var partition = headers.get(KafkaHeaders.RECEIVED_PARTITION, Integer.class);
      var offset = headers.get(KafkaHeaders.OFFSET, Long.class);

      DeadLetterMessage deadLetter =
          DeadLetterMessage.builder()
              .topic(Objects.toString(originalTopic, "unknown"))
              .partition(partition != null ? partition : 0)
              .offset(offset != null ? offset : 0)
              .employeeId(extractEmployeeId(payload))
              .operationType(extractOperationType(payload))
              .payload(payload)
              .errorMessage(Objects.toString(exception, "unknown"))
              .failedAt(LocalDateTime.now())
              .build();

      deadLetterRepository.insert(deadLetter);
      log.info("Persisted dead letter message to MongoDB: {}", deadLetter.getId());
    } catch (Exception e) {
      log.error("Failed to persist dead letter message to MongoDB", e);
    }
  }

  private String extractPayload(Message<?> errorMessage) {
    var payloadObj = errorMessage.getPayload();
    try {
      return objectMapper.writeValueAsString(payloadObj);
    } catch (JsonProcessingException e) {
      return Objects.toString(payloadObj, "unknown");
    }
  }

  private String extractEmployeeId(String payload) {
    try {
      var node = objectMapper.readTree(payload);
      var id = node.path("employeeId").asText(null);
      return id != null ? id : "unknown";
    } catch (Exception e) {
      return "unknown";
    }
  }

  private String extractOperationType(String payload) {
    try {
      var node = objectMapper.readTree(payload);
      var op = node.path("operationType").asText(null);
      return op != null ? op : "unknown";
    } catch (Exception e) {
      return "unknown";
    }
  }
}
