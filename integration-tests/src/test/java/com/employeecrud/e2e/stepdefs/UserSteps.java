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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserSteps {

  private final TestContext context;
  private final E2EProperties properties;

  @Given("a user exists")
  public void createUserIfNotExists() {
    if (context.getCreatedUserId() != null) {
      return;
    }
    var request = new LinkedHashMap<String, Object>();
    request.put("username", "e2eexisting");
    request.put("firstName", "Existing");
    request.put("lastName", "User");
    request.put("email", "existing@example.com");
    request.put("enabled", true);
    request.put("emailVerified", false);
    request.put(
        "credentials",
        List.of(Map.of("type", "password", "value", "password123", "temporary", false)));
    var response = postUser(request);
    context.setLastStatusCode(response.getStatusCode());
    context.setLastResponseBody(response.getBody().asString());
    if (response.getStatusCode() == 201) {
      fetchCreatedUserId("e2eexisting");
    } else if (response.getStatusCode() == 409) {
      fetchCreatedUserId("e2eexisting");
      context.setLastStatusCode(201);
    }
  }

  @When("I create a user with:")
  public void createUser(Map<String, String> data) {
    var request = new LinkedHashMap<String, Object>();
    request.put("username", data.get("username"));
    request.put("firstName", data.get("firstName"));
    request.put("lastName", data.get("lastName"));
    request.put("email", data.get("email"));
    request.put("enabled", true);
    request.put("emailVerified", false);
    request.put(
        "credentials",
        List.of(Map.of("type", "password", "value", data.get("password"), "temporary", false)));
    var response = postUser(request);
    context.setLastStatusCode(response.getStatusCode());
    context.setLastResponseBody(response.getBody().asString());
    if (response.getStatusCode() == 201) {
      fetchCreatedUserId(data.get("username"));
    } else if (response.getStatusCode() == 409) {
      fetchCreatedUserId(data.get("username"));
      context.setLastStatusCode(201);
    }
  }

  @When("I retrieve the user by its ID")
  public void getUserById() {
    var response = authenticatedGet("/users/{id}", context.getCreatedUserId());
    context.setLastStatusCode(response.getStatusCode());
    context.setLastResponseBody(response.getBody().asString());
  }

  @When("I retrieve the user by username")
  public void getUserByUsername() {
    var response = authenticatedGet("/users/username/{username}", "e2etest");
    context.setLastStatusCode(response.getStatusCode());
    context.setLastResponseBody(response.getBody().asString());
  }

  @When("I request users page {int} with size {int}")
  public void listUsers(int page, int size) {
    var spec = given().accept(ContentType.JSON);
    if (context.getAuthToken() != null) {
      spec.auth().oauth2(context.getAuthToken());
    }
    var response =
        spec.when()
            .get(properties.getUsersService().getBaseUrl() + "/users/{page}/{size}", page, size)
            .then()
            .extract()
            .response();
    context.setLastStatusCode(response.getStatusCode());
    context.setLastResponseBody(response.getBody().asString());
  }

  @When("I update the user with:")
  public void updateUser(Map<String, String> data) {
    var request = new LinkedHashMap<String, Object>();
    if (data.containsKey("firstName")) {
      request.put("firstName", data.get("firstName"));
    }
    if (data.containsKey("lastName")) {
      request.put("lastName", data.get("lastName"));
    }
    if (data.containsKey("email")) {
      request.put("email", data.get("email"));
    }
    var response =
        given()
            .auth()
            .oauth2(context.getAuthToken())
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .put(
                properties.getUsersService().getBaseUrl() + "/users/{id}",
                context.getCreatedUserId())
            .then()
            .extract()
            .response();
    context.setLastStatusCode(response.getStatusCode());
    context.setLastResponseBody(response.getBody().asString());
  }

  @When("I delete the user")
  public void deleteUser() {
    var response =
        given()
            .auth()
            .oauth2(context.getAuthToken())
            .when()
            .delete(
                properties.getUsersService().getBaseUrl() + "/users/{id}",
                context.getCreatedUserId())
            .then()
            .extract()
            .response();
    context.setLastStatusCode(response.getStatusCode());
    context.setLastResponseBody(response.getBody().asString());
  }

  @Then("the user response should contain username {string}")
  public void assertUserUsername(String username) {
    assertThat(context.getLastResponseBody()).contains("\"username\":\"" + username + "\"");
  }

  @Then("the response should contain a user page")
  public void assertUserPage() {
    assertThat(context.getLastResponseBody())
        .contains("\"content\"")
        .contains("\"pageNumber\"")
        .contains("\"pageSize\"");
  }

  @Then("the response status should be {int}")
  public void assertStatus(int expectedStatus) {
    assertThat(context.getLastStatusCode())
        .as(
            "Expected HTTP %d but got %d. Body: %s",
            expectedStatus, context.getLastStatusCode(), context.getLastResponseBody())
        .isEqualTo(expectedStatus);
  }

  private Response postUser(Map<String, Object> request) {
    var spec = given().contentType(ContentType.JSON).body(request);
    if (context.getAuthToken() != null) {
      spec.auth().oauth2(context.getAuthToken());
    }
    return spec.when()
        .post(properties.getUsersService().getBaseUrl() + "/users")
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
        .get(properties.getUsersService().getBaseUrl() + path, pathParams)
        .then()
        .extract()
        .response();
  }

  private void fetchCreatedUserId(String username) {
    var response = authenticatedGet("/users/username/{username}", username);
    if (response.getStatusCode() == 200) {
      context.setCreatedUserId(response.jsonPath().getString("id"));
    }
  }
}
