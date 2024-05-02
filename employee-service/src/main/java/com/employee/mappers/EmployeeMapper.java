package com.employee.mappers;

import com.common.employee.dto.EmployeeDto;
import com.common.employee.dto.EmployeeRequest;

import jakarta.validation.constraints.NotNull;

/**
  * Mapper class between {@link EmployeeDto}, {@link EmployeeDto} and {@link EmployeeRequest}.
  */
public interface EmployeeMapper {

    EmployeeDto convert(@NotNull EmployeeRequest emplReq);

}
