package com.example.employee.controller;

import com.example.employee.dto.EmployeeRequestDTO;
import com.example.employee.dto.EmployeeResponseDTO;
import com.example.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class EmployeeControllerIntegrationTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    private final RestTemplate rest = new RestTemplate();
    private final String baseUrl = "http://localhost:8080/api/employees";

    @BeforeEach
    void cleanup() {
        employeeRepository.deleteAll();
    }

    @Test
    void createEmployee_withValidData_returns201_andPersists() {
        EmployeeRequestDTO dto = new EmployeeRequestDTO();
        dto.setName("Grace Hopper");
        dto.setEmail("grace@example.com");
        dto.setDepartment("Engineering");
        dto.setSalary(90000);
        dto.setDateOfJoining(LocalDate.of(2022, 4, 10));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmployeeRequestDTO> request = new HttpEntity<>(dto, headers);

        ResponseEntity<String> response = rest.postForEntity(baseUrl, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("Employee created");

        // persisted in repository
        List<?> all = employeeRepository.findAll();
        assertThat(all).hasSize(1);
    }

    @Test
    void createEmployee_withMissingName_returns400() {
        EmployeeRequestDTO dto = new EmployeeRequestDTO();
        dto.setName("");  // invalid — @NotBlank
        dto.setEmail("invalid@example.com");
        dto.setDepartment("Engineering");
        dto.setSalary(50000);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmployeeRequestDTO> request = new HttpEntity<>(dto, headers);

        org.junit.jupiter.api.Assertions.assertThrows(org.springframework.web.client.HttpClientErrorException.class,
                () -> rest.postForEntity(baseUrl, request, String.class));
    }

    @Test
    void getById_returnsEmployee() {
        // create employee
        EmployeeRequestDTO dto = new EmployeeRequestDTO();
        dto.setName("Ada Lovelace");
        dto.setEmail("ada@example.com");
        dto.setDepartment("R&D");
        dto.setSalary(120000);
        dto.setDateOfJoining(LocalDate.of(2020, 1, 1));

        rest.postForEntity(baseUrl, new HttpEntity<>(dto, jsonHeaders()), String.class);

        // find id from repository
        Optional<Long> maybeId = employeeRepository.findAll().stream()
                .filter(e -> "ada@example.com".equals(e.getEmail()))
                .map(e -> e.getId())
                .findFirst();
        assertThat(maybeId).isPresent();
        Long id = maybeId.get();

        ResponseEntity<EmployeeResponseDTO> response = rest.getForEntity(baseUrl + "/" + id, EmployeeResponseDTO.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        EmployeeResponseDTO body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getEmail()).isEqualTo("ada@example.com");
        assertThat(body.getName()).isEqualTo("Ada Lovelace");
    }

    @Test
    void updateEmployee_returns200_andPersistsChanges() {
        // create employee
        EmployeeRequestDTO dto = new EmployeeRequestDTO();
        dto.setName("Alan Turing");
        dto.setEmail("alan@example.com");
        dto.setDepartment("Research");
        dto.setSalary(110000);
        dto.setDateOfJoining(LocalDate.of(2019, 6, 15));
        rest.postForEntity(baseUrl, new HttpEntity<>(dto, jsonHeaders()), String.class);

        Long id = employeeRepository.findAll().stream()
                .filter(e -> "alan@example.com".equals(e.getEmail())).findFirst().map(e -> e.getId()).get();

        EmployeeRequestDTO update = new EmployeeRequestDTO();
        update.setName("Alan M. Turing");
        update.setEmail("alan@example.com");
        update.setDepartment("Research");
        update.setSalary(115000);
        update.setDateOfJoining(LocalDate.of(2019, 6, 15));

        ResponseEntity<String> resp = rest.exchange(baseUrl + "/" + id,
                HttpMethod.PUT, new HttpEntity<>(update, jsonHeaders()), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEqualTo("Employee Updated");

        // verify persisted
        assertThat(employeeRepository.findById(id)).isPresent();
        assertThat(employeeRepository.findById(id).get().getName()).isEqualTo("Alan M. Turing");
    }

    @Test
    void deleteEmployee_returns204_andRemoves() {
        EmployeeRequestDTO dto = new EmployeeRequestDTO();
        dto.setName("To Be Deleted");
        dto.setEmail("tbd@example.com");
        dto.setDepartment("Temp");
        dto.setSalary(30000);
        rest.postForEntity(baseUrl, new HttpEntity<>(dto, jsonHeaders()), String.class);

        Long id = employeeRepository.findAll().stream()
                .filter(e -> "tbd@example.com".equals(e.getEmail())).findFirst().map(e -> e.getId()).get();

        ResponseEntity<Void> resp = rest.exchange(baseUrl + "/" + id, HttpMethod.DELETE, null, Void.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(employeeRepository.existsById(id)).isFalse();
    }

    @Test
    void getByDept_returnsEmployeesInDepartment() {
        EmployeeRequestDTO a = new EmployeeRequestDTO();
        a.setName("E1"); a.setEmail("e1@example.com"); a.setDepartment("Sales"); a.setSalary(40000);
        EmployeeRequestDTO b = new EmployeeRequestDTO();
        b.setName("E2"); b.setEmail("e2@example.com"); b.setDepartment("Sales"); b.setSalary(42000);
        EmployeeRequestDTO c = new EmployeeRequestDTO();
        c.setName("E3"); c.setEmail("e3@example.com"); c.setDepartment("HR"); c.setSalary(38000);
        rest.postForEntity(baseUrl, new HttpEntity<>(a, jsonHeaders()), String.class);
        rest.postForEntity(baseUrl, new HttpEntity<>(b, jsonHeaders()), String.class);
        rest.postForEntity(baseUrl, new HttpEntity<>(c, jsonHeaders()), String.class);

        ResponseEntity<EmployeeResponseDTO[]> resp = rest.getForEntity(baseUrl + "/department/Sales", EmployeeResponseDTO[].class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        EmployeeResponseDTO[] arr = resp.getBody();
        assertThat(arr).isNotNull();
        List<EmployeeResponseDTO> list = Arrays.asList(arr);
        assertThat(list).hasSize(2);
        assertThat(list).allSatisfy(e -> assertThat(e.getDepartment()).isEqualTo("Sales"));
    }

    @Test
    void searchByName_containsMatchingEmployees() {
        EmployeeRequestDTO a = new EmployeeRequestDTO();
        a.setName("Ada Lovelace"); a.setEmail("ada2@example.com"); a.setDepartment("R&D"); a.setSalary(100000);
        EmployeeRequestDTO b = new EmployeeRequestDTO();
        b.setName("Adrian Smith"); b.setEmail("adrian@example.com"); b.setDepartment("R&D"); b.setSalary(90000);
        rest.postForEntity(baseUrl, new HttpEntity<>(a, jsonHeaders()), String.class);
        rest.postForEntity(baseUrl, new HttpEntity<>(b, jsonHeaders()), String.class);

        ResponseEntity<String> resp = rest.getForEntity(baseUrl + "/search?name=Ada", String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).contains("Ada Lovelace");
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }
}