package com.common.employee.dto;

import java.util.List;

import com.common.employee.enums.Sort;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeePage {
    private int pageNumber;
    private int pageSize;
    long offset;
    Sort sort;
    List<EmployeeDto> content;
}

