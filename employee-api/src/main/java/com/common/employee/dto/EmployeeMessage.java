package com.common.employee.dto;

import com.common.employee.enums.EmployeeOperationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/** Data Transfer Object for an employee message. */
@Builder
public record EmployeeMessage(
    @Schema(description = "employee id", example = "e26b1b1e-a8d0-11e9-a2a3-2a2ae2dbcce4")
        String employeeId,
    @Schema(description = "employee info", example = "Jose Marcos Luna") EmployeeInfo employee,
    @Schema(description = "employee operation type", example = "CREATE")
        EmployeeOperationType operationType) {}
