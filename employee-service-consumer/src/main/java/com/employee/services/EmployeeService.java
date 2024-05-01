package com.employee.services;

import com.employee.dto.EmployeeDto;
import com.employee.dto.EmployeeRequest;
import com.employee.entities.Employee;
import com.employee.enums.EmployeeStatus;
import com.employee.exceptions.EmployeeNotFound;
import com.employee.mappers.EmployeeMapper;
import com.employee.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeMapper employeeMapper;

    public Page<EmployeeDto> list(Integer page, Integer sizePage) {
        Sort orders = Sort.by(Sort.Direction.DESC, "dateOfEmployment");
        Page<Employee> employeeList = employeeRepository.findByStatus(EmployeeStatus.ACTIVE, PageRequest.of(page, sizePage, orders));
        return employeeList.map(employeeMapper::convert);
    }

    public EmployeeDto getEmployee(String employeeId) throws EmployeeNotFound {
        Optional<Employee> employee = employeeRepository.findByIdAndStatus(employeeId, EmployeeStatus.ACTIVE);
        return employeeMapper.convert(employee.orElseThrow(() -> new EmployeeNotFound("Unable to find the Employee")));
    }

    public Optional<EmployeeDto> createEmployee(EmployeeRequest req) {
        List<Employee> existanEmployee = employeeRepository.findByFirstNameAndMiddleInitialAndLastNameAndStatus(
                req.firstName(), req.middleInitial(), req.lastName(), EmployeeStatus.ACTIVE);

        if(!existanEmployee.isEmpty())
            return Optional.empty();

        Employee employee = employeeMapper.convert(req);
        employee.setStatus(EmployeeStatus.ACTIVE);
        employeeRepository.save(employee);
        EmployeeDto employeeDto = employeeMapper.convert(employee);
        return Optional.of(employeeDto);
    }

    public EmployeeDto updateEmployee(String id, EmployeeRequest emplReq) throws EmployeeNotFound {

        Optional<Employee> existingEmployee = employeeRepository.findByIdAndStatus(id, EmployeeStatus.ACTIVE);
        Employee employee = existingEmployee.orElseThrow(() -> new EmployeeNotFound("Unable to find the employee"));

        employee.setFirstName(emplReq.firstName());
        employee.setLastName(emplReq.lastName());
        employee.setMiddleInitial(emplReq.middleInitial());
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setDateOfBirth(emplReq.dateOfBirth());
        employee.setDateOfEmployment(emplReq.dateOfEmployment());

        employeeRepository.save(employee);
        return employeeMapper.convert(employee);
    }

    public void deleteEmployee(String id) throws EmployeeNotFound {
        Optional<Employee> existanEmployee = employeeRepository.findByIdAndStatus(id, EmployeeStatus.ACTIVE);
        Employee employee = existanEmployee.orElseThrow(() -> new EmployeeNotFound("Unable to find the employee"));
        employeeRepository.deleteById(employee.getId());
    }
}
