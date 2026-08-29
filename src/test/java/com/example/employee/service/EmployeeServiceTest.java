package com.example.employee.service;

import com.example.employee.dto.EmployeeRequestDTO;
import com.example.employee.dto.EmployeeResponseDTO;
import com.example.employee.exception.EmployeeNotFoundException;
import com.example.employee.model.Employee;
import com.example.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void getEmployeeList_returnsPagedResponse() {
        Employee e1 = new Employee();
        e1.setId(1L);
        e1.setName("Alice");
        e1.setEmail("alice@example.com");

        Page<Employee> page = new PageImpl<>(List.of(e1));
        Pageable pageable = PageRequest.of(0, 10);

        when(employeeRepository.findAll(pageable)).thenReturn(page);

        Page<EmployeeResponseDTO> result = employeeService.getEmployeeList(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Alice", result.getContent().getFirst().getName());
        verify(employeeRepository).findAll(pageable);
    }

    @Test
    void searchEmployeesByName_delegatesToRepository() {
        Employee e = new Employee();
        e.setId(2L);
        e.setName("Bob");
        Page<Employee> page = new PageImpl<>(List.of(e));
        Pageable pageable = PageRequest.of(0, 5);

        when(employeeRepository.searchByName("Bo", pageable)).thenReturn(page);

        Page<EmployeeResponseDTO> result = employeeService.searchEmployeesByName("Bo", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Bob", result.getContent().getFirst().getName());
        verify(employeeRepository).searchByName("Bo", pageable);
    }

    @Test
    void createEmployeeList_savesEntity() {
        EmployeeRequestDTO dto = new EmployeeRequestDTO();
        dto.setName("Carol");
        dto.setEmail("carol@example.com");
        dto.setDepartment("HR");
        dto.setSalary(50000);
        dto.setDateOfJoining(LocalDate.of(2020, 1, 1));

        employeeService.createEmployee(dto);

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());

        Employee saved = captor.getValue();
        assertEquals("Carol", saved.getName());
        assertEquals("HR", saved.getDepartment());
    }

    @Test
    void getEmployeeById_found_returnsDto() {
        Employee e = new Employee();
        e.setId(10L);
        e.setName("Dan");
        e.setEmail("dan@example.com");

        when(employeeRepository.findById(10L)).thenReturn(Optional.of(e));

        EmployeeResponseDTO dto = employeeService.getEmployeeById(10L);

        assertEquals(10L, dto.getId());
        assertEquals("Dan", dto.getName());
    }

    @Test
    void getEmployeeById_notFound_throws() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(99L));
    }

    @Test
    void getEmployeeByDept_returnsList() {
        Employee e1 = new Employee();
        e1.setId(3L);
        e1.setName("Eve");
        e1.setDepartment("IT");

        when(employeeRepository.findEmployeesInDepartment("IT")).thenReturn(List.of(e1));

        List<EmployeeResponseDTO> result = employeeService.getEmployeeByDept("IT");

        assertEquals(1, result.size());
        assertEquals("Eve", result.getFirst().getName());
    }

    @Test
    void deleteById_exists_deletes() {
        when(employeeRepository.existsById(5L)).thenReturn(true);

        employeeService.deleteById(5L);

        verify(employeeRepository).deleteById(5L);
    }

    @Test
    void deleteById_notExists_throws() {
        when(employeeRepository.existsById(6L)).thenReturn(false);

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteById(6L));
    }

    @Test
    void updateEmployee_updatesFieldsAndSaves() {
        Employee existing = new Employee();
        existing.setId(7L);
        existing.setName("Frank");
        existing.setEmail("old@example.com");
        existing.setDepartment("Sales");
        existing.setSalary(30000);
        existing.setDateOfJoining(LocalDate.of(2019, 6, 1));

        EmployeeRequestDTO update = new EmployeeRequestDTO();
        update.setName("Franklin");
        update.setEmail("franklin@example.com");
        update.setDepartment("Marketing");
        update.setSalary(35000);
        update.setDateOfJoining(LocalDate.of(2019, 6, 1));

        when(employeeRepository.findById(7L)).thenReturn(Optional.of(existing));

        employeeService.updateEmployee(7L, update);

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());

        Employee saved = captor.getValue();
        assertEquals("Franklin", saved.getName());
        assertEquals("franklin@example.com", saved.getEmail());
        assertEquals("Marketing", saved.getDepartment());
        assertEquals(35000, saved.getSalary());
        assertEquals(LocalDate.of(2019, 6, 1), saved.getDateOfJoining());
    }

    @Test
    void updateEmployee_notFound_throws() {
        when(employeeRepository.findById(88L)).thenReturn(Optional.empty());

        EmployeeRequestDTO update = new EmployeeRequestDTO();
        update.setName("X");

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.updateEmployee(88L, update));
    }
}
