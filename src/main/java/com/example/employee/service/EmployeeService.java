package com.example.employee.service;


import com.example.employee.dto.EmployeeRequestDTO;
import com.example.employee.dto.EmployeeResponseDTO;
import com.example.employee.mapper.EmployeeMapper;
import com.example.employee.exception.EmployeeNotFoundException;
import com.example.employee.model.Employee;
import com.example.employee.repository.EmployeeRepository;
import com.example.employee.specification.EmployeeSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository){
        this.employeeRepository=employeeRepository;
    }

    public Page<EmployeeResponseDTO> getEmployeeList(Pageable pageable){
        return employeeRepository.findAll(pageable)
                .map(EmployeeMapper::toResponseDTO);
    }

    public Page<EmployeeResponseDTO> searchEmployeesByName(String name, Pageable pageable) {
        return employeeRepository.searchByName(name, pageable)
                .map(EmployeeMapper::toResponseDTO);
    }

    public Page<EmployeeResponseDTO> searchEmployees(String name, String department,
                                                     Double minSalary, Double maxSalary,
                                                     Pageable pageable) {

        Specification<Employee> spec = (root,
                                        query,
                                        criteriaBuilder) -> null;

        if (name != null && !name.isBlank()) {
            spec = spec.and(EmployeeSpecification.hasName(name));
        }
        if (department != null && !department.isBlank()) {
            spec = spec.and(EmployeeSpecification.hasDepartment(department));
        }
        if (minSalary != null) {
            spec = spec.and(EmployeeSpecification.hasMinSalary(minSalary));
        }
        if (maxSalary != null) {
            spec = spec.and(EmployeeSpecification.hasMaxSalary(maxSalary));
        }

        return employeeRepository.findAll(spec, pageable)
                .map(EmployeeMapper::toResponseDTO);
    }

    public void createEmployee(EmployeeRequestDTO employee){
        employeeRepository.save(EmployeeMapper.toEntity(employee));
    }

    public EmployeeResponseDTO getEmployeeById(Long id){
        return employeeRepository.findById(id)
                .map(EmployeeMapper::toResponseDTO)
                .orElseThrow(() ->
                new EmployeeNotFoundException("Employee not found Exception"));
    }

    public List<EmployeeResponseDTO> getEmployeeByDept(String department){
        return employeeRepository.findEmployeesInDepartment(department)
                .stream().map(EmployeeMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public void deleteById(Long id){
        if(!employeeRepository.existsById(id)) {
            throw new EmployeeNotFoundException("Employee not found with id: " + id);
        }
         employeeRepository.deleteById(id);
    }

    public void updateEmployee(Long id, EmployeeRequestDTO employeeDetails) {
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));

        existingEmployee.setName(employeeDetails.getName());
        existingEmployee.setEmail(employeeDetails.getEmail());
        existingEmployee.setDepartment(employeeDetails.getDepartment());
        existingEmployee.setSalary(employeeDetails.getSalary());
        existingEmployee.setDateOfJoining(employeeDetails.getDateOfJoining());

        employeeRepository.save(existingEmployee);
    }

}
