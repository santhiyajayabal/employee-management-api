package com.example.employee.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeRequestDTO {

    @NotBlank(message = "Name is Required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;
    @NotBlank(message = "Email is Required")
    @Email(message = "Enter the valid email")
    private String email;
    @NotBlank(message = "Department is Required")
    private String department;
    @Positive(message = "Value should be positive value")
    private double salary;
    @PastOrPresent(message = "Date of joining should be in the past or present")
    private LocalDate dateOfJoining;
}
