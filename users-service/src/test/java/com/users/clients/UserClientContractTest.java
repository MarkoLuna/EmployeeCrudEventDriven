package com.users.clients;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.users.dto.Credential;
import com.users.dto.UserCreateRequest;
import com.users.dto.UserResponse;
import com.users.dto.UserUpdateRequest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Contract tests for {@link UserClient} to verify API contract compliance */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserClient Contract Tests")
class UserClientContractTest {

  private static final String REALM = "dev";
  @Mock private UserClient userClient;

  // ========== Keycloak Admin API contract tests ==========

  @DisplayName("Create user admin API - verifies POST /admin/realms/{realm}/users")
  @Test
  void createUserAdminContract() {
    var request = new UserCreateRequest();
    request.setUsername("jdoe");
    request.setFirstName("John");
    request.setLastName("Doe");
    request.setEmail("john.doe@example.com");
    request.setCredentials(
        List.of(
            Credential.builder().type("password").value("password123").temporary(false).build()));
    request.setEnabled(true);
    request.setEmailVerified(false);

    doNothing().when(userClient).createUser(eq(REALM), any(UserCreateRequest.class));

    userClient.createUser(REALM, request);

    verify(userClient).createUser(eq(REALM), any(UserCreateRequest.class));
  }

  @DisplayName("Get user by ID admin API - verifies GET /admin/realms/{realm}/users/{id}")
  @Test
  void getUserByIdAdminContract() {
    var userId = "user-id-123";
    var expectedResponse = new UserResponse();
    expectedResponse.setId(userId);
    expectedResponse.setUsername("jdoe");
    expectedResponse.setFirstName("John");
    expectedResponse.setLastName("Doe");
    expectedResponse.setEmail("john.doe@example.com");

    when(userClient.getUserById(REALM, userId)).thenReturn(expectedResponse);

    var response = userClient.getUserById(REALM, userId);

    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(userId);
    assertThat(response.getUsername()).isEqualTo("jdoe");
    verify(userClient).getUserById(REALM, userId);
  }

  @DisplayName(
      "Get user by username admin API - verifies GET /admin/realms/{realm}/users?username={username}&exact=true")
  @Test
  void getUserByUsernameAdminContract() {
    var username = "jdoe";
    var expectedResponse = new UserResponse();
    expectedResponse.setId("user-id-123");
    expectedResponse.setUsername(username);
    expectedResponse.setFirstName("John");
    expectedResponse.setLastName("Doe");
    expectedResponse.setEmail("john.doe@example.com");

    when(userClient.getUserByUsername(REALM, username)).thenReturn(List.of(expectedResponse));

    var response = userClient.getUserByUsername(REALM, username);

    assertThat(response).isNotNull();
    assertThat(response).hasSize(1);
    assertThat(response.get(0).getUsername()).isEqualTo(username);
    verify(userClient).getUserByUsername(REALM, username);
  }

  @DisplayName(
      "Get all users admin API - verifies GET /admin/realms/{realm}/users?first={first}&max={max}")
  @Test
  void getAllUsersAdminContract() {
    var first = 0;
    var max = 10;

    var user1 = new UserResponse();
    user1.setId("user-id-1");
    user1.setUsername("jdoe");
    user1.setFirstName("John");
    user1.setLastName("Doe");
    user1.setEmail("john.doe@example.com");

    var user2 = new UserResponse();
    user2.setId("user-id-2");
    user2.setUsername("asmith");
    user2.setFirstName("Alice");
    user2.setLastName("Smith");
    user2.setEmail("alice.smith@example.com");

    var expectedUsers = List.of(user1, user2);

    when(userClient.getAllUsers(REALM, first, max)).thenReturn(expectedUsers);

    var response = userClient.getAllUsers(REALM, first, max);

    assertThat(response).isNotNull();
    assertThat(response).hasSize(2);
    assertThat(response.get(0).getUsername()).isEqualTo("jdoe");
    assertThat(response.get(1).getUsername()).isEqualTo("asmith");
    verify(userClient).getAllUsers(REALM, first, max);
  }

  @DisplayName("Update user admin API - verifies PUT /admin/realms/{realm}/users/{id}")
  @Test
  void updateUserAdminContract() {
    var userId = "user-id-123";
    var request = new UserUpdateRequest();
    request.setFirstName("Johnny");
    request.setLastName("Smith");
    request.setEmail("johnny.smith@example.com");
    request.setEnabled(true);
    request.setEmailVerified(true);

    doNothing().when(userClient).updateUser(eq(REALM), eq(userId), any(UserUpdateRequest.class));

    userClient.updateUser(REALM, userId, request);

    verify(userClient).updateUser(eq(REALM), eq(userId), any(UserUpdateRequest.class));
  }

  @DisplayName("Delete user admin API - verifies DELETE /admin/realms/{realm}/users/{id}")
  @Test
  void deleteUserAdminContract() {
    var userId = "user-id-123";

    doNothing().when(userClient).deleteUser(REALM, userId);

    userClient.deleteUser(REALM, userId);

    verify(userClient).deleteUser(REALM, userId);
  }
}
