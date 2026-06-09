package com.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
@Schema(description = "Request to update an existing user")
public class UserUpdateRequest {

  @Size(max = 50, message = "First name must not exceed 50 characters")
  @Schema(description = "New first name", example = "Johnny")
  private String firstName;

  @Size(max = 50, message = "Last name must not exceed 50 characters")
  @Schema(description = "New last name", example = "Smith")
  private String lastName;

  @Email(message = "Email should be valid")
  @Schema(description = "New email address", example = "johnny.smith@example.com")
  private String email;

  @Schema(description = "Update enabled status", example = "true")
  private Boolean enabled;

  @Schema(description = "Update email verified status", example = "true")
  private Boolean emailVerified;

  @Schema(description = "New credentials (e.g. password reset)")
  private List<Credential> credentials;

  @Schema(description = "Update user attributes")
  private Map<String, List<String>> attributes;
}
