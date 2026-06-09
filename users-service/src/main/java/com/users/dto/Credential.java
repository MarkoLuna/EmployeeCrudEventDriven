package com.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User credential for Keycloak")
public class Credential {

  @Schema(description = "Credential type", example = "password")
  private String type;

  @Schema(description = "Credential value", example = "password123")
  private String value;

  @Schema(description = "Whether the credential is temporary", example = "false")
  private boolean temporary;
}
