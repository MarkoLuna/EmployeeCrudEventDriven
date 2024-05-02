package com.employee.mappers;

import java.util.List;

import com.common.employee.dto.EmployeeDto;
import com.common.employee.dto.EmployeeRequest;
import com.employee.entities.Employee;

import jakarta.validation.constraints.NotNull;

/**
  * Mapper class between {@link EmployeeDto}, {@link Employee} and {@link EmployeeRequest}.
  */
public interface EmployeeMapper {

    /**
      * Maps {@link Employee} to {@link EmployeeDto}.
      *
      * @param employee {@link Employee} to move
      * @return {@link EmployeeDto} containing the stored data.
      */
    EmployeeDto convert(@NotNull Employee employee);

    /**
      * Maps a list of {@link Employee} to a list of {@link EmployeeDto}.
      *
      * @param employeeList list of {@link Employee} to move
      * @return a list of {@link EmployeeDto} containing the stored data.
      */
    List<EmployeeDto> convert(@NotNull List<Employee> employeeList);

    Employee convert(@NotNull EmployeeDto employeeDto);

}
