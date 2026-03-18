package com.perscholas.dao;

import com.perscholas.db.ConnectionManager;
import com.perscholas.model.Employee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link EmployeeDAO}.
 */
public class EmployeeDAOImpl implements EmployeeDAO {

    private static final String SELECT_BY_ID_SQL =
            "SELECT * FROM employees WHERE employeeNumber = ?";

    private static final String SELECT_ALL_SQL =
            "SELECT * FROM employees ORDER BY lastName, firstName";

    private static final String SELECT_BY_JOB_TITLE_SQL =
            "SELECT * FROM employees WHERE jobTitle = ? ORDER BY lastName";

    @Override
    public Optional<Employee> findById(int employeeNumber) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            ps.setInt(1, employeeNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding employee " + employeeNumber, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Employee> findAll() {
        List<Employee> employees = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                employees.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listing employees", e);
        }
        return employees;
    }

    @Override
    public List<Employee> findByJobTitle(String jobTitle) {
        List<Employee> employees = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_JOB_TITLE_SQL)) {

            ps.setString(1, jobTitle);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    employees.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding employees by job title", e);
        }
        return employees;
    }

    private Employee mapRow(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setEmployeeNumber(rs.getInt("employeeNumber"));
        e.setLastName(rs.getString("lastName"));
        e.setFirstName(rs.getString("firstName"));
        e.setExtension(rs.getString("extension"));
        e.setEmail(rs.getString("email"));
        e.setOfficeCode(rs.getString("officeCode"));

        int reportsTo = rs.getInt("reportsTo");
        e.setReportsTo(rs.wasNull() ? null : reportsTo);

        e.setJobTitle(rs.getString("jobTitle"));
        return e;
    }
}
