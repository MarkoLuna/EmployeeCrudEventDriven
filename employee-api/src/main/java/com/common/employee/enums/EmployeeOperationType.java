package com.common.employee.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/** Enum to represent the type of operation performed on an employee. */
public enum EmployeeOperationType {
  /** Employee creation operation. */
  @Schema(description = "Employee creation operation")
  CREATE,
  /** Employee update operation. */
  @Schema(description = "Employee update operation")
  UPDATE,
  /** Employee deletion operation. */
  @Schema(description = "Employee deletion operation")
  DELETE
}
