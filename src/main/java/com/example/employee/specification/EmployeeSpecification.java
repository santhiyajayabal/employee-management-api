package com.example.employee.specification;

import com.example.employee.model.Employee;
import org.springframework.data.jpa.domain.Specification;

public class EmployeeSpecification {

    public static Specification<Employee> hasName(String name) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Employee> hasDepartment(String department) {
        return (root, query, cb) ->
                cb.equal(cb.lower(root.get("department")), department.toLowerCase());
    }

    public static Specification<Employee> hasMinSalary(Double minSalary) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("salary"), minSalary);
    }

    public static Specification<Employee> hasMaxSalary(Double maxSalary) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("salary"), maxSalary);
    }
}
