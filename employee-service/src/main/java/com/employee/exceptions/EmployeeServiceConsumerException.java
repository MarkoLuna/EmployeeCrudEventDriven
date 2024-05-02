package com.employee.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeServiceConsumerException extends RuntimeException {
    private int httpStatus;
    private String errorMessage;
}
