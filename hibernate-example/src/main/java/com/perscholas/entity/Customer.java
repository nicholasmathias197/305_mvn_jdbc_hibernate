package com.perscholas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * JPA entity mapped to the {@code customers} table.
 */
@Entity
@Table(name = "customers")
@Getter @Setter @NoArgsConstructor
@ToString(exclude = "salesRep")
public class Customer {

    @Id
    @Column(name = "customerNumber")
    private int customerNumber;

    @Column(nullable = false, length = 50)
    private String customerName;

    @Column(nullable = false, length = 50)
    private String contactLastName;

    @Column(nullable = false, length = 50)
    private String contactFirstName;

    @Column(nullable = false, length = 50)
    private String phone;

    @Column(nullable = false, length = 50)
    private String addressLine1;

    @Column(length = 50)
    private String addressLine2;

    @Column(nullable = false, length = 50)
    private String city;

    @Column(length = 50)
    private String state;

    @Column(length = 15)
    private String postalCode;

    @Column(nullable = false, length = 50)
    private String country;

    @Column(precision = 10, scale = 2)
    private BigDecimal creditLimit;

    // ---- Relationships ----

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salesRepEmployeeNumber", referencedColumnName = "employeeNumber")
    private Employee salesRep;
}
