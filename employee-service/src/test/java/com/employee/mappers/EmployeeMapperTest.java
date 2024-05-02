package com.employee.mappers;

import java.time.LocalDate;

import com.common.employee.dto.EmployeeDto;
import com.common.employee.dto.EmployeeRequest;
import com.common.employee.enums.EmployeeStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
  * Unit tests for {@link EmployeeMapper}.
  */
@ExtendWith(MockitoExtension.class)
class EmployeeMapperTest {

    private EmployeeMapper employeeMapper = new EmployeeMapperImpl();

    @Test
    @DisplayName("Verify the mapping from EmployeeRequest to EmployeeDto")
    void whenMapFromEmployeeRequest_WithValidObjectToMap_ThenAllFieldsAreCorrectlyMapped() {
        var expected = buildEmployeeDto();
        var employee = buildEmployeeRequest();

        var employeeDto = employeeMapper.convert(employee);

        assertThat(employeeDto).isNotNull()
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expected);
    }

    private EmployeeRequest buildEmployeeRequest() {
        return EmployeeRequest.builder()
                .firstName("firstName")
                .lastName("lastName")
                .middleInitial("middleInitial")
                .dateOfBirth(LocalDate.of(2012, 9, 17))
                .dateOfEmployment(LocalDate.of(2014, 9, 17))
                .build();
    }

    private EmployeeDto buildEmployeeDto() {
        return new EmployeeDto(
                "fee120ce-6b40-47c1-a24e-e1e3cf50e6b0",
                "firstName",
                "middleInitial",
                "lastName",
                LocalDate.of(2012, 9, 17),
                LocalDate.of(2014, 9, 17),
                EmployeeStatus.ACTIVE
        );
    }

}
