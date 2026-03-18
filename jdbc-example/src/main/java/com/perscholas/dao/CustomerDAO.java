package com.perscholas.dao;

import com.perscholas.model.Customer;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for the {@code customers} table.
 */
public interface CustomerDAO {

    void insert(Customer customer);

    Optional<Customer> findById(int customerNumber);

    List<Customer> findAll();

    List<Customer> findByCountry(String country);

    boolean update(Customer customer);

    boolean delete(int customerNumber);
}
