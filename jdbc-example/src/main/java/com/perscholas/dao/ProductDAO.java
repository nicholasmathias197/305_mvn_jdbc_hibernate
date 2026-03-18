package com.perscholas.dao;

import com.perscholas.model.Product;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for the {@code products} table.
 */
public interface ProductDAO {

    Optional<Product> findById(String productCode);

    List<Product> findAll();

    List<Product> findByProductLine(String productLine);
}
