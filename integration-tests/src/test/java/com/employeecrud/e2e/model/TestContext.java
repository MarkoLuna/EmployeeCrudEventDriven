package com.employeecrud.e2e.model;

import io.cucumber.spring.ScenarioScope;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@ScenarioScope
@Getter
@Setter
public class TestContext {

  private String authToken;
  private int lastStatusCode;
  private String lastResponseBody;
  private String createdEmployeeId;
  private String createdUserId;
  private String createdEmployeeFirstName;
}
