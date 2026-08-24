package com.example.employee.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI employeeApiInfo() {
        return new OpenAPI().info(new Info().title("Employee Management API")
                .description("REST API for managing employee records — supports CRUD operations, pagination, sorting, and dynamic multi-filter search using JPA Specifications.")
                .version("1.0.0")
                .contact(new Contact()
                        .name("San")
                        .email("santhiyaofficial@gmail.com")));

    }
}