package com.employee.services;

import com.common.employee.dto.EmployeeDto;
import com.common.employee.dto.EmployeeInfo;
import com.common.employee.dto.EmployeePage;
import com.common.employee.enums.EmployeeStatus;
import com.common.employee.exceptions.EmployeeNotFound;
import com.employee.entities.Employee;
import com.employee.mappers.EmployeeMapper;
import com.employee.repositories.EmployeeRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeService {

  private final EmployeeRepository employeeRepository;

  private final EmployeeMapper employeeMapper;

  public EmployeePage list(Integer page, Integer sizePage) {
    Sort orders = Sort.by(Sort.Direction.DESC, "dateOfEmployment");
    Page<Employee> employeeList =
        employeeRepository.findByStatus(
            EmployeeStatus.ACTIVE, PageRequest.of(page, sizePage, orders));

    return EmployeePage.builder()
        .content(employeeList.map(employeeMapper::convert).getContent())
        .pageSize(employeeList.getSize())
        .build();
  }

  public EmployeeDto getEmployee(String employeeId) throws EmployeeNotFound {
    return employeeRepository
        .findByIdAndStatus(employeeId, EmployeeStatus.ACTIVE)
        .map(employeeMapper::convert)
        .orElseThrow(() -> new EmployeeNotFound("Unable to find the Employee"));
  }

  public Optional<EmployeeDto> createEmployee(EmployeeInfo req) {
    Employee employee = employeeMapper.convert(req);
    employeeRepository.save(employee);
    return Optional.of(employeeMapper.convert(employee));
  }

  public EmployeeDto updateEmployee(String id, EmployeeInfo emplReq) throws EmployeeNotFound {

    Employee employee =
        employeeRepository
            .findByIdAndStatus(id, EmployeeStatus.ACTIVE)
            .orElseThrow(() -> new EmployeeNotFound("Unable to find the employee"));

    employee.setFirstName(emplReq.firstName());
    employee.setLastName(emplReq.lastName());
    employee.setMiddleInitial(emplReq.middleInitial());
    employee.setStatus(emplReq.status());
    employee.setDateOfBirth(emplReq.dateOfBirth());
    employee.setDateOfEmployment(emplReq.dateOfEmployment());

    employeeRepository.save(employee);
    return employeeMapper.convert(employee);
  }

  public void deleteEmployee(String id) throws EmployeeNotFound {
    Employee employee =
        employeeRepository
            .findByIdAndStatus(id, EmployeeStatus.ACTIVE)
            .orElseThrow(() -> new EmployeeNotFound("Unable to find the employee"));
    employee.setStatus(EmployeeStatus.INACTIVE);
    employeeRepository.save(employee);
  }

  public boolean employeeMatch(String id, EmployeeInfo employee) {
    Employee employeeEntity = employeeMapper.convert(employee);
    employeeEntity.setId(id);

    return employeeRepository
        .findByIdAndStatus(id, employee.status())
        .map(employeeEntity::equals)
        .orElse(false);
  }

  public boolean employeeMatch(String id, EmployeeStatus status) {
    return employeeRepository.findByIdAndStatus(id, status).isPresent();
  }
}
