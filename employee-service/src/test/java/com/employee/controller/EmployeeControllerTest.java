package com.employee.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.common.employee.dto.EmployeeDto;
import com.common.employee.dto.EmployeePage;
import com.common.employee.dto.EmployeeRequest;
import com.common.employee.enums.Sort;
import com.common.employee.exceptions.EmployeeNotFound;
import com.employee.config.ApplicationExceptionHandler;
import com.employee.controllers.EmployeeController;
import com.employee.services.EmployeeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
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

@ExtendWith(SpringExtension.class)
@DisplayName("Employee tests")
public class EmployeeControllerTest {

  private static final LocalDate BASIC_DATE = LocalDate.of(2012, 9, 17);

  private MockMvc mockMvc;

  @Mock EmployeeService employeeService;

  @InjectMocks private EmployeeController employeeController;

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeAll
  public static void setUp() {
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.registerModule(new Jdk8Module());
  }

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
    var employeePage =
        EmployeePage.builder()
            .pageNumber(0)
            .sort(Sort.ASCENDING)
            .pageSize(10)
            .content(List.of(EmployeeDto.builder().id("id").build()))
            .build();
    when(employeeService.list(0, 10)).thenReturn(employeePage);

    mockMvc
        .perform(get("/employees/{page}/{total}", 0, 10).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].id").isNotEmpty());
  }

  @DisplayName("Get employee by Id")
  @Test
  void getEmployeeById() throws Exception {
    var employeeId = "e26b1ed4-a8d0-11e9-a2a3-2a2ae2dbcce4";
    var employeeDto = EmployeeDto.builder().id(employeeId).build();
    when(employeeService.getEmployee(employeeId)).thenReturn(employeeDto);

    mockMvc
        .perform(get("/employees/{id}", employeeId).accept(MediaType.APPLICATION_JSON))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(employeeId));
  }

  @DisplayName("Get employee by Id with invalid id")
  @Test
  void getEmployeeByIdWithInvalidIdThenNotFound() throws Exception {
    var employeeId = "invalid-id";
    when(employeeService.getEmployee(employeeId))
        .thenThrow(new EmployeeNotFound("Unable to find the Employee"));

    mockMvc
        .perform(get("/employees/{id}", employeeId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @DisplayName("Create a new employee")
  @Test
  void createEmployee() throws Exception {
    var request = new EmployeeRequest("Gerardo2", "J", "Luna", BASIC_DATE, BASIC_DATE);
    var responseDto = EmployeeDto.builder().firstName("Gerardo2").build();
    when(employeeService.createEmployee(request)).thenReturn(Optional.of(responseDto));

    mockMvc
        .perform(
            post("/employees")
                .content(asJsonString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.id").doesNotExist());
  }

  @DisplayName("Create a new employee with invalid request")
  @Test
  void createEmployeeWithInvalidRequest() throws Exception {

    mockMvc
        .perform(
            post("/employees")
                .content(asJsonString(new EmployeeRequest("", "", "", null, null)))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @DisplayName("Update employee")
  @Test
  void updateEmployee() throws Exception {
    var id = "e26b1ed4-a8d0-11e9-a2a3-2a2ae2dbcce4";
    var request = EmployeeDto.builder().id(id).firstName("Gerardo").lastName("Luna").build();
    when(employeeService.updateEmployee(id, request))
        .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(1));

    mockMvc
        .perform(
            put("/employees/{id}", id)
                .content(asJsonString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.firstName").value("Gerardo"))
        .andExpect(jsonPath("$.lastName").value("Luna"));
  }

  @DisplayName("Delete employee")
  @Test
  void deleteEmployee() throws Exception {

    mockMvc
        .perform(delete("/employees/{id}", "e26b1d76-a8d0-11e9-a2a3-2a2ae2dbcce4"))
        .andExpect(status().isAccepted());
  }

  public static String asJsonString(final Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
