package com.employee.mappers;

import com.common.employee.dto.EmployeeDto;
import com.common.employee.dto.EmployeeInfo;
import jakarta.validation.constraints.NotNull;

/** Mapper class between {@link EmployeeDto}, {@link EmployeeDto} and {@link EmployeeInfo}. */
public interface EmployeeMapper {


  /**
   * Converts {@link EmployeeInfo} to {@link EmployeeDto}.
   *
   * @param empl the employee dto
   * @return the employee info
   */
  EmployeeInfo convert(EmployeeDto empl);

  /**
   * Converts {@link EmployeeDto} to {@link EmployeeInfo}.
   *
   * @param empl the employee info
   * @return the employee dto
   */
  EmployeeDto convert(@NotNull EmployeeInfo empl);
}
