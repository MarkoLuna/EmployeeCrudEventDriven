package com.users.services;

import com.users.dto.UserCreateRequest;
import com.users.dto.UserPage;
import com.users.dto.UserResponse;
import com.users.dto.UserUpdateRequest;

public interface UserService {

  void createUser(UserCreateRequest request);

  UserResponse getUserById(String id);

  UserResponse getUserByUsername(String username);

  UserPage getAllUsers(Integer page, Integer size);

  void updateUser(String id, UserUpdateRequest request);

  void deleteUser(String id);
}
