package com.users.services;

import com.users.clients.UserClient;
import com.users.dto.UserCreateRequest;
import com.users.dto.UserPage;
import com.users.dto.UserResponse;
import com.users.dto.UserUpdateRequest;
import com.users.exceptions.IamServiceException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserClient userClient;

  @Value("${app.keycloak.realm}")
  private String defaultRealm;

  @Override
  public void createUser(UserCreateRequest request) {
    String realm = extractRealm();
    log.info("Creating user: {} in realm: {}", request.getUsername(), realm);
    userClient.createUser(realm, request);
  }

  @Override
  public UserResponse getUserById(String id) {
    String realm = extractRealm();
    log.info("Getting user by ID: {} in realm: {}", id, realm);
    return userClient.getUserById(realm, id);
  }

  @Override
  public UserResponse getUserByUsername(String username) {
    String realm = extractRealm();
    log.info("Getting user by username: {} in realm: {}", username, realm);
    var users = userClient.getUserByUsername(realm, username);
    if (users.isEmpty()) {
      throw new IamServiceException(HttpStatus.NOT_FOUND, "User not found: " + username);
    }
    return users.get(0);
  }

  @Override
  public UserPage getAllUsers(Integer page, Integer size) {
    String realm = extractRealm();
    int first = page * size;
    log.info("Getting all users in realm: {} - first: {}, max: {}", realm, first, size);
    List<UserResponse> users = userClient.getAllUsers(realm, first, size);
    return UserPage.builder().pageNumber(page).pageSize(size).offset(first).content(users).build();
  }

  @Override
  public void updateUser(String id, UserUpdateRequest request) {
    String realm = extractRealm();
    log.info("Updating user: {} in realm: {}", id, realm);
    userClient.updateUser(realm, id, request);
  }

  @Override
  public void deleteUser(String id) {
    String realm = extractRealm();
    log.info("Deleting user: {} in realm: {}", id, realm);
    userClient.deleteUser(realm, id);
  }

  private String extractRealm() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth instanceof JwtAuthenticationToken jwtAuth) {
      String issuer = jwtAuth.getToken().getClaimAsString("iss");
      if (issuer != null && issuer.contains("/realms/")) {
        return issuer.substring(issuer.lastIndexOf('/') + 1);
      }
    }
    return defaultRealm;
  }
}
