package com.employee.it;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.employee.EmployeeConsumerServiceApplication;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@Disabled("fix integration test")
@SpringBootTest(
    classes = {
      EmployeeConsumerServiceApplication.class,
    })
@DisplayName("Employee tests")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
public class EmployeeServiceConsumerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean ClientRegistrationRepository registrations;

  @DisplayName("List all employees")
  @Test
  void getAllEmployees() throws Exception {
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
    when(registrations.findByRegistrationId(anyString())).thenReturn(buildClientRegistration());

    mockMvc
        .perform(
            get("/employees/{id}", "e26b1ed4-a8d0-11e9-a2a3-2a2ae2dbcce4")
                .with(jwt())
                .accept(MediaType.APPLICATION_JSON))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("e26b1ed4-a8d0-11e9-a2a3-2a2ae2dbcce4"));
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
