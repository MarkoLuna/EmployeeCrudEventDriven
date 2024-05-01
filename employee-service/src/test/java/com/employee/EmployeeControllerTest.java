package com.employee;

import java.time.LocalDate;

import com.employee.dto.EmployeeRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        EmployeeCrudApplication.class,
})
@DisplayName("Employee tests")
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EmployeeControllerTest {

    private static final LocalDate BASIC_DATE = LocalDate.of(2012, 9, 17);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    ClientRegistrationRepository registrations;

    private static ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    private static void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new Jdk8Module());
    }

    @DisplayName("List all employees")
    @Test
    public void getAllEmployees() throws Exception {
        when(registrations.findByRegistrationId(anyString())).thenReturn(buildClientRegistration());

        mockMvc.perform(get("/employees/{page}/{total}", 0, 10)
                        .with(jwt())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id").isNotEmpty());
    }

    @DisplayName("Attempt get all employees and has unauthorized status")
    @Test
    public void getAllEmployeesUnAuthorized() throws Exception {
        when(registrations.findByRegistrationId(anyString())).thenReturn(buildClientRegistration());

        mockMvc.perform(get("/employees/{page}/{total}", 0, 10)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Get employee by Id")
    @Test
    public void getEmployeeById() throws Exception {
        when(registrations.findByRegistrationId(anyString())).thenReturn(buildClientRegistration());

        mockMvc.perform(get("/employees/{id}", "e26b1ed4-a8d0-11e9-a2a3-2a2ae2dbcce4")
                        .with(jwt())
                .accept(MediaType.APPLICATION_JSON))
                // .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("e26b1ed4-a8d0-11e9-a2a3-2a2ae2dbcce4"));
    }

    @DisplayName("Get employee by Id with invalid id")
    @Test
    public void getEmployeeByIdWithInvalidIdThenNotFound() throws Exception {
        when(registrations.findByRegistrationId(anyString())).thenReturn(buildClientRegistration());

        mockMvc.perform(get("/employees/{id}", "invalid-id")
                        .with(jwt())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @DisplayName("Create a new employee")
    @Test
    public void createEmployee() throws Exception {
        when(registrations.findByRegistrationId(anyString())).thenReturn(buildClientRegistration());

        mockMvc.perform(post("/employees")
                        .with(jwt())
                .content(asJsonString(new EmployeeRequest("Gerardo2", "J", "Luna", BASIC_DATE, BASIC_DATE)))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    @DisplayName("Create a new employee with invalid request")
    @Test
    public void createEmployeeWithInvalidRequest() throws Exception {
        when(registrations.findByRegistrationId(anyString())).thenReturn(buildClientRegistration());

        mockMvc.perform(post("/employees")
                        .with(jwt())
                        .content(asJsonString(new EmployeeRequest("", "", "", null, null)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Update employee")
    @Test
    public void updateEmployee() throws Exception {
        when(registrations.findByRegistrationId(anyString())).thenReturn(buildClientRegistration());

        mockMvc.perform(put("/employees/{id}", "e26b1ed4-a8d0-11e9-a2a3-2a2ae2dbcce4")
                        .with(jwt())
                .content(asJsonString(new EmployeeRequest("Gerardo", "J", "Luna", BASIC_DATE, BASIC_DATE)))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Gerardo"))
                .andExpect(jsonPath("$.lastName").value("Luna"));
    }

    @DisplayName("Delete employee")
    @Test
    public void deleteEmployee() throws Exception {
        when(registrations.findByRegistrationId(anyString())).thenReturn(buildClientRegistration());

        mockMvc.perform(delete("/employees/{id}", "e26b1d76-a8d0-11e9-a2a3-2a2ae2dbcce4")
                        .with(jwt())
                )
                .andExpect(status().isOk());
    }

    public static String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private ClientRegistration buildClientRegistration() {
        return ClientRegistration
                .withRegistrationId("id")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .clientId("newClient")
                .clientSecret("newClientSecret")
                .redirectUri("http://localhost:8080/login")
                .authorizationUri("http://localhost:8080/new-client/login/oauth2/code/custom")
                .tokenUri("http://localhost:8080/new-client/login/oauth2/code/custom")
                .build();
    }

}
