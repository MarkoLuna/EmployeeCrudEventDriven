package com.users.clients;

import com.users.dto.UserCreateRequest;
import com.users.dto.UserResponse;
import com.users.dto.UserUpdateRequest;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.List;

@Headers("Content-Type: application/json")
public interface UserClient {

  @RequestLine("POST /admin/realms/{realm}/users")
  void createUser(@Param("realm") String realm, UserCreateRequest request);

  @RequestLine("GET /admin/realms/{realm}/users/{id}")
  UserResponse getUserById(@Param("realm") String realm, @Param("id") String id);

  @RequestLine("GET /admin/realms/{realm}/users?username={username}&exact=true")
  List<UserResponse> getUserByUsername(
      @Param("realm") String realm, @Param(value = "username") String username);

  @RequestLine("GET /admin/realms/{realm}/users?first={first}&max={max}")
  List<UserResponse> getAllUsers(
      @Param("realm") String realm, @Param("first") Integer first, @Param("max") Integer max);

  @RequestLine("PUT /admin/realms/{realm}/users/{id}")
  void updateUser(@Param("realm") String realm, @Param("id") String id, UserUpdateRequest request);

  @RequestLine("DELETE /admin/realms/{realm}/users/{id}")
  void deleteUser(@Param("realm") String realm, @Param("id") String id);
}
