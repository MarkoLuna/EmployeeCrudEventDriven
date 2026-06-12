package com.employee.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.common.employee.dto.EmployeeDto;
import com.common.employee.dto.EmployeePage;
import com.common.employee.exceptions.EmployeeNotFound;
import com.employee.config.ApplicationExceptionHandler;
import com.employee.services.EmployeeService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Unit tests for {@link EmployeeController}. */
@ExtendWith(SpringExtension.class)
@DisplayName("Employee tests")
class EmployeeControllerTest {

  private MockMvc mockMvc;

  @Mock private EmployeeService employeeService;

  @InjectMocks private EmployeeController employeeController;

  @BeforeEach
  public void sepUpTests() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(employeeController)
            .setControllerAdvice(new ApplicationExceptionHandler())
            .build();
  }

  @DisplayName("List all employees")
  @Test
  void getAllEmployees() throws Exception {

    var employees = List.of(EmployeeDto.builder().id("id").build());
    when(employeeService.list(0, 10))
        .thenReturn(EmployeePage.builder().content(employees).pageSize(employees.size()).build());

    mockMvc
        .perform(get("/employees/list/{page}/{total}", 0, 10).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].id").isNotEmpty());
  }

  @DisplayName("Get employee by Id")
  @Test
  void getEmployeeById() throws Exception {

    var id = "e26b1ed4-a8d0-11e9-a2a3-2a2ae2dbcce4";
    var employeeResponse = EmployeeDto.builder().id(id).firstName("name").build();
    when(employeeService.getEmployee(id)).thenReturn(employeeResponse);
    mockMvc
        .perform(get("/employees/{id}", id).accept(MediaType.APPLICATION_JSON))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id));
  }

  @DisplayName("Get employee by Id with invalid id")
  @Test
  void getEmployeeByIdWithInvalidIdThenNotFound() throws Exception {
    var id = "invalid-id";
    when(employeeService.getEmployee(id))
        .thenThrow(new EmployeeNotFound("Unable to find the Employee"));
    mockMvc
        .perform(get("/employees/{id}", id).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }
}
