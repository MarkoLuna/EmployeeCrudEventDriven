package com.common.employee.dto;

import com.common.employee.enums.EmployeeStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Builder;

/** Data Transfer Object for employee information. */
@Builder
public record EmployeeInfo(
    @NotBlank(message = "First Name cannot be empty")
        @Schema(description = "employee first name", example = "Jose Marcos")
        String firstName,
    @Size(max = 1) @Schema(description = "employee middle initial", example = "M")
        String middleInitial,
    @NotBlank(message = "Last Name cannot be empty")
        @Schema(description = "employee last name", example = "Luna")
        String lastName,
    @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
        @Schema(description = "employee date of birth", example = "17-09-2012")
        LocalDate dateOfBirth,
    @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
        @Schema(description = "employee date of employment", example = "17-09-2012")
        LocalDate dateOfEmployment,
    @NotNull @Schema(description = "employee status", example = "ACTIVE") EmployeeStatus status) {}
