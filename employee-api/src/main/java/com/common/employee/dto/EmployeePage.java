package com.common.employee.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeePage {
    private int pageNumber;
    private int pageSize;
    long offset;
    Sort sort;
    List<EmployeeDto> content;
}

enum Sort {
    DESCENDING, ASCENDING;
}

