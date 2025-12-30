package com.employee.services;

import com.common.employee.dto.*;
import com.common.employee.enums.EmployeeOperationType;
import com.common.employee.exceptions.EmployeeNotFound;
import com.employee.clients.EmployeeClient;
import com.employee.mappers.EmployeeMapper;
import java.util.Optional;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
  private final Executor customTaskExecutor;

  public EmployeePage list(Integer page, Integer sizePage) {
    return employeeClient.listEmployees(page, sizePage);
  }

  public EmployeeDto getEmployee(String employeeId) throws EmployeeNotFound {
    Optional<EmployeeDto> employee = employeeClient.getEmployee(employeeId);
    return employee.orElseThrow(() -> new EmployeeNotFound("Unable to find the Employee"));
  }

  public Optional<EmployeeDto> createEmployee(EmployeeRequest req) {
    EmployeeDto employee = employeeMapper.convert(req);
    var employeeMessage =
        EmployeeMessage.builder()
            .employee(employee)
            .operationType(EmployeeOperationType.CREATE)
            .build();
    Message<EmployeeMessage> message = MessageBuilder.withPayload(employeeMessage).build();
    employeeUpsertKafkaTemplate
        .send(message)
            .whenComplete((result, ex) -> {
              if (ex == null) {
                log.debug("employee created successfully. [topic: {}], [partition: {}], [offset: {}], [value: {}]",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        result.getProducerRecord().value());
              } else {
                log.warn("Failed to send employee record to kafka", ex);
              }
            });
    return Optional.of(employee);
  }

  public EmployeeDto updateEmployee(String employeeId, EmployeeDto empl) {
    var employeeMessage =
        EmployeeMessage.builder()
            .employee(empl)
            .operationType(EmployeeOperationType.UPDATE)
            .build();
    Message<EmployeeMessage> message = MessageBuilder.withPayload(employeeMessage).build();
    employeeUpsertKafkaTemplate.send(message);
    return empl;
  }

  public void deleteEmployee(String id) {
    var employeeMessage =
        EmployeeMessage.builder()
            .employee(EmployeeDto.builder().id(id).build())
            .operationType(EmployeeOperationType.DELETE)
            .build();
    Message<EmployeeMessage> message = MessageBuilder.withPayload(employeeMessage).build();
    employeeDeletionKafkaTemplate.send(message);
  }
}
