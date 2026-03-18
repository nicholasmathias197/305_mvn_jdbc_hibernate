package com.perscholas.dao;

import com.perscholas.db.ConnectionManager;
import com.perscholas.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link ProductDAO}.
 */
public class ProductDAOImpl implements ProductDAO {

    private static final String SELECT_BY_ID_SQL =
            "SELECT * FROM products WHERE productCode = ?";

    private static final String SELECT_ALL_SQL =
            "SELECT * FROM products ORDER BY productName";

    private static final String SELECT_BY_LINE_SQL =
            "SELECT * FROM products WHERE productLine = ? ORDER BY productName";

    @Override
    public Optional<Product> findById(String productCode) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            ps.setString(1, productCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding product " + productCode, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                products.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listing products", e);
        }
        return products;
    }

    @Override
    public List<Product> findByProductLine(String productLine) {
        List<Product> products = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_LINE_SQL)) {

            ps.setString(1, productLine);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding products by line " + productLine, e);
        }
        return products;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductCode(rs.getString("productCode"));
        p.setProductName(rs.getString("productName"));
        p.setProductLine(rs.getString("productLine"));
        p.setProductScale(rs.getString("productScale"));
        p.setProductVendor(rs.getString("productVendor"));
        p.setProductDescription(rs.getString("productDescription"));
        p.setQuantityInStock(rs.getInt("quantityInStock"));
        p.setBuyPrice(rs.getBigDecimal("buyPrice"));
        p.setMsrp(rs.getBigDecimal("MSRP"));
        return p;
    }
}
