package com.employee.services;

import com.common.employee.dto.*;
import com.common.employee.enums.EmployeeOperationType;
import com.common.employee.exceptions.EmployeeNotFound;
import com.employee.clients.EmployeeClient;
import com.employee.mappers.EmployeeMapper;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmployeeService {

  @Autowired private KafkaTemplate<String, EmployeeMessage> employeeDeletionKafkaTemplate;

  @Autowired private KafkaTemplate<String, EmployeeMessage> employeeUpsertKafkaTemplate;

  @Autowired private EmployeeClient employeeClient;

  @Autowired private EmployeeMapper employeeMapper;

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
    employeeUpsertKafkaTemplate.send(message);
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
