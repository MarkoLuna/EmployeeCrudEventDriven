package com.employee.entities;

import com.common.employee.enums.EmployeeStatus;
import java.time.LocalDate;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@ToString
@Document(collection = "employee")
public class Employee {

  @Id private String id;

  private String firstName;
  private String middleInitial;
  private String lastName;
  private LocalDate dateOfBirth;
  private LocalDate dateOfEmployment;

  private EmployeeStatus status;
}
