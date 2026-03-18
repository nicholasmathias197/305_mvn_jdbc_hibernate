package com.perscholas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * JPA entity mapped to the {@code products} table.
 */
@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor
@ToString(exclude = "productLine")
public class Product {

    @Id
    @Column(name = "productCode", length = 15)
    private String productCode;

    @Column(nullable = false, length = 70)
    private String productName;

    @Column(nullable = false, length = 10)
    private String productScale;

    @Column(nullable = false, length = 50)
    private String productVendor;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String productDescription;

    @Column(nullable = false)
    private short quantityInStock;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal buyPrice;

    @Column(name = "MSRP", nullable = false, precision = 10, scale = 2)
    private BigDecimal msrp;

    // ---- Relationships ----

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productLine", referencedColumnName = "productLine")
    private ProductLine productLine;
}
