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
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

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

  private EmployeeDto getEmployeeFallback(String employeeId, Throwable t) {
    log.warn("Circuit breaker or Feign exception in getEmployee({}), propagating", employeeId, t);
    throw new EmployeeServiceConsumerException(
        HttpStatus.SERVICE_UNAVAILABLE,
        "Employee consumer is unavailable, please try again later.");
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
