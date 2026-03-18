package com.perscholas.dao;

import com.perscholas.db.ConnectionManager;
import com.perscholas.model.Customer;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link CustomerDAO}.
 * Demonstrates PreparedStatement, ResultSet mapping, and try-with-resources.
 */
public class CustomerDAOImpl implements CustomerDAO {

    // ---- SQL constants ----
    private static final String INSERT_SQL =
            "INSERT INTO customers (customerNumber, customerName, contactLastName, " +
            "contactFirstName, phone, addressLine1, addressLine2, city, state, " +
            "postalCode, country, salesRepEmployeeNumber, creditLimit) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID_SQL =
            "SELECT * FROM customers WHERE customerNumber = ?";

    private static final String SELECT_ALL_SQL =
            "SELECT * FROM customers ORDER BY customerName";

    private static final String SELECT_BY_COUNTRY_SQL =
            "SELECT * FROM customers WHERE country = ? ORDER BY customerName";

    private static final String UPDATE_SQL =
            "UPDATE customers SET customerName = ?, contactLastName = ?, contactFirstName = ?, " +
            "phone = ?, addressLine1 = ?, addressLine2 = ?, city = ?, state = ?, " +
            "postalCode = ?, country = ?, salesRepEmployeeNumber = ?, creditLimit = ? " +
            "WHERE customerNumber = ?";

    private static final String DELETE_SQL =
            "DELETE FROM customers WHERE customerNumber = ?";

    // ---- CRUD methods ----

    @Override
    public void insert(Customer c) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

            ps.setInt(1, c.getCustomerNumber());
            ps.setString(2, c.getCustomerName());
            ps.setString(3, c.getContactLastName());
            ps.setString(4, c.getContactFirstName());
            ps.setString(5, c.getPhone());
            ps.setString(6, c.getAddressLine1());
            ps.setString(7, c.getAddressLine2());
            ps.setString(8, c.getCity());
            ps.setString(9, c.getState());
            ps.setString(10, c.getPostalCode());
            ps.setString(11, c.getCountry());
            if (c.getSalesRepEmployeeNumber() != null) {
                ps.setInt(12, c.getSalesRepEmployeeNumber());
            } else {
                ps.setNull(12, Types.INTEGER);
            }
            if (c.getCreditLimit() != null) {
                ps.setBigDecimal(13, c.getCreditLimit());
            } else {
                ps.setNull(13, Types.DECIMAL);
            }

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting customer " + c.getCustomerNumber(), e);
        }
    }

    @Override
    public Optional<Customer> findById(int customerNumber) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            ps.setInt(1, customerNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding customer " + customerNumber, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Customer> findAll() {
        List<Customer> customers = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                customers.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listing customers", e);
        }
        return customers;
    }

    @Override
    public List<Customer> findByCountry(String country) {
        List<Customer> customers = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_COUNTRY_SQL)) {

            ps.setString(1, country);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    customers.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding customers by country " + country, e);
        }
        return customers;
    }

    @Override
    public boolean update(Customer c) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {

            ps.setString(1, c.getCustomerName());
            ps.setString(2, c.getContactLastName());
            ps.setString(3, c.getContactFirstName());
            ps.setString(4, c.getPhone());
            ps.setString(5, c.getAddressLine1());
            ps.setString(6, c.getAddressLine2());
            ps.setString(7, c.getCity());
            ps.setString(8, c.getState());
            ps.setString(9, c.getPostalCode());
            ps.setString(10, c.getCountry());
            if (c.getSalesRepEmployeeNumber() != null) {
                ps.setInt(11, c.getSalesRepEmployeeNumber());
            } else {
                ps.setNull(11, Types.INTEGER);
            }
            if (c.getCreditLimit() != null) {
                ps.setBigDecimal(12, c.getCreditLimit());
            } else {
                ps.setNull(12, Types.DECIMAL);
            }
            ps.setInt(13, c.getCustomerNumber());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating customer " + c.getCustomerNumber(), e);
        }
    }

    @Override
    public boolean delete(int customerNumber) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {

            ps.setInt(1, customerNumber);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting customer " + customerNumber, e);
        }
    }

    // ---- Helper ----

    /**
     * Maps the current ResultSet row to a Customer object.
     */
    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerNumber(rs.getInt("customerNumber"));
        c.setCustomerName(rs.getString("customerName"));
        c.setContactLastName(rs.getString("contactLastName"));
        c.setContactFirstName(rs.getString("contactFirstName"));
        c.setPhone(rs.getString("phone"));
        c.setAddressLine1(rs.getString("addressLine1"));
        c.setAddressLine2(rs.getString("addressLine2"));
        c.setCity(rs.getString("city"));
        c.setState(rs.getString("state"));
        c.setPostalCode(rs.getString("postalCode"));
        c.setCountry(rs.getString("country"));

        int salesRep = rs.getInt("salesRepEmployeeNumber");
        c.setSalesRepEmployeeNumber(rs.wasNull() ? null : salesRep);

        BigDecimal credit = rs.getBigDecimal("creditLimit");
        c.setCreditLimit(credit);

        return c;
    }
}
