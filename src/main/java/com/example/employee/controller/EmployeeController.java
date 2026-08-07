package com.example.employee.controller;


import com.example.employee.dto.EmployeeRequestDTO;
import com.example.employee.dto.EmployeeResponseDTO;
import com.example.employee.model.Employee;
import com.example.employee.service.EmployeeService;
import jakarta.validation.Valid;
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

    private EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService){
      this.employeeService=employeeService;
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeResponseDTO>> get(Pageable pageable){
        return ResponseEntity.ok(employeeService.getEmployeeList(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<EmployeeResponseDTO>> search(@RequestParam String name, Pageable pageable) {
        Page<EmployeeResponseDTO> employees = employeeService.searchEmployeesByName(name, pageable);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getById(@PathVariable Long id) {
        EmployeeResponseDTO employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/department/{dept}")
    public ResponseEntity<List<EmployeeResponseDTO>> getByDept(@PathVariable String dept){
        List<EmployeeResponseDTO> employees = employeeService.getEmployeeByDept(dept);
        return ResponseEntity.ok(employees);
    }

    @PostMapping
    public ResponseEntity<String> create(@Valid @RequestBody EmployeeRequestDTO employee){
        employeeService.createEmployeeList(employee);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Employee created");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id, @Valid @RequestBody EmployeeRequestDTO employee) {
        employeeService.updateEmployee(id, employee);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Employee Updated");
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id){
        employeeService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
