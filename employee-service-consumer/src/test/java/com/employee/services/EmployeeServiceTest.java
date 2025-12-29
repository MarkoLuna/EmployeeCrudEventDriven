package com.employee.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.common.employee.dto.EmployeeDto;
import com.common.employee.dto.EmployeeRequest;
import com.common.employee.enums.EmployeeStatus;
import com.common.employee.exceptions.EmployeeNotFound;
import com.employee.entities.Employee;
import com.employee.mappers.EmployeeMapper;
import com.employee.mappers.EmployeeMapperImpl;
import com.employee.repositories.EmployeeRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

/** Unit tests for {@link EmployeeService} */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
  private static final LocalDate BASIC_DATE = LocalDate.of(2012, 9, 17);

  @Captor private ArgumentCaptor<Employee> employeeCaptor;

  @Mock private EmployeeRepository employeeRepository;

  @Spy private EmployeeMapper employeeMapper = new EmployeeMapperImpl();

  @InjectMocks private EmployeeService employeeService;

  @DisplayName("List all employees")
  @Test
  void getAllEmployees() {
    var employeeId = "id";
    var employees = List.of(createEmployeeEntity(employeeId));
    org.springframework.data.domain.Sort orders =
        org.springframework.data.domain.Sort.by(
            org.springframework.data.domain.Sort.Direction.DESC, "dateOfEmployment");
    var pageReq = PageRequest.of(0, 10, orders);

    when(employeeRepository.findByStatus(EmployeeStatus.ACTIVE, pageReq))
        .thenReturn(new PageImpl<>(employees));

    var result = employeeService.list(0, 10);
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isNotEmpty().hasSize(1);
  }

  @DisplayName("Get employee by Id")
  @Test
  void getEmployeeById() {
    var employeeId = "e26b1ed4-a8d0-11e9-a2a3-2a2ae2dbcce4";
    var employee = createEmployeeEntity(employeeId);
    when(employeeRepository.findByIdAndStatus(employeeId, EmployeeStatus.ACTIVE))
        .thenReturn(Optional.of(employee));

    assertThat(employeeService.getEmployee(employeeId))
        .isNotNull()
        .usingRecursiveComparison()
        .isEqualTo(employee);
  }

  @DisplayName("Get employee by Id with invalid id")
  @Test
  void getEmployeeByIdWithInvalidIdThenNotFound() {

    var employeeId = "e26b1ed4-a8d0-11e9-a2a3-2a2ae2dbcce4";
    when(employeeRepository.findByIdAndStatus(employeeId, EmployeeStatus.ACTIVE))
        .thenReturn(Optional.empty());

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
    assertThat(employeeService.createEmployee(request))
        .isNotNull()
        .isEqualTo(Optional.of(expected));
  }

  @DisplayName("Update employee")
  @Test
  void updateEmployee() {
    var id = "e26b1ed4-a8d0-11e9-a2a3-2a2ae2dbcce4";
    var req =
        EmployeeDto.builder()
            .id(id)
            .firstName("Gerardo")
            .middleInitial("J")
            .lastName("Luna")
            .dateOfBirth(BASIC_DATE)
            .dateOfEmployment(BASIC_DATE)
            .build();

    when(employeeRepository.findByIdAndStatus(id, EmployeeStatus.ACTIVE))
        .thenReturn(Optional.of(createEmployeeEntity(id)));
    assertThat(employeeService.updateEmployee(id, req))
        .isNotNull()
        .usingRecursiveComparison()
        .isEqualTo(req);
    verify(employeeRepository).save(employeeCaptor.capture());

    assertThat(employeeCaptor.getValue())
        .as("employee information is correct")
        .isNotNull()
        .returns(req.firstName(), Employee::getFirstName)
        .returns(req.lastName(), Employee::getLastName)
        .returns(req.middleInitial(), Employee::getMiddleInitial)
        .returns(req.dateOfBirth(), Employee::getDateOfBirth)
        .returns(req.dateOfEmployment(), Employee::getDateOfEmployment);
  }

  @DisplayName("Delete employee")
  @Test
  void deleteEmployee() {
    var id = "e26b1d76-a8d0-11e9-a2a3-2a2ae2dbcce4";
    var employee = createEmployeeEntity(id);

    when(employeeRepository.findByIdAndStatus(id, EmployeeStatus.ACTIVE))
        .thenReturn(Optional.of(employee));

    assertThatCode(() -> employeeService.deleteEmployee(id)).doesNotThrowAnyException();

    verify(employeeRepository).deleteById(id);
  }

  private Employee createEmployeeEntity(String id) {
    var employee = new Employee();
    employee.setId(id);
    employee.setFirstName("Gerardo");
    employee.setMiddleInitial("J");
    employee.setLastName("Luna");
    employee.setDateOfBirth(BASIC_DATE);
    employee.setDateOfEmployment(BASIC_DATE);
    return employee;
  }
}
