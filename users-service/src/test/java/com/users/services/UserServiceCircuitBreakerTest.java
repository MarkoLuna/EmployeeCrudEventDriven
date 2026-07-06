package com.users.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.users.UsersServiceApplication;
import com.users.clients.UserClient;
import com.users.dto.UserCreateRequest;
import com.users.dto.UserUpdateRequest;
import com.users.exceptions.IamServiceException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Disabled("Requires running Keycloak infrastructure")
@SpringBootTest(classes = {UsersServiceApplication.class})
@DisplayName("Users Service Circuit Breaker Integration Tests")
@ActiveProfiles("test")
class UserServiceCircuitBreakerTest {

  @Autowired private UserService userService;

  @MockitoBean private UserClient userClient;

  @DisplayName("getUserById() returns fallback 503 when circuit breaker opens")
  @Test
  void getUserById_shouldReturn503_whenCircuitBreakerOpens() {
    when(userClient.getUserById(anyString(), anyString()))
        .thenThrow(new IamServiceException(HttpStatus.SERVICE_UNAVAILABLE, "iam unavailable"));

    for (int i = 0; i < 5; i++) {
      assertThatThrownBy(() -> userService.getUserById("test-id"))
          .isInstanceOf(IamServiceException.class);
    }

    assertThatThrownBy(() -> userService.getUserById("test-id"))
        .isInstanceOf(IamServiceException.class)
        .hasMessageContaining("unavailable");
  }

  @DisplayName("getAllUsers() returns fallback empty page when circuit breaker opens")
  @Test
  void getAllUsers_shouldReturnEmptyPage_whenCircuitBreakerOpens() {
    when(userClient.getAllUsers(anyString(), anyInt(), anyInt()))
        .thenThrow(new IamServiceException(HttpStatus.SERVICE_UNAVAILABLE, "iam unavailable"));

    for (int i = 0; i < 5; i++) {
      assertThatThrownBy(() -> userService.getAllUsers(0, 10))
          .isInstanceOf(IamServiceException.class);
    }

    var result = userService.getAllUsers(0, 10);
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEmpty();
  }

  @DisplayName("createUser() returns fallback 503 when circuit breaker opens")
  @Test
  void createUser_shouldReturn503_whenCircuitBreakerOpens() {
    doThrow(new IamServiceException(HttpStatus.SERVICE_UNAVAILABLE, "iam unavailable"))
        .when(userClient)
        .createUser(anyString(), any());

    for (int i = 0; i < 5; i++) {
      assertThatThrownBy(() -> userService.createUser(new UserCreateRequest()))
          .isInstanceOf(IamServiceException.class);
    }

    assertThatThrownBy(() -> userService.createUser(new UserCreateRequest()))
        .isInstanceOf(IamServiceException.class)
        .hasMessageContaining("unavailable");
  }

  @DisplayName("updateUser() returns fallback 503 when circuit breaker opens")
  @Test
  void updateUser_shouldReturn503_whenCircuitBreakerOpens() {
    doThrow(new IamServiceException(HttpStatus.SERVICE_UNAVAILABLE, "iam unavailable"))
        .when(userClient)
        .updateUser(anyString(), anyString(), any());

    for (int i = 0; i < 5; i++) {
      assertThatThrownBy(() -> userService.updateUser("test-id", new UserUpdateRequest()))
          .isInstanceOf(IamServiceException.class);
    }

    assertThatThrownBy(() -> userService.updateUser("test-id", new UserUpdateRequest()))
        .isInstanceOf(IamServiceException.class)
        .hasMessageContaining("unavailable");
  }

  @DisplayName("deleteUser() returns fallback 503 when circuit breaker opens")
  @Test
  void deleteUser_shouldReturn503_whenCircuitBreakerOpens() {
    doThrow(new IamServiceException(HttpStatus.SERVICE_UNAVAILABLE, "iam unavailable"))
        .when(userClient)
        .deleteUser(anyString(), anyString());

    for (int i = 0; i < 5; i++) {
      assertThatThrownBy(() -> userService.deleteUser("test-id"))
          .isInstanceOf(IamServiceException.class);
    }

    assertThatThrownBy(() -> userService.deleteUser("test-id"))
        .isInstanceOf(IamServiceException.class)
        .hasMessageContaining("unavailable");
  }
}
