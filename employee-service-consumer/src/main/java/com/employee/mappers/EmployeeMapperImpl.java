package com.employee.mappers;

import java.util.List;
import java.util.stream.Collectors;

import com.common.employee.dto.EmployeeDto;
import com.common.employee.dto.EmployeeRequest;
import com.common.employee.enums.EmployeeStatus;
import org.springframework.stereotype.Service;

import com.employee.entities.Employee;

import jakarta.validation.constraints.NotNull;

/**
  * implementation for mapper class between {@link EmployeeDto}, {@link Employee} and {@link EmployeeRequest}.
  */
@Service
public class EmployeeMapperImpl implements EmployeeMapper {

    /**
      * {@inheritDoc}
      */
    public EmployeeDto convert(@NotNull Employee employee) {
        return EmployeeDto.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .middleInitial(employee.getMiddleInitial())
                .status(employee.getStatus().name())
                .dateOfBirth(employee.getDateOfBirth())
                .dateOfEmployment(employee.getDateOfEmployment())
                .build();
    }

    /**
      * {@inheritDoc}
      */
    public List<EmployeeDto> convert(@NotNull List<Employee> employeeList) {
        return employeeList.stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    public Employee convert(@NotNull EmployeeRequest emplReq) {
        Employee employee = new Employee();
        employee.setFirstName(emplReq.firstName());
        employee.setLastName(emplReq.lastName());
        employee.setMiddleInitial(emplReq.middleInitial());
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setDateOfBirth(emplReq.dateOfBirth());
        employee.setDateOfEmployment(emplReq.dateOfEmployment());
        return employee;
    }

}
