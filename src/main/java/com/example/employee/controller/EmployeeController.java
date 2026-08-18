package com.example.employee.controller;


import com.example.employee.dto.EmployeeRequestDTO;
import com.example.employee.dto.EmployeeResponseDTO;
import com.example.employee.model.Employee;
import com.example.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService){
      this.employeeService=employeeService;
    }

    @Operation(summary = "Get all employees with pagination",
            description = "Returns a paginated list of employees")
    @GetMapping
    public ResponseEntity<Page<EmployeeResponseDTO>> get(@ParameterObject
                                                             Pageable pageable){
        return ResponseEntity.ok(employeeService.getEmployeeList(pageable));
    }

    @Operation(summary = "Search employees by name",
            description = "Returns a list of employees matching the provided name")
    @GetMapping("/search")
    public ResponseEntity<Page<EmployeeResponseDTO>> search(@Parameter(description = "Employee name")
                                                                @RequestParam String name, @ParameterObject Pageable pageable) {
        Page<EmployeeResponseDTO> employees = employeeService.searchEmployeesByName(name, pageable);
        return ResponseEntity.ok(employees);
    }

    @Operation(summary = "Get employee by ID",
            description = "Returns the employee with the specified ID")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getById(@Parameter(description = "Employee ID")
                                                           @PathVariable Long id) {
        EmployeeResponseDTO employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @Operation(summary = "Get employees by department",
            description = "Returns a list of employees in the specified department")
    @GetMapping("/department/{dept}")
    public ResponseEntity<List<EmployeeResponseDTO>> getByDept(@Parameter(description = "Department name")
                                                                   @PathVariable String dept){
        List<EmployeeResponseDTO> employees = employeeService.getEmployeeByDept(dept);
        return ResponseEntity.ok(employees);
    }

    @Operation(summary = "Create a new employee",
            description = "Creates a new employee with the provided details")
    @PostMapping
    public ResponseEntity<String> create(@Valid @Parameter(description = "Employee details")
                                             @RequestBody EmployeeRequestDTO employee){
        employeeService.createEmployee(employee);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Employee created");
    }

    @Operation(summary = "Update an existing employee",
            description = "Updates the employee with the specified ID")
    @PutMapping("/{id}")
    public ResponseEntity<String> update(@Parameter(description = "Employee ID")
                                             @PathVariable Long id, @Valid @RequestBody EmployeeRequestDTO employee) {
        employeeService.updateEmployee(id, employee);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Employee Updated");
    }


    @Operation(summary = "Delete an existing employee",
            description = "Deletes the employee with the specified ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@Parameter(description = "Employee ID")
                                             @PathVariable Long id){
        employeeService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
