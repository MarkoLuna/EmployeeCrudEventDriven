package com.employee.it;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.common.employee.dto.EmployeeDto;
import com.common.employee.dto.EmployeeMessage;
import com.common.employee.dto.EmployeePage;
import com.common.employee.dto.EmployeeRequest;
import com.common.employee.enums.Sort;
import com.employee.clients.EmployeeClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@Disabled
@ExtendWith(SpringExtension.class)
@SpringBootTest(
    classes = {
      com.employee.EmployeeServiceApplication.class,
    })
@DisplayName("Employee tests")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
public class EmployeeServiceTest {

  private static final LocalDate BASIC_DATE = LocalDate.of(2012, 9, 17);

  @Autowired private MockMvc mockMvc;

  @MockBean ClientRegistrationRepository registrations;

  @MockBean EmployeeClient employeeClient;

  @MockBean(name = "employeeDeletionKafkaTemplate")
  KafkaTemplate<String, EmployeeMessage> employeeDeletionKafkaTemplate;

  @MockBean(name = "employeeUpsertKafkaTemplate")
  KafkaTemplate<String, EmployeeMessage> employeeUpsertKafkaTemplate;

  private static ObjectMapper objectMapper = new ObjectMapper();

  @BeforeAll
  private static void setUp() {
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.registerModule(new Jdk8Module());
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
    when(employeeClient.listEmployees(0, 10)).thenReturn(employeePage);
    when(registrations.findByRegistrationId(anyString())).thenReturn(buildClientRegistration());

    mockMvc
        .perform(
            get("/employees/{page}/{total}", 0, 10).with(jwt()).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].id").isNotEmpty());
  }

  @DisplayName("Attempt get all employees and has unauthorized status")
  @Test
  void getAllEmployeesUnAuthorized() throws Exception {
    when(registrations.findByRegistrationId(anyString())).thenReturn(buildClientRegistration());

    mockMvc
        .perform(get("/employees/{page}/{total}", 0, 10).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @DisplayName("Get employee by Id")
  @Test
  void getEmployeeById() throws Exception {
    var employeeId = "e26b1ed4-a8d0-11e9-a2a3-2a2ae2dbcce4";
    var employeeDto = EmployeeDto.builder().id(employeeId).build();
    when(employeeClient.getEmployee(employeeId)).thenReturn(Optional.of(employeeDto));
    when(registrations.findByRegistrationId(anyString())).thenReturn(buildClientRegistration());

    mockMvc
        .perform(get("/employees/{id}", employeeId).with(jwt()).accept(MediaType.APPLICATION_JSON))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(employeeId));
  }

  @DisplayName("Get employee by Id with invalid id")
  @Test
  void getEmployeeByIdWithInvalidIdThenNotFound() throws Exception {
    when(registrations.findByRegistrationId(anyString())).thenReturn(buildClientRegistration());

    mockMvc
        .perform(
            get("/employees/{id}", "invalid-id").with(jwt()).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @DisplayName("Create a new employee")
  @Test
  void createEmployee() throws Exception {
    when(employeeUpsertKafkaTemplate.send(any(Message.class)))
        .thenReturn(CompletableFuture.completedFuture(null));
    when(registrations.findByRegistrationId(anyString())).thenReturn(buildClientRegistration());

    mockMvc
        .perform(
            post("/employees")
                .with(jwt())
                .content(
                    asJsonString(
                        new EmployeeRequest("Gerardo2", "J", "Luna", BASIC_DATE, BASIC_DATE)))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isProcessing())
        .andExpect(jsonPath("$.id").doesNotExist());
  }

  @DisplayName("Create a new employee with invalid request")
  @Test
  void createEmployeeWithInvalidRequest() throws Exception {
    when(registrations.findByRegistrationId(anyString())).thenReturn(buildClientRegistration());

    mockMvc
        .perform(
            post("/employees")
                .with(jwt())
                .content(asJsonString(new EmployeeRequest("", "", "", null, null)))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @DisplayName("Update employee")
  @Test
  void updateEmployee() throws Exception {
    when(employeeUpsertKafkaTemplate.send(any(Message.class)))
        .thenReturn(CompletableFuture.completedFuture(null));
    when(registrations.findByRegistrationId(anyString())).thenReturn(buildClientRegistration());

    mockMvc
        .perform(
            put("/employees/{id}", "e26b1ed4-a8d0-11e9-a2a3-2a2ae2dbcce4")
                .with(jwt())
                .content(
                    asJsonString(
                        new EmployeeRequest("Gerardo", "J", "Luna", BASIC_DATE, BASIC_DATE)))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isProcessing())
        .andExpect(jsonPath("$.firstName").value("Gerardo"))
        .andExpect(jsonPath("$.lastName").value("Luna"));
  }

  @DisplayName("Delete employee")
  @Test
  void deleteEmployee() throws Exception {
    when(employeeDeletionKafkaTemplate.send(any(Message.class)))
        .thenReturn(CompletableFuture.completedFuture(null));
    when(registrations.findByRegistrationId(anyString())).thenReturn(buildClientRegistration());

    mockMvc
        .perform(delete("/employees/{id}", "e26b1d76-a8d0-11e9-a2a3-2a2ae2dbcce4").with(jwt()))
        .andExpect(status().isProcessing());
  }

  public static String asJsonString(final Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private ClientRegistration buildClientRegistration() {
    return ClientRegistration.withRegistrationId("id")
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .clientId("newClient")
        .clientSecret("newClientSecret")
        .redirectUri("http://localhost:8080/login")
        .authorizationUri("http://localhost:8080/new-client/login/oauth2/code/custom")
        .tokenUri("http://localhost:8080/new-client/login/oauth2/code/custom")
        .build();
  }
}
