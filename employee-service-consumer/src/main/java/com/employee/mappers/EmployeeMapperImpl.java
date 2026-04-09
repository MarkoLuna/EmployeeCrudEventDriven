package com.employee.mappers;

import com.common.employee.dto.EmployeeDto;
import com.common.employee.dto.EmployeeInfo;
import com.common.employee.enums.EmployeeStatus;
import com.employee.entities.Employee;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

/**
 * implementation for mapper class between {@link EmployeeDto}, {@link Employee} and {@link
 * EmployeeInfo}.
 */
@Service
public class EmployeeMapperImpl implements EmployeeMapper {

  /** {@inheritDoc} */
  public EmployeeDto convert(@NotNull Employee employee) {
    return EmployeeDto.builder()
        .id(employee.getId())
        .firstName(employee.getFirstName())
        .lastName(employee.getLastName())
        .middleInitial(employee.getMiddleInitial())
        .status(employee.getStatus())
        .dateOfBirth(employee.getDateOfBirth())
        .dateOfEmployment(employee.getDateOfEmployment())
        .build();
  }

  /** {@inheritDoc} */
  public List<EmployeeDto> convert(@NotNull List<Employee> employeeList) {
    return employeeList.stream().map(this::convert).toList();
  }

  /** {@inheritDoc} */
  public Employee convert(@NotNull EmployeeDto employeeDto) {
    Employee employee = new Employee();
    employee.setFirstName(employeeDto.firstName());
    employee.setLastName(employeeDto.lastName());
    employee.setMiddleInitial(employeeDto.middleInitial());
    employee.setStatus(EmployeeStatus.ACTIVE);
    employee.setDateOfBirth(employeeDto.dateOfBirth());
    employee.setDateOfEmployment(employeeDto.dateOfEmployment());
    return employee;
  }

  @Override
  public Employee convert(EmployeeInfo info) {
    Employee employee = new Employee();
    employee.setFirstName(info.firstName());
    employee.setLastName(info.lastName());
    employee.setMiddleInitial(info.middleInitial());
    employee.setStatus(Optional.ofNullable(info.status()).orElse(EmployeeStatus.ACTIVE));
    employee.setDateOfBirth(info.dateOfBirth());
    employee.setDateOfEmployment(info.dateOfEmployment());
    return employee;
  }
}
