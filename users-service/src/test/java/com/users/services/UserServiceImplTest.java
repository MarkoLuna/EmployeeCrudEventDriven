package com.users.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.users.clients.UserClient;
import com.users.dto.Credential;
import com.users.dto.UserCreateRequest;
import com.users.dto.UserResponse;
import com.users.dto.UserUpdateRequest;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

/** Unit tests for {@link UserServiceImpl} */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  private static final String DEFAULT_REALM = "dev";

  @Mock private UserClient userClient;

  private UserServiceImpl userService;

  @BeforeEach
  public void setUp() {
    this.userService = new UserServiceImpl(userClient);
    ReflectionTestUtils.setField(userService, "defaultRealm", DEFAULT_REALM);

    var jwt =
        Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .claim("iss", "http://localhost:8081/realms/" + DEFAULT_REALM)
            .build();
    SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @DisplayName("Create a new user")
  @Test
  void createUser() {
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

    doNothing().when(userClient).createUser(eq(DEFAULT_REALM), any(UserCreateRequest.class));

    userService.createUser(request);

    verify(userClient).createUser(eq(DEFAULT_REALM), any(UserCreateRequest.class));
  }

  @DisplayName("Get user by ID")
  @Test
  void getUserById() {
    var userId = "user-id-123";
    var expectedResponse = new UserResponse();
    expectedResponse.setId(userId);
    expectedResponse.setUsername("jdoe");
    expectedResponse.setFirstName("John");
    expectedResponse.setLastName("Doe");
    expectedResponse.setEmail("john.doe@example.com");

    when(userClient.getUserById(DEFAULT_REALM, userId)).thenReturn(expectedResponse);

    var result = userService.getUserById(userId);

    assertThat(result).isNotNull().usingRecursiveComparison().isEqualTo(expectedResponse);
    verify(userClient).getUserById(DEFAULT_REALM, userId);
  }

  @DisplayName("Get user by username")
  @Test
  void getUserByUsername() {
    var username = "jdoe";
    var expectedResponse = new UserResponse();
    expectedResponse.setId("user-id-123");
    expectedResponse.setUsername(username);
    expectedResponse.setFirstName("John");
    expectedResponse.setLastName("Doe");
    expectedResponse.setEmail("john.doe@example.com");

    when(userClient.getUserByUsername(DEFAULT_REALM, username))
        .thenReturn(List.of(expectedResponse));

    var result = userService.getUserByUsername(username);

    assertThat(result).isNotNull().usingRecursiveComparison().isEqualTo(expectedResponse);
    verify(userClient).getUserByUsername(DEFAULT_REALM, username);
  }

  @DisplayName("Get all users with pagination")
  @Test
  void getAllUsers() {
    var page = 1;
    var size = 10;
    var first = page * size;

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

    when(userClient.getAllUsers(eq(DEFAULT_REALM), eq(first), eq(size))).thenReturn(expectedUsers);

    var result = userService.getAllUsers(page, size);

    assertThat(result).isNotNull();
    assertThat(result.getPageNumber()).isEqualTo(page);
    assertThat(result.getPageSize()).isEqualTo(size);
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getContent().get(0).getUsername()).isEqualTo("jdoe");
    assertThat(result.getContent().get(1).getUsername()).isEqualTo("asmith");
    verify(userClient).getAllUsers(eq(DEFAULT_REALM), eq(first), eq(size));
  }

  @DisplayName("Update user")
  @Test
  void updateUser() {
    var userId = "user-id-123";
    var request = new UserUpdateRequest();
    request.setFirstName("Johnny");
    request.setLastName("Smith");
    request.setEmail("johnny.smith@example.com");
    request.setEnabled(true);
    request.setEmailVerified(true);

    doNothing()
        .when(userClient)
        .updateUser(eq(DEFAULT_REALM), eq(userId), any(UserUpdateRequest.class));

    userService.updateUser(userId, request);

    verify(userClient).updateUser(eq(DEFAULT_REALM), eq(userId), any(UserUpdateRequest.class));
  }

  @DisplayName("Delete user")
  @Test
  void deleteUser() {
    var userId = "user-id-123";

    doNothing().when(userClient).deleteUser(DEFAULT_REALM, userId);

    userService.deleteUser(userId);

    verify(userClient).deleteUser(DEFAULT_REALM, userId);
  }
}
