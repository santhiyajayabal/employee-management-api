package com.example.employee.controller;

import com.example.employee.dto.EmployeeRequestDTO;
import com.example.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void cleanup() {
        employeeRepository.deleteAll();
    }

    // ---------- CREATE ----------

    @Test
    void createEmployee_withValidData_returns201() throws Exception {
        EmployeeRequestDTO dto = new EmployeeRequestDTO();
        dto.setName("Grace Hopper");
        dto.setEmail("grace@example.com");
        dto.setDepartment("Engineering");
        dto.setSalary(90000);
        dto.setDateOfJoining(LocalDate.of(2022, 4, 10));

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Employee created"));

        assertEquals(1, employeeRepository.findAll().size());
    }

    @Test
    void createEmployee_withMissingName_returns400() throws Exception {
        EmployeeRequestDTO dto = new EmployeeRequestDTO();
        dto.setName("");
        dto.setEmail("invalid@example.com");
        dto.setDepartment("Engineering");
        dto.setSalary(50000);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // ---------- GET BY ID ----------

    @Test
    void getEmployeeById_found_returns200() throws Exception {
        Long id = createEmployeeAndGetId("Ada Lovelace", "ada@example.com", "R&D", 120000);

        mockMvc.perform(get("/api/employees/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ada@example.com"))
                .andExpect(jsonPath("$.name").value("Ada Lovelace"));
    }

    @Test
    void getEmployeeById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/employees/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    // ---------- UPDATE ----------

    @Test
    void updateEmployee_returns200_andPersistsChanges() throws Exception {
        Long id = createEmployeeAndGetId("Alan Turing", "alan@example.com", "Research", 110000);

        EmployeeRequestDTO update = new EmployeeRequestDTO();
        update.setName("Alan M. Turing");
        update.setEmail("alan@example.com");
        update.setDepartment("Research");
        update.setSalary(115000);
        update.setDateOfJoining(LocalDate.of(2019, 6, 15));

        mockMvc.perform(put("/api/employees/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(content().string("Employee Updated"));

        assertEquals("Alan M. Turing", employeeRepository.findById(id).get().getName());
    }

    // ---------- DELETE ----------

    @Test
    void deleteEmployee_returns204_andRemoves() throws Exception {
        Long id = createEmployeeAndGetId("To Be Deleted", "tbd@example.com", "Temp", 30000);

        mockMvc.perform(delete("/api/employees/" + id))
                .andExpect(status().isNoContent());

        assertFalse(employeeRepository.existsById(id));
    }

    // ---------- DEPARTMENT ----------

    @Test
    void getByDept_returnsEmployeesInDepartment() throws Exception {
        createEmployeeAndGetId("E1", "e1@example.com", "Sales", 40000);
        createEmployeeAndGetId("E2", "e2@example.com", "Sales", 42000);
        createEmployeeAndGetId("E3", "e3@example.com", "HR", 38000);

        mockMvc.perform(get("/api/employees/department/Sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].department").value("Sales"))
                .andExpect(jsonPath("$[1].department").value("Sales"));
    }

    // ---------- SEARCH ----------

    @Test
    void searchByName_containsMatchingEmployees() throws Exception {
        createEmployeeAndGetId("Ada Lovelace", "ada2@example.com", "R&D", 100000);
        createEmployeeAndGetId("Adrian Smith", "adrian@example.com", "R&D", 90000);

        mockMvc.perform(get("/api/employees/search").param("name", "Ada"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Ada Lovelace")));
    }

    // ---------- Helper ----------

    private Long createEmployeeAndGetId(String name, String email, String dept, double salary) throws Exception {
        EmployeeRequestDTO dto = new EmployeeRequestDTO();
        dto.setName(name);
        dto.setEmail(email);
        dto.setDepartment(dept);
        dto.setSalary(salary);
        dto.setDateOfJoining(LocalDate.of(2021, 1, 1));

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));

        return employeeRepository.findAll().stream()
                .filter(e -> email.equals(e.getEmail()))
                .findFirst()
                .map(e -> e.getId())
                .orElseThrow();
    }
}