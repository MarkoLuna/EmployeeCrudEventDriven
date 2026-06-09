package com.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
@Schema(description = "User details response")
public class UserResponse {

  @Schema(
      description = "Unique identifier of the user",
      example = "550e8400-e29b-41d4-a716-446655440000")
  private String id;

  @Schema(description = "Username for login", example = "jdoe")
  private String username;

  @Schema(description = "User's first name", example = "John")
  private String firstName;

  @Schema(description = "User's last name", example = "Doe")
  private String lastName;

  @Schema(description = "User's email address", example = "john.doe@example.com")
  private String email;

  @Schema(description = "Whether the user is enabled", example = "true")
  private boolean enabled;

  @Schema(description = "Whether the user's email has been verified", example = "true")
  private boolean emailVerified;

  @Schema(
      description = "Timestamp (epoch millis) when the user was created",
      example = "1574174706812")
  private long createdTimestamp;

  @Schema(
      description = "List of realm roles assigned to the user",
      example = "[\"user\", \"admin\"]")
  private List<String> realmRoles;

  @Schema(description = "Map of client roles assigned to the user")
  private Map<String, List<String>> clientRoles;

  @Schema(description = "Custom user attributes")
  private Map<String, List<String>> attributes;
}
