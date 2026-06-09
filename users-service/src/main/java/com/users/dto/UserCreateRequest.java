package com.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "Request to create a new user")
public class UserCreateRequest {

  @NotBlank(message = "Username is required")
  @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
  @Schema(
      description = "Username for the new user",
      example = "jdoe",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String username;

  @NotBlank(message = "First name is required")
  @Size(max = 50, message = "First name must not exceed 50 characters")
  @Schema(
      description = "First name of the user",
      example = "John",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String firstName;

  @NotBlank(message = "Last name is required")
  @Size(max = 50, message = "Last name must not exceed 50 characters")
  @Schema(
      description = "Last name of the user",
      example = "Doe",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String lastName;

  @Email(message = "Email should be valid")
  @NotBlank(message = "Email is required")
  @Schema(
      description = "Email address of the user",
      example = "john.doe@example.com",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String email;

  @NotEmpty(message = "At least one credential is required")
  @Schema(
      description = "User credentials (e.g. password)",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private List<Credential> credentials;

  @Schema(description = "Whether the user is enabled", example = "true", defaultValue = "true")
  private boolean enabled = true;

  @Schema(
      description = "Whether the user's email is verified",
      example = "false",
      defaultValue = "false")
  private boolean emailVerified = false;
}
