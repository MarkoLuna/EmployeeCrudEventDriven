/*
 * Copyright (c) 2024 Nextiva, Inc. to Present.
 * All rights reserved.
 */

package com.common.employee.dto;

import java.time.LocalDate;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.common.employee.enums.EmployeeStatus;

/**
 * Unit tests for {@link EmployeeDto}
 */
public class EmployeeDtoTest {
    public static final LocalDate DEFAULT_DATE = LocalDate.of(2024, 05, 15);
    public static final String EMPLOYEE1_FILENAME = "employee1.json";

    public static final EmployeeDto TARGET_EMPLOYEE1 =
            EmployeeDto.builder()
                    .id("id")
                    .dateOfBirth(DEFAULT_DATE)
                    .dateOfEmployment(DEFAULT_DATE)
                    .status(EmployeeStatus.ACTIVE)
                    .firstName("first name")
                    .lastName("last name")
                    .middleInitial("m")
                    .build();

    @Test
    @DisplayName("Serialization EmployeeDto")
    void serialization_test() throws Exception {
        var EMPLOYEE1Json = Commons.readFile("testfiles/" + EMPLOYEE1_FILENAME);
        JSONAssert.assertEquals(EMPLOYEE1Json.orElseThrow(),
                Commons.OBJECT_MAPPER.writeValueAsString(TARGET_EMPLOYEE1), true);
    }

    @Test
    @DisplayName("Deserialization EmployeeDto")
    void deserialization_test() throws Exception {
        var EMPLOYEE1Json = Commons.readFile("testfiles/" + EMPLOYEE1_FILENAME);
        var event = Commons.OBJECT_MAPPER.readValue(EMPLOYEE1Json.orElseThrow(), EmployeeDto.class);
        Assertions.assertThat(event).isNotNull().isEqualTo(TARGET_EMPLOYEE1);
    }
}
