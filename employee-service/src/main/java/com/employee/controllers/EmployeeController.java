package com.employee.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.employee.dto.EmployeeDto;
import com.employee.dto.EmployeeRequest;
import com.employee.exceptions.InvalidDataException;
import com.employee.services.EmployeeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/employees", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(
        name = "Employees",
        description = "Set of endpoints for Creating, Retrieving, Updating and Deleting of Employees."
)
@Validated
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/{page}/{size}")
    @Operation(summary = "Returns list of all Employees in the system.")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Page<EmployeeDto>> list(
            @Parameter(required = true, name = "page",
                    description = "Page number of the employee list to be obtained. Cannot be empty.")
            @PathVariable("page") Integer page,
            @Parameter(required = true, name = "size",
                    description = "Page size of the employee list to be obtained. Cannot be empty.")
            @PathVariable("size") Integer pageSize) {
    	
        Page<EmployeeDto> employeeDtoList = employeeService.list(page, pageSize);
        return new ResponseEntity<>(employeeDtoList, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Returns a specific employee by their identifier. 404 if does not exist.")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<EmployeeDto> getEmployee(@Parameter(required = true,  name = "id",
            description = "Id of the employee to be obtained. Cannot be empty.")
                                                       @PathVariable("id") String employeeId) {

        EmployeeDto employee = employeeService.getEmployee(employeeId);
        return new ResponseEntity<>(employee, HttpStatus.OK);
    }

    @PostMapping()
    @Operation(summary = "Creates a new employee.")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<EmployeeDto> saveEmployee(
            @Parameter(description = "Employee information for a new employee to be created.")
            @Valid @RequestBody EmployeeRequest request) {

        EmployeeDto employee = employeeService.createEmployee(request)
                .orElseThrow(() -> new InvalidDataException("Employee already exists .."));
        return new ResponseEntity<>(employee, HttpStatus.OK);
    }

    @PutMapping(value = "/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<EmployeeDto> updateEmployee(@PathVariable("id") String employeeId,
                                                      @Valid @RequestBody EmployeeRequest request) {

        EmployeeDto employee = employeeService.updateEmployee(employeeId, request);
        return new ResponseEntity<>(employee, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletes a employee from the system. 404 if the employee's identifier is not found.")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> deleteEmployee(
            @Parameter(required = true, name = "id",
                    description =  "Id of the employee to be deleted. Cannot be empty.")
            @PathVariable("id") String employeeId) {

        employeeService.deleteEmployee(employeeId);
        return new ResponseEntity<>("Employee Deleted", HttpStatus.OK);
    }
}
