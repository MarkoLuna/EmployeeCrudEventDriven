package com.employee.mappers;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.employee.dto.EmployeeDto;
import com.employee.entities.Employee;
import com.employee.enums.EmployeeStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
  * Unit tests for {@link EmployeeMapper}.
  */
@ExtendWith(MockitoExtension.class)
class EmployeeMapperTest {

    private EmployeeMapper employeeMapper = new EmployeeMapperImpl();

    @Test
    @DisplayName("Verify the mapping from a single Employee to EmployeeDto")
    void whenMapFromEmployee_WithValidObjectToMap_ThenAllFieldsAreCorrectlyMapped() {
        var expected = buildEmployeeDto();
        var employee = buildEmployee();

        var employeeDto = employeeMapper.convert(employee);

        assertThat(employeeDto).isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("Verify the mapping from multiple Employee to EmployeeDto.")
    void whenMapFromEmployees_WithValidObjectToMap_ThenAllFieldsAreCorrectlyMapped() {
        var expected = List.of(buildEmployeeDto());
        var employeeList = List.of(buildEmployee());

        var listEmployeeDto = employeeMapper.convert(employeeList);

        assertThat(listEmployeeDto).isNotNull()
                .isNotEmpty()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }


    private Employee buildEmployee() {
        var employee = new Employee();

        employee.setId("fee120ce-6b40-47c1-a24e-e1e3cf50e6b0");
        employee.setFirstName("firstName");
        employee.setLastName("lastName");
        employee.setMiddleInitial("middleInitial");
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setDateOfBirth(LocalDate.of(2012, 9, 17));
        employee.setDateOfEmployment(LocalDate.of(2014, 9, 17));
        return employee;
    }

    private EmployeeDto buildEmployeeDto() {
        return new EmployeeDto(
                "fee120ce-6b40-47c1-a24e-e1e3cf50e6b0",
                "firstName",
                "middleInitial",
                "lastName",
                LocalDate.of(2012, 9, 17),
                LocalDate.of(2014, 9, 17),
                "ACTIVE"
        );
    }

}
