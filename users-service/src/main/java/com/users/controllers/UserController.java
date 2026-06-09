package com.users.controllers;

import com.users.dto.UserCreateRequest;
import com.users.dto.UserPage;
import com.users.dto.UserResponse;
import com.users.dto.UserUpdateRequest;
import com.users.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(
    name = "Users",
    description = "Set of endpoints for Creating, Retrieving, Updating and Deleting of Users.")
@Validated
public class UserController {

  @Autowired private UserService userService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('admin', 'manage-users')")
  @Operation(summary = "Creates a new user.")
  @SecurityRequirement(name = "Bearer Authentication")
  public void createUser(
      @Parameter(description = "User information for a new user to be created.") @Valid @RequestBody
          UserCreateRequest request) {
    userService.createUser(request);
  }

  @GetMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasAnyRole('admin', 'manage-users', 'view-users', 'query-users')")
  @Operation(summary = "Returns a specific user by their identifier. 404 if does not exist.")
  @SecurityRequirement(name = "Bearer Authentication")
  public UserResponse getUserById(
      @Parameter(
              required = true,
              name = "id",
              description = "Id of the user to be obtained. Cannot be empty.")
          @PathVariable("id")
          String userId) {
    return userService.getUserById(userId);
  }

  @GetMapping("/username/{username}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasAnyRole('admin', 'manage-users', 'view-users', 'query-users')")
  @Operation(summary = "Returns a specific user by their username. 404 if does not exist.")
  @SecurityRequirement(name = "Bearer Authentication")
  public UserResponse getUserByUsername(
      @Parameter(
              required = true,
              name = "username",
              description = "Username of the user to be obtained. Cannot be empty.")
          @PathVariable("username")
          String username) {
    return userService.getUserByUsername(username);
  }

  @GetMapping("/{page}/{size}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasAnyRole('admin', 'manage-users', 'view-users', 'query-users')")
  @Operation(summary = "Returns list of all users in the system with pagination.")
  @SecurityRequirement(name = "Bearer Authentication")
  public UserPage getAllUsers(
      @Parameter(required = true, name = "page", description = "Page number for pagination")
          @PathVariable("page")
          Integer page,
      @Parameter(required = true, name = "size", description = "Page size for pagination")
          @PathVariable("size")
          Integer size) {
    return userService.getAllUsers(page, size);
  }

  @PutMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('admin', 'manage-users')")
  @Operation(summary = "Updates a user.")
  @SecurityRequirement(name = "Bearer Authentication")
  public void updateUser(
      @PathVariable("id") String userId, @Valid @RequestBody UserUpdateRequest request) {
    userService.updateUser(userId, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('admin', 'manage-users')")
  @Operation(summary = "Deletes a user from the system. 404 if the user's identifier is not found.")
  @SecurityRequirement(name = "Bearer Authentication")
  public void deleteUser(
      @Parameter(
              required = true,
              name = "id",
              description = "Id of the user to be deleted. Cannot be empty.")
          @PathVariable("id")
          String userId) {
    userService.deleteUser(userId);
  }
}
