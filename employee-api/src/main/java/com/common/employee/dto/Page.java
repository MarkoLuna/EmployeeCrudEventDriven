package com.common.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Page<T> {
    private int pageNumber;
    private int pageSize;
    long offset;
    Sort sort;
    T content;
}

enum Sort {
    DESCENDING, ASCENDING;
}

