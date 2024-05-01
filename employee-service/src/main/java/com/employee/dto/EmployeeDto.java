package com.employee.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record EmployeeDto(
        @Schema(description = "employee id", example = "e26b1b1e-a8d0-11e9-a2a3-2a2ae2dbcce4")
        String id,
        @Schema(description = "employee first name", example = "Jose Marcos")
        String firstName,
        @Schema(description = "employee middle initial", example = "M")
        String middleInitial,
        @Schema(description = "employee last name", example = "Luna")
        String lastName,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy", timezone = "UTC")
        @Schema(description = "employee date of birth", example = "17-09-2012")
        LocalDate dateOfBirth,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy", timezone = "UTC")
        @Schema(description = "employee date of employment", example = "17-09-2012")
        LocalDate dateOfEmployment,
        @Schema(description = "employee status", example = "ACTIVE")
        String status) {
}
