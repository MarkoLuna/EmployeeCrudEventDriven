package com.employee.services;

import com.common.employee.dto.EmployeeMessage;
import com.common.employee.enums.EmployeeOperationType;
import com.common.employee.exceptions.EmployeeNotFound;
import com.employee.exceptions.RetryableMessagingException;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.retrytopic.RetryTopicHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

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
    public void listenEmployeeUpsert(@Payload EmployeeMessage message,
                                     @Header(name = RetryTopicHeaders.DEFAULT_HEADER_ATTEMPTS, required = false)
                                     Integer nonBlockingAttempts) {
        var entry = log.traceEntry("listenEmployeeUpsertMessage: {}", message);
        try {
            if (recordAlreadyProcessed(nonBlockingAttempts, message)) {
                return;
            }
            if (message.operationType() == EmployeeOperationType.CREATE) {
                employeeService.createEmployee(message.employee());
            } else if (message.operationType() == EmployeeOperationType.UPDATE) {
                employeeService.updateEmployee(message.employee().id(), message.employee());
            } else {
                log.warn("unable to process record due to the operation type is invalid {}", message);
            }
            log.traceExit(entry);
        } catch (EmployeeNotFound ex) {
            log.error("Unable to process employee update message", ex);
            throw RetryableMessagingException.fromException(ex);
        } catch (Exception exception) {
            log.error("Unable to process employee upsert message", exception);
        }
    }

    @KafkaListener(
            topics = "${kafka.consumer.employee-deletion-topic}",
            autoStartup = "${kafka.consumer.enabled:false}",
            containerFactory = "employeeDeletionKafkaListenerContainerFactory")
    public void listenEmployeeDeletion(@Payload EmployeeMessage message,
                                       @Header(name = RetryTopicHeaders.DEFAULT_HEADER_ATTEMPTS, required = false)
                                       Integer nonBlockingAttempts) {
        var entry = log.traceEntry("listenEmployeeDeletionMessage: {}", message);
        try {
            if (recordAlreadyProcessed(nonBlockingAttempts, message)) {
                return;
            }
            employeeService.deleteEmployee(message.employee().id());
            log.traceExit(entry);
        } catch (EmployeeNotFound ex) {
            log.error("Unable to process employee deletion message", ex);
            throw RetryableMessagingException.fromException(ex);
        } catch (Exception exception) {
            log.error("Unable to process employee deletion message", exception);
        }
    }

    private boolean recordAlreadyProcessed(Integer nonBlockingAttempts, EmployeeMessage message) {
        int deliveryAttempt = isNull(nonBlockingAttempts) ? 0 : nonBlockingAttempts;
        if (deliveryAttempt > 0) {
            //If it comes from retries then we verify first if the record exists
            log.info("Getting the ms365 event again. delivery attempt: [{}] the employeeId: [{}]",
                    () -> deliveryAttempt, () -> message.employee().id());
            if (employeeService.employeeMatch(message.employee())) {
                log.info("Record Already found, wont process.. delivery attempt: [{}] the employeeId: [{}]",
                        () -> deliveryAttempt, () -> message.employee().id());
                return true;
            }
        }
        return false;
    }

    /**
     * Handles DLT messages.
     *
     * @param errorMessage the error message to handle.
     */
    public void handleDltForEmployeeDeletion(Message<?> errorMessage) {
        //TO DO HANDLE DLT
        log.warn("Employee Deletion kafka dlt listener reached, max retrys: {}", errorMessage);
    }

    /**
     * Handles DLT messages.
     *
     * @param errorMessage the error message to handle.
     */
    public void handleDltForEmployeeUpsert(Message<?> errorMessage) {
        //TO DO HANDLE DLT
        log.warn("Employee Upsert kafka dlt listener reached, max retrys: {}", errorMessage);
    }

}
