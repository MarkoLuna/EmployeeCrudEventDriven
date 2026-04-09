package com.common.employee.dto;

import com.common.employee.enums.EmployeeOperationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record EmployeeMessage(
    @Schema(description = "employee id") String employeeId,
    @Schema(description = "employee info") EmployeeInfo employee,
    @Schema(description = "employee operation type", example = "CREATE")
        EmployeeOperationType operationType) {}
