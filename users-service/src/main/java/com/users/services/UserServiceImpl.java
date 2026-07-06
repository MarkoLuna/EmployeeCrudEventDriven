package com.users.services;

import com.users.clients.UserClient;
import com.users.dto.UserCreateRequest;
import com.users.dto.UserPage;
import com.users.dto.UserResponse;
import com.users.dto.UserUpdateRequest;
import com.users.exceptions.IamServiceException;
import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

/**
 * Service for Keycloak user management operations. All CRUD methods call the Keycloak Admin REST
 * API via Feign, protected by a Resilience4j {@link CircuitBreaker} named "keycloak". When the
 * circuit is open, fallback methods throw 503 for mutating operations or return empty pages for
 * list operations.
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserClient userClient;

  @Value("${app.keycloak.realm}")
  private String defaultRealm;

  @Override
  @CircuitBreaker(name = "keycloak", fallbackMethod = "createUserFallback")
  public void createUser(UserCreateRequest request) {
    String realm = extractRealm();
    log.info("Creating user: {} in realm: {}", request.getUsername(), realm);
    userClient.createUser(realm, request);
  }

  /**
   * Fallback for {@link #createUser(UserCreateRequest)}. Returns 503 when the circuit is open or
   * the IAM is unreachable; rethrows the original exception for business errors (e.g. 409
   * Conflict).
   *
   * @param request the original create request
   * @param t the exception that triggered the fallback
   * @throws IamServiceException with status 503 when the circuit is open
   * @throws RuntimeException the original exception for all other cases
   */
  private void createUserFallback(UserCreateRequest request, Throwable t) {
    if (t instanceof CallNotPermittedException || t instanceof RetryableException) {
      log.warn("IAM unavailable in createUser({}), returning 503", request.getUsername(), t);
      throw new IamServiceException(
          HttpStatus.SERVICE_UNAVAILABLE, "IAM service is unavailable, please try again later.");
    }
    log.warn("Error in createUser({}), rethrowing", request.getUsername(), t);
    if (t instanceof RuntimeException) {
      throw (RuntimeException) t;
    }
    throw new RuntimeException(t);
  }

  @Override
  @CircuitBreaker(name = "keycloak", fallbackMethod = "getUserByIdFallback")
  public UserResponse getUserById(String id) {
    String realm = extractRealm();
    log.info("Getting user by ID: {} in realm: {}", id, realm);
    return userClient.getUserById(realm, id);
  }

  /**
   * Fallback for {@link #getUserById(String)}. Returns 503 when the circuit is open or the IAM is
   * unreachable; rethrows the original exception for business errors (e.g. 404 Not Found).
   *
   * @param id the requested user id
   * @param t the exception that triggered the fallback
   * @return never returns normally; always throws
   * @throws IamServiceException with status 503 when the circuit is open
   * @throws RuntimeException the original exception for all other cases
   */
  private UserResponse getUserByIdFallback(String id, Throwable t) {
    if (t instanceof CallNotPermittedException || t instanceof RetryableException) {
      log.warn("IAM unavailable in getUserById({}), returning 503", id, t);
      throw new IamServiceException(
          HttpStatus.SERVICE_UNAVAILABLE, "IAM service is unavailable, please try again later.");
    }
    log.warn("Error in getUserById({}), rethrowing", id, t);
    if (t instanceof RuntimeException) {
      throw (RuntimeException) t;
    }
    throw new RuntimeException(t);
  }

  @Override
  @CircuitBreaker(name = "keycloak", fallbackMethod = "getUserByUsernameFallback")
  public UserResponse getUserByUsername(String username) {
    String realm = extractRealm();
    log.info("Getting user by username: {} in realm: {}", username, realm);
    var users = userClient.getUserByUsername(realm, username);
    if (users.isEmpty()) {
      throw new IamServiceException(HttpStatus.NOT_FOUND, "User not found: " + username);
    }
    return users.get(0);
  }

  /**
   * Fallback for {@link #getUserByUsername(String)}. Returns 503 when the circuit is open or the
   * IAM is unreachable; rethrows the original exception for business errors (e.g. empty result).
   *
   * @param username the requested username
   * @param t the exception that triggered the fallback
   * @return never returns normally; always throws
   * @throws IamServiceException with status 503 when the circuit is open
   * @throws RuntimeException the original exception for all other cases
   */
  private UserResponse getUserByUsernameFallback(String username, Throwable t) {
    if (t instanceof CallNotPermittedException || t instanceof RetryableException) {
      log.warn("IAM unavailable in getUserByUsername({}), returning 503", username, t);
      throw new IamServiceException(
          HttpStatus.SERVICE_UNAVAILABLE, "IAM service is unavailable, please try again later.");
    }
    log.warn("Error in getUserByUsername({}), rethrowing", username, t);
    if (t instanceof RuntimeException) {
      throw (RuntimeException) t;
    }
    throw new RuntimeException(t);
  }

  @Override
  @CircuitBreaker(name = "keycloak", fallbackMethod = "getAllUsersFallback")
  public UserPage getAllUsers(Integer page, Integer size) {
    String realm = extractRealm();
    int first = page * size;
    log.info("Getting all users in realm: {} - first: {}, max: {}", realm, first, size);
    List<UserResponse> users = userClient.getAllUsers(realm, first, size);
    return UserPage.builder().pageNumber(page).pageSize(size).offset(first).content(users).build();
  }

  /**
   * Fallback for {@link #getAllUsers(Integer, Integer)}. Triggered when the circuit breaker is open
   * or a Feign exception occurs. Returns an empty user page.
   *
   * @param page the requested page number
   * @param size the requested page size
   * @param t the exception that triggered the fallback
   * @return an empty {@link UserPage}
   */
  private UserPage getAllUsersFallback(Integer page, Integer size, Throwable t) {
    log.warn("Circuit breaker or Feign exception in getAllUsers({}, {})", page, size, t);
    return UserPage.builder().pageNumber(page).pageSize(size).offset(0).content(List.of()).build();
  }

  @Override
  @CircuitBreaker(name = "keycloak", fallbackMethod = "updateUserFallback")
  public void updateUser(String id, UserUpdateRequest request) {
    String realm = extractRealm();
    log.info("Updating user: {} in realm: {}", id, realm);
    userClient.updateUser(realm, id, request);
  }

  /**
   * Fallback for {@link #updateUser(String, UserUpdateRequest)}. Returns 503 when the circuit is
   * open or the IAM is unreachable; rethrows the original exception for business errors.
   *
   * @param id the user id
   * @param request the update request
   * @param t the exception that triggered the fallback
   * @throws IamServiceException with status 503 when the circuit is open
   * @throws RuntimeException the original exception for all other cases
   */
  private void updateUserFallback(String id, UserUpdateRequest request, Throwable t) {
    if (t instanceof CallNotPermittedException || t instanceof RetryableException) {
      log.warn("IAM unavailable in updateUser({}), returning 503", id, t);
      throw new IamServiceException(
          HttpStatus.SERVICE_UNAVAILABLE, "IAM service is unavailable, please try again later.");
    }
    log.warn("Error in updateUser({}), rethrowing", id, t);
    if (t instanceof RuntimeException) {
      throw (RuntimeException) t;
    }
    throw new RuntimeException(t);
  }

  @Override
  @CircuitBreaker(name = "keycloak", fallbackMethod = "deleteUserFallback")
  public void deleteUser(String id) {
    String realm = extractRealm();
    log.info("Deleting user: {} in realm: {}", id, realm);
    userClient.deleteUser(realm, id);
  }

  /**
   * Fallback for {@link #deleteUser(String)}. Returns 503 when the circuit is open or the IAM is
   * unreachable; rethrows the original exception for business errors.
   *
   * @param id the user id to delete
   * @param t the exception that triggered the fallback
   * @throws IamServiceException with status 503 when the circuit is open
   * @throws RuntimeException the original exception for all other cases
   */
  private void deleteUserFallback(String id, Throwable t) {
    if (t instanceof CallNotPermittedException || t instanceof RetryableException) {
      log.warn("IAM unavailable in deleteUser({}), returning 503", id, t);
      throw new IamServiceException(
          HttpStatus.SERVICE_UNAVAILABLE, "IAM service is unavailable, please try again later.");
    }
    log.warn("Error in deleteUser({}), rethrowing", id, t);
    if (t instanceof RuntimeException) {
      throw (RuntimeException) t;
    }
    throw new RuntimeException(t);
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
