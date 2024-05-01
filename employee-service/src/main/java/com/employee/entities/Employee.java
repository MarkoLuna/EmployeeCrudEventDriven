package com.employee.entities;

import com.employee.enums.EmployeeStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;

@Data
@ToString
@Entity
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    private String firstName;
    private String middleInitial;
    private String lastName;
    private LocalDate dateOfBirth;
    private LocalDate dateOfEmployment;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private EmployeeStatus status;
}
