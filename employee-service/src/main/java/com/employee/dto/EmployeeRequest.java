package com.employee.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record EmployeeRequest(
        @NotBlank(message = "First Name cannot be empty")
        @Schema(description = "employee first name", example = "Jose Marcos")
        String firstName,

        @NotBlank(message = "Middle Initial cannot be empty")
        @Schema(description = "employee middle initial", example = "M")
        String middleInitial,

        @NotBlank(message = "Last Name cannot be empty")
        @Schema(description = "employee last name", example = "Luna")
        String lastName,

        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy", timezone = "UTC")
        @Schema(description = "employee date of birth", example = "17-09-2012")
        LocalDate dateOfBirth,

        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy", timezone = "UTC")
        @Schema(description = "employee date of employment", example = "17-09-2012")
        LocalDate dateOfEmployment) {

}
