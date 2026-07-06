package com.employee.services;

import com.common.employee.dto.EmployeeDto;
import com.common.employee.dto.EmployeeInfo;
import com.common.employee.dto.EmployeeMessage;
import com.common.employee.dto.EmployeePage;
import com.common.employee.enums.EmployeeOperationType;
import com.common.employee.exceptions.EmployeeNotFound;
import com.employee.clients.EmployeeClient;
import com.employee.exceptions.EmployeeServiceConsumerException;
import com.employee.mappers.EmployeeMapper;
import feign.FeignException;
import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

/**
 * Service for employee read/write operations. Write operations (create, update, delete) publish
 * events to Kafka asynchronously. Read operations (list, get) call the consumer synchronously via
 * Feign, protected by a Resilience4j {@link CircuitBreaker} named "employeeConsumer". When the
 * circuit is open, fallback methods return empty pages or 503 respectively.
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class EmployeeService {

  private final KafkaTemplate<String, EmployeeMessage> employeeDeletionKafkaTemplate;
  private final KafkaTemplate<String, EmployeeMessage> employeeUpsertKafkaTemplate;
  private final EmployeeClient employeeClient;
  private final EmployeeMapper employeeMapper;

  /**
   * List all employees.
   *
   * @param page the page number
   * @param sizePage the page size
   * @return the list of employees
   */
  @CircuitBreaker(name = "employeeConsumer", fallbackMethod = "listFallback")
  public EmployeePage list(Integer page, Integer sizePage) {
    return employeeClient.listEmployees(page, sizePage);
  }

  /**
   * Fallback for {@link #list(Integer, Integer)}. Triggered when the circuit breaker is open or a
   * Feign exception occurs. Returns an empty employee page.
   *
   * @param page the requested page number
   * @param sizePage the requested page size
   * @param t the exception that triggered the fallback
   * @return an empty {@link EmployeePage}
   */
  private EmployeePage listFallback(Integer page, Integer sizePage, Throwable t) {
    log.warn("Circuit breaker or Feign exception in list(), returning empty page", t);
    return EmployeePage.builder()
        .pageNumber(page)
        .pageSize(sizePage)
        .content(java.util.List.of())
        .build();
  }

  /**
   * Get an employee by id.
   *
   * @param employeeId the employee id
   * @return the employee
   */
  @CircuitBreaker(name = "employeeConsumer", fallbackMethod = "getEmployeeFallback")
  public EmployeeDto getEmployee(String employeeId) throws EmployeeNotFound {
    return employeeClient
        .getEmployee(employeeId)
        .orElseThrow(() -> new EmployeeNotFound("Unable to find the Employee"));
  }

  /**
   * Fallback for {@link #getEmployee(String)}. Distinguishes between circuit-breaker or
   * network-level exceptions (returns 503) and business exceptions (rethrows original).
   *
   * @param employeeId the requested employee id
   * @param t the exception that triggered the fallback
   * @return never returns normally; always throws
   * @throws EmployeeServiceConsumerException when the circuit is open or the consumer is
   *     unreachable (HTTP 503)
   * @throws EmployeeNotFound when the consumer replied with 404 (feign.FeignException.NotFound)
   * @throws RuntimeException the original exception for all other cases
   */
  private EmployeeDto getEmployeeFallback(String employeeId, Throwable t) {
    if (t instanceof CallNotPermittedException || t instanceof RetryableException) {
      log.warn("Consumer unavailable in getEmployee({}), returning 503", employeeId, t);
      throw new EmployeeServiceConsumerException(
          HttpStatus.SERVICE_UNAVAILABLE,
          "Employee consumer is unavailable, please try again later.");
    }
    if (t instanceof FeignException.NotFound) {
      throw new EmployeeNotFound("Employee not found: " + employeeId);
    }
    log.warn("Error in getEmployee({}), rethrowing", employeeId, t);
    if (t instanceof RuntimeException) {
      throw (RuntimeException) t;
    }
    throw new RuntimeException(t);
  }

  /**
   * Create an employee.
   *
   * @param req the employee info
   * @return the employee
   */
  public Optional<EmployeeDto> createEmployee(EmployeeInfo req) {
    var employeeMessage =
        EmployeeMessage.builder()
            .employeeInfo(req)
            .operationType(EmployeeOperationType.CREATE)
            .build();
    Message<EmployeeMessage> message = MessageBuilder.withPayload(employeeMessage).build();

    employeeUpsertKafkaTemplate
        .send(message)
        .whenCompleteAsync(
            (result, ex) -> {
              if (ex == null) {
                log.debug(
                    "employee created successfully. [topic: {}], [partition: {}], [offset: {}], [value: {}]",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    result.getProducerRecord().value());
              } else {
                log.error("Failed to send employee record to kafka", ex);
              }
            });
    return Optional.ofNullable(employeeMapper.convert(req));
  }

  /**
   * Update an employee.
   *
   * @param employeeId the employee id
   * @param empl the employee
   * @return the employee
   */
  public EmployeeDto updateEmployee(String employeeId, EmployeeDto empl) {
    var employeeMessage =
        EmployeeMessage.builder()
            .employeeId(employeeId)
            .employeeInfo(employeeMapper.convert(empl))
            .operationType(EmployeeOperationType.UPDATE)
            .build();
    Message<EmployeeMessage> message = MessageBuilder.withPayload(employeeMessage).build();

    employeeUpsertKafkaTemplate
        .send(message)
        .whenCompleteAsync(
            (result, ex) -> {
              if (ex == null) {
                log.debug(
                    "employee updated successfully. [topic: {}], [partition: {}], [offset: {}], [value: {}]",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    result.getProducerRecord().value());
              } else {
                log.error("Failed to send employee record to kafka", ex);
              }
            });
    return empl;
  }

  /**
   * Delete an employee.
   *
   * @param id the employee id
   */
  public void deleteEmployee(String id) {
    var employeeMessage =
        EmployeeMessage.builder()
            .employeeId(id)
            .operationType(EmployeeOperationType.DELETE)
            .build();
    Message<EmployeeMessage> message = MessageBuilder.withPayload(employeeMessage).build();

    employeeDeletionKafkaTemplate
        .send(message)
        .whenCompleteAsync(
            (result, ex) -> {
              if (ex == null) {
                log.debug(
                    "employee deleted successfully. [topic: {}], [partition: {}], [offset: {}], [value: {}]",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    result.getProducerRecord().value());
              } else {
                log.error("Failed to send employee record to kafka", ex);
              }
            });
  }
}
