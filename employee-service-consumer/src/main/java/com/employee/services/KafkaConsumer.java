package com.employee.services;

import com.common.employee.dto.EmployeeMessage;
import com.common.employee.enums.EmployeeOperationType;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@AllArgsConstructor
public class KafkaConsumer {

    @Autowired
    private EmployeeService employeeService;

    @KafkaListener(
            topics = "${kafka.consumer.employee-upsert-topic}",
            autoStartup = "${kafka.consumer.enabled:false}",
            containerFactory = "employeeUpsertKafkaListenerContainerFactory")
    public void listenEmployeeUpsert(EmployeeMessage message) {
        try {
            if (message.operationType() == EmployeeOperationType.CREATE) {
                employeeService.createEmployee(message.employee());
            } else if (message.operationType() == EmployeeOperationType.UPDATE) {
                employeeService.updateEmployee(message.employee().id(), message.employee());
            } else {
                log.warn("unable to process record due to the operation type is invalid {}", message);
            }
        } catch (Exception exception) {
            // TODO add retry policy
            log.error("Unable to process employee deletion message", exception);
        }
    }

    @KafkaListener(
            topics = "${kafka.consumer.employee-deletion-topic}",
            autoStartup = "${kafka.consumer.enabled:false}",
            containerFactory = "employeeDeletionKafkaListenerContainerFactory")
    public void listenEmployeeDeletion(EmployeeMessage message) {
        try {
            employeeService.deleteEmployee(message.employee().id());
        } catch (Exception exception) {
            // TODO add retry policy
            log.error("Unable to process employee deletion message", exception);
        }
    }

}
