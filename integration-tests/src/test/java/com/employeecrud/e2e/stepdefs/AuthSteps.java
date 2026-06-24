package com.employeecrud.e2e.stepdefs;

import static io.restassured.RestAssured.given;

import com.employeecrud.e2e.config.E2EProperties;
import com.employeecrud.e2e.model.TestContext;
import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthSteps {

  private final TestContext context;
  private final E2EProperties properties;

  @Given("I am authenticated as {string} with password {string}")
  public void authenticate(String username, String password) {
    var kc = properties.getKeycloak();
    var response =
        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("client_id", kc.getClientId())
            .formParam("client_secret", kc.getClientSecret())
            .formParam("username", username)
            .formParam("password", password)
            .formParam("grant_type", "password")
            .when()
            .post(kc.getTokenUrl())
            .then()
            .extract()
            .response();

    var token = response.jsonPath().getString("access_token");
    context.setAuthToken(token);
  }

  @Given("I am not authenticated")
  public void clearAuthentication() {
    context.setAuthToken(null);
  }
}
