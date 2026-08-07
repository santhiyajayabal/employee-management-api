package com.example.employee.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class EmployeeResponseDTO {

    private Long id;
    private String name;
    private String email;
    private String department;
    private double salary;
    private LocalDate dateOfJoining;
}