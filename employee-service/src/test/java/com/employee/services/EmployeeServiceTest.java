package com.employee.services;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.common.employee.dto.EmployeeDto;
import com.common.employee.dto.EmployeeMessage;
import com.common.employee.dto.EmployeePage;
import com.common.employee.dto.EmployeeRequest;
import com.common.employee.enums.EmployeeOperationType;
import com.common.employee.enums.EmployeeStatus;
import com.common.employee.enums.Sort;
import com.common.employee.exceptions.EmployeeNotFound;
import com.employee.clients.EmployeeClient;
import com.employee.mappers.EmployeeMapper;
import com.employee.mappers.EmployeeMapperImpl;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;

/** Unit tests for {@link EmployeeService} */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
  private static final LocalDate BASIC_DATE = LocalDate.of(2012, 9, 17);

  @Captor private ArgumentCaptor<Message<EmployeeMessage>> employeeMessageCaptor;

  @Mock private KafkaTemplate<String, EmployeeMessage> employeeDeletionKafkaTemplate;

  @Mock private KafkaTemplate<String, EmployeeMessage> employeeUpsertKafkaTemplate;

  @Mock private EmployeeClient employeeClient;

  @Spy private EmployeeMapper employeeMapper = new EmployeeMapperImpl();

  private EmployeeService employeeService;

  @BeforeEach
  public void setUp() {
    this.employeeService =
        new EmployeeService(
            employeeDeletionKafkaTemplate,
            employeeUpsertKafkaTemplate,
            employeeClient,
            employeeMapper,
            Executors.newSingleThreadExecutor());
  }

  @DisplayName("List all employees")
  @Test
  void getAllEmployees() {
    var employeePage =
        EmployeePage.builder()
            .pageNumber(0)
            .sort(Sort.ASCENDING)
            .pageSize(10)
            .content(List.of(EmployeeDto.builder().id("id").build()))
            .build();
    when(employeeClient.listEmployees(0, 10)).thenReturn(employeePage);

    assertThat(employeeService.list(0, 10))
        .isNotNull()
        .usingRecursiveComparison()
        .isEqualTo(employeePage);
  }

  @DisplayName("Get employee by Id")
  @Test
  void getEmployeeById() {
    var employeeId = "e26b1ed4-a8d0-11e9-a2a3-2a2ae2dbcce4";
    var employeeDto = EmployeeDto.builder().id(employeeId).build();
    when(employeeClient.getEmployee(employeeId)).thenReturn(Optional.of(employeeDto));

    assertThat(employeeService.getEmployee(employeeId))
        .isNotNull()
        .usingRecursiveComparison()
        .isEqualTo(employeeDto);
  }

  @DisplayName("Get employee by Id with invalid id")
  @Test
  void getEmployeeByIdWithInvalidIdThenNotFound() {

    var employeeId = "e26b1ed4-a8d0-11e9-a2a3-2a2ae2dbcce4";
    when(employeeClient.getEmployee(employeeId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> employeeService.getEmployee(employeeId))
        .isInstanceOf(EmployeeNotFound.class)
        .hasMessageContaining("Unable to find the Employee");
  }

  @DisplayName("Create a new employee")
  @Test
  void createEmployee() {
    var request = new EmployeeRequest("Gerardo2", "J", "Luna", BASIC_DATE, BASIC_DATE);
    var expected =
        EmployeeDto.builder()
            .firstName("Gerardo2")
            .middleInitial("J")
            .lastName("Luna")
            .dateOfBirth(BASIC_DATE)
            .dateOfEmployment(BASIC_DATE)
            .status(EmployeeStatus.ACTIVE)
            .build();
    when(employeeUpsertKafkaTemplate.send(any(Message.class)))
            .thenReturn(CompletableFuture.completedFuture(new SendResult(null, null)));
    assertThat(employeeService.createEmployee(request))
        .isNotNull()
        .isEqualTo(Optional.of(expected));

    verify(employeeUpsertKafkaTemplate).send(employeeMessageCaptor.capture());

    assertThat(employeeMessageCaptor.getValue())
        .as("message should be valid and with CREATE operation")
        .isNotNull()
        .extracting(Message::getPayload)
        .isNotNull()
        .isExactlyInstanceOf(EmployeeMessage.class)
        .asInstanceOf(InstanceOfAssertFactories.type(EmployeeMessage.class))
        .extracting(EmployeeMessage::operationType)
        .isEqualTo(EmployeeOperationType.CREATE);
  }

  @DisplayName("Update employee")
  @Test
  void updateEmployee() {
    var id = "e26b1ed4-a8d0-11e9-a2a3-2a2ae2dbcce4";
    var req =
        EmployeeDto.builder()
            .firstName("Gerardo")
            .middleInitial("J")
            .lastName("Luna")
            .dateOfBirth(BASIC_DATE)
            .dateOfEmployment(BASIC_DATE)
            .build();

    assertThat(employeeService.updateEmployee(id, req)).isNotNull().isEqualTo(req);
    verify(employeeUpsertKafkaTemplate).send(employeeMessageCaptor.capture());

    assertThat(employeeMessageCaptor.getValue())
        .as("message should be valid and with UPDATE operation")
        .isNotNull()
        .extracting(Message::getPayload)
        .isNotNull()
        .returns(EmployeeOperationType.UPDATE, EmployeeMessage::operationType)
        .satisfies(
            employeeMessage -> {
              assertThat(employeeMessage.employee())
                  .as("employee information is correct")
                  .isNotNull()
                  .returns(req.firstName(), EmployeeDto::firstName)
                  .returns(req.lastName(), EmployeeDto::lastName)
                  .returns(req.middleInitial(), EmployeeDto::middleInitial)
                  .returns(req.dateOfBirth(), EmployeeDto::dateOfBirth)
                  .returns(req.dateOfEmployment(), EmployeeDto::dateOfEmployment);
            });
  }

  @DisplayName("Delete employee")
  @Test
  void deleteEmployee() {
    var id = "e26b1d76-a8d0-11e9-a2a3-2a2ae2dbcce4";
    assertThatCode(() -> employeeService.deleteEmployee(id)).doesNotThrowAnyException();

    verify(employeeDeletionKafkaTemplate).send(employeeMessageCaptor.capture());
    assertThat(employeeMessageCaptor.getValue())
        .as("message should be valid and with DELETE operation")
        .isNotNull()
        .extracting(Message::getPayload)
        .isNotNull()
        .isExactlyInstanceOf(EmployeeMessage.class)
        .asInstanceOf(InstanceOfAssertFactories.type(EmployeeMessage.class))
        .extracting(EmployeeMessage::operationType)
        .isEqualTo(EmployeeOperationType.DELETE);
  }
}
