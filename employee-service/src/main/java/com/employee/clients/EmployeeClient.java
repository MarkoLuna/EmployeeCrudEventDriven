package com.employee.clients;

import com.common.employee.dto.EmployeeDto;
import com.common.employee.dto.EmployeePage;
import feign.Param;
import feign.RequestLine;
import java.util.Optional;

/**
 * Feign client in the employee-service (producer) that calls the employee-service-consumer
 * synchronously for read operations.
 *
 * <p>Write operations (create, update, delete) go through Kafka. This client is used only for GET
 * and LIST endpoints, forwarding the Bearer token via {@link
 * com.employee.config.AuthorizationInterceptor}.
 */
public interface EmployeeClient {
  /**
   * Get an employee by id.
   *
   * @param id the employee id
   * @return the employee
   */
  @RequestLine("GET /employees/{id}")
  Optional<EmployeeDto> getEmployee(@Param("id") String id);

  /**
   * List all employees.
   *
   * @param page the page number
   * @param size the page size
   * @return the list of employees
   */
  @RequestLine("GET /employees/list/{page}/{size}")
  EmployeePage listEmployees(@Param("page") Integer page, @Param("size") Integer size);
}
