package com.employeecrud.e2e.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "e2e")
@Getter
@Setter
public class E2EProperties {

  private Service employeeService = new Service();
  private Service employeeConsumer = new Service();
  private Service usersService = new Service();
  private Keycloak keycloak = new Keycloak();

  @Getter
  @Setter
  public static class Service {
    private String baseUrl;
  }

  @Getter
  @Setter
  public static class Keycloak {
    private String tokenUrl;
    private String clientId;
    private String clientSecret;
  }
}
