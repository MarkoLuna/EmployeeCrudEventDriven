package com.employee.clients;

import com.common.employee.dto.EmployeeDto;
import com.common.employee.dto.EmployeePage;
import feign.Param;
import feign.RequestLine;
import java.util.Optional;

public interface EmployeeClient {

  @RequestLine("GET /employees/{id}")
  Optional<EmployeeDto> getEmployee(@Param("id") String id);

  @RequestLine("GET /employees/{page}/{size}")
  EmployeePage listEmployees(@Param("page") Integer page, @Param("size") Integer size);
}
