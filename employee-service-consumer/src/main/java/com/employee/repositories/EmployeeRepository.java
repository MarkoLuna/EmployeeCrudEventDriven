package com.employee.repositories;

import com.common.employee.enums.EmployeeStatus;
import com.employee.entities.Employee;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EmployeeRepository extends MongoRepository<Employee, String> {

  Page<Employee> findAll(Pageable pageable);

  Page<Employee> findByStatus(EmployeeStatus status, Pageable pageable);

  Optional<Employee> findByIdAndStatus(String id, EmployeeStatus status);

  List<Employee> findByFirstNameAndMiddleInitialAndLastNameAndStatus(
      String firstName, String middleInitial, String lastName, EmployeeStatus status);
}
