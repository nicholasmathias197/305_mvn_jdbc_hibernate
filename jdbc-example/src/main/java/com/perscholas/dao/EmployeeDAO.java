package com.perscholas.dao;

import com.perscholas.model.Employee;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for the {@code employees} table.
 */
public interface EmployeeDAO {

    Optional<Employee> findById(int employeeNumber);

    List<Employee> findAll();

    List<Employee> findByJobTitle(String jobTitle);
}
