package com.employee.controllers;

import com.common.employee.dto.EmployeeDto;
import com.common.employee.dto.EmployeePage;
import com.employee.services.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Employees", description = "Set of endpoints for Retrieving Employees.")
@RestController
@RequiredArgsConstructor
public class EmployeeController {

  private final EmployeeService employeeService;

  @GetMapping("/employees/list/{page}/{size}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Returns list of all Employees in the system.")
  @SecurityRequirement(name = "Bearer Authentication")
  public EmployeePage list(
      @Parameter(
              required = true,
              name = "page",
              description = "Page number of the employee list to be obtained. Cannot be empty.")
          @PathVariable("page")
          Integer page,
      @Parameter(
              required = true,
              name = "size",
              description = "Page size of the employee list to be obtained. Cannot be empty.")
          @PathVariable("size")
          Integer pageSize) {
    return employeeService.list(page, pageSize);
  }

  @GetMapping("/employees/{id}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Returns a specific employee by their identifier. 404 if does not exist.")
  @SecurityRequirement(name = "Bearer Authentication")
  public EmployeeDto getEmployee(
      @Parameter(
              required = true,
              name = "id",
              description = "Id of the employee to be obtained. Cannot be empty.")
          @PathVariable("id")
          String employeeId) {

    return employeeService.getEmployee(employeeId);
  }
}
