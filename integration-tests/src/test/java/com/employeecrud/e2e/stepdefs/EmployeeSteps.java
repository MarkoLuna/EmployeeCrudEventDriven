package com.employeecrud.e2e.stepdefs;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.employeecrud.e2e.config.E2EProperties;
import com.employeecrud.e2e.model.TestContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EmployeeSteps {

  private final TestContext context;
  private final E2EProperties properties;

  @Given("an employee exists")
  public void createEmployeeIfNotExists() {
    if (context.getCreatedEmployeeId() != null) {
      return;
    }
    var request = new LinkedHashMap<String, Object>();
    request.put("firstName", "E2EExisting");
    request.put("middleInitial", "T");
    request.put("lastName", "Employee");
    request.put("dateOfBirth", "17-09-2012");
    request.put("dateOfEmployment", "17-09-2014");
    request.put("status", "ACTIVE");
    var response = postEmployee(request);
    context.setLastStatusCode(response.getStatusCode());
    context.setLastResponseBody(response.getBody().asString());
    context.setCreatedEmployeeFirstName("E2EExisting");
    fetchCreatedEmployeeId("E2EExisting");
  }

  @When("I create an employee with:")
  public void createEmployee(Map<String, String> data) {
    var request = buildEmployeeRequest(data);
    var response = postEmployee(request);
    context.setLastStatusCode(response.getStatusCode());
    context.setLastResponseBody(response.getBody().asString());
    if (response.getStatusCode() == 202) {
      context.setCreatedEmployeeFirstName(data.get("firstName"));
      fetchCreatedEmployeeId(data.get("firstName"));
    }
  }

  @When("I retrieve the employee by its ID")
  public void getEmployeeById() {
    var response = authenticatedGet("/employees/{id}", context.getCreatedEmployeeId());
    context.setLastStatusCode(response.getStatusCode());
    context.setLastResponseBody(response.getBody().asString());
  }

  @When("I retrieve employee with ID {string}")
  public void getEmployeeById(String employeeId) {
    var response = authenticatedGet("/employees/{id}", employeeId);
    context.setLastStatusCode(response.getStatusCode());
    context.setLastResponseBody(response.getBody().asString());
  }

  @When("I request employees page {int} with size {int}")
  public void listEmployees(int page, int size) {
    var spec = given().accept(ContentType.JSON);
    if (context.getAuthToken() != null) {
      spec.auth().oauth2(context.getAuthToken());
    }
    var response =
        spec.when()
            .get(
                properties.getEmployeeService().getBaseUrl() + "/employees/{page}/{size}",
                page,
                size)
            .then()
            .extract()
            .response();
    context.setLastStatusCode(response.getStatusCode());
    context.setLastResponseBody(response.getBody().asString());
  }

  @When("I update the employee with:")
  public void updateEmployee(Map<String, String> data) {
    var response =
        given()
            .auth()
            .oauth2(context.getAuthToken())
            .contentType(ContentType.JSON)
            .body(buildEmployeeRequest(data))
            .when()
            .put(
                properties.getEmployeeService().getBaseUrl() + "/employees/{id}",
                context.getCreatedEmployeeId())
            .then()
            .extract()
            .response();
    context.setLastStatusCode(response.getStatusCode());
    context.setLastResponseBody(response.getBody().asString());
  }

  @When("I delete the employee")
  public void deleteEmployee() {
    var response =
        given()
            .auth()
            .oauth2(context.getAuthToken())
            .when()
            .delete(
                properties.getEmployeeService().getBaseUrl() + "/employees/{id}",
                context.getCreatedEmployeeId())
            .then()
            .extract()
            .response();
    context.setLastStatusCode(response.getStatusCode());
    context.setLastResponseBody(response.getBody().asString());
  }

  @Then("the employee response should contain firstName {string}")
  public void assertEmployeeFirstName(String firstName) {
    assertThat(context.getLastResponseBody()).contains("\"firstName\":\"" + firstName + "\"");
  }

  @Then("the employee data should match the created employee")
  public void assertEmployeeDataMatches() {
    assertThat(context.getLastResponseBody())
        .contains("\"firstName\":\"" + context.getCreatedEmployeeFirstName() + "\"");
  }

  @Then("the response should contain an employee page")
  public void assertEmployeePage() {
    assertThat(context.getLastResponseBody())
        .contains("\"content\"")
        .contains("\"pageNumber\"")
        .contains("\"pageSize\"");
  }

  private void fetchCreatedEmployeeId(String firstName) {
    var deadline = System.currentTimeMillis() + Duration.ofSeconds(10).toMillis();
    while (System.currentTimeMillis() < deadline) {
      var response =
          given()
              .accept(ContentType.JSON)
              .auth()
              .oauth2(context.getAuthToken())
              .when()
              .get(properties.getEmployeeService().getBaseUrl() + "/employees/0/10")
              .then()
              .extract()
              .response();
      var ids = response.jsonPath().getList("content.id");
      var firstNames = response.jsonPath().getList("content.firstName");
      if (firstNames != null) {
        for (int i = 0; i < firstNames.size(); i++) {
          if (firstName.equals(firstNames.get(i))) {
            context.setCreatedEmployeeId((String) ids.get(i));
            return;
          }
        }
      }
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
    }
  }

  private Response postEmployee(Map<String, Object> request) {
    var spec = given().contentType(ContentType.JSON).body(request);
    if (context.getAuthToken() != null) {
      spec.auth().oauth2(context.getAuthToken());
    }
    return spec.when()
        .post(properties.getEmployeeService().getBaseUrl() + "/employees")
        .then()
        .extract()
        .response();
  }

  private Response authenticatedGet(String path, Object... pathParams) {
    return given()
        .auth()
        .oauth2(context.getAuthToken())
        .accept(ContentType.JSON)
        .when()
        .get(properties.getEmployeeService().getBaseUrl() + path, pathParams)
        .then()
        .extract()
        .response();
  }

  private static Map<String, Object> buildEmployeeRequest(Map<String, String> data) {
    var request = new LinkedHashMap<String, Object>();
    request.put("firstName", data.getOrDefault("firstName", "Default"));
    request.put("middleInitial", data.getOrDefault("middleInitial", "M"));
    request.put("lastName", data.getOrDefault("lastName", "Employee"));
    request.put("dateOfBirth", data.getOrDefault("dateOfBirth", "17-09-2012"));
    request.put("dateOfEmployment", data.getOrDefault("dateOfEmployment", "17-09-2014"));
    request.put("status", "ACTIVE");
    return request;
  }
}
