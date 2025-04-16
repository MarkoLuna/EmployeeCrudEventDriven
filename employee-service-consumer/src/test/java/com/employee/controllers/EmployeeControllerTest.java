package com.employee.controllers;

import com.common.employee.dto.EmployeeDto;
import com.common.employee.dto.EmployeeRequest;
import com.common.employee.exceptions.EmployeeNotFound;
import com.employee.config.ApplicationExceptionHandler;
import com.employee.services.EmployeeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link EmployeeController}.
 */
@ExtendWith(SpringExtension.class)
@DisplayName("Employee tests")
class EmployeeControllerTest {

    private static final LocalDate BASIC_DATE = LocalDate.of(2012, 9, 17);

    private MockMvc mockMvc;

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    @BeforeAll
    static void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new Jdk8Module());
    }

    @BeforeEach
    public void sepUpTests() {
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController)
                .setControllerAdvice(new ApplicationExceptionHandler())
                .build();
    }

    @DisplayName("List all employees")
    @Test
    void getAllEmployees() throws Exception {

        var employees = List.of(EmployeeDto.builder().id("id").build());
        when(employeeService.list(0, 10)).thenReturn(new PageImpl<>(employees, Pageable.ofSize(employees.size()), employees.size()));

        mockMvc.perform(get("/employees/{page}/{total}", 0, 10)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id").isNotEmpty());
    }

    @DisplayName("Get employee by Id")
    @Test
    void getEmployeeById() throws Exception {

        var id = "e26b1ed4-a8d0-11e9-a2a3-2a2ae2dbcce4";
        var employeeResponse = EmployeeDto.builder().id(id).firstName("name").build();
        when(employeeService.getEmployee(id)).thenReturn(employeeResponse);
        mockMvc.perform(get("/employees/{id}", id)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @DisplayName("Get employee by Id with invalid id")
    @Test
    void getEmployeeByIdWithInvalidIdThenNotFound() throws Exception {
        var id = "invalid-id";
        when(employeeService.getEmployee(id)).thenThrow(new EmployeeNotFound("Unable to find the Employee"));
        mockMvc.perform(get("/employees/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @DisplayName("Create a new employee")
    @Test
    void createEmployee() throws Exception {

        var request = new EmployeeRequest("Gerardo2", "J", "Luna", BASIC_DATE, BASIC_DATE);
        var employeeDto = EmployeeDto.builder()
                .id("id")
                .firstName("Gerardo")
                .middleInitial("J")
                .lastName("Luna")
                .dateOfBirth(BASIC_DATE)
                .dateOfEmployment(BASIC_DATE)
                .build();
        when(employeeService.createEmployee(request)).thenReturn(Optional.of(employeeDto));

        mockMvc.perform(post("/employees")
                .content(asJsonString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    @DisplayName("Create a new employee with invalid request")
    @Test
    void createEmployeeWithInvalidRequest() throws Exception {

        mockMvc.perform(post("/employees")
                        .content(asJsonString(new EmployeeRequest("", "", "", null, null)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Update employee")
    @Test
    void updateEmployee() throws Exception {
        var id = "e26b1ed4-a8d0-11e9-a2a3-2a2ae2dbcce4";
        var request = new EmployeeRequest("Gerardo", "J", "Luna", BASIC_DATE, BASIC_DATE);
        var employeeDto = EmployeeDto.builder()
                .firstName("Gerardo")
                .middleInitial("J")
                .lastName("Luna")
                .dateOfBirth(BASIC_DATE)
                .dateOfEmployment(BASIC_DATE)
                .build();

        when(employeeService.updateEmployee(id, employeeDto))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(1));

        mockMvc.perform(put("/employees/{id}", "e26b1ed4-a8d0-11e9-a2a3-2a2ae2dbcce4")
                .content(asJsonString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Gerardo"))
                .andExpect(jsonPath("$.lastName").value("Luna"));
    }

    @DisplayName("Delete employee")
    @Test
    void deleteEmployee() throws Exception {
        mockMvc.perform(delete("/employees/{id}", "e26b1d76-a8d0-11e9-a2a3-2a2ae2dbcce4"))
                .andExpect(status().isOk());
    }

    public static String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
