package com.perscholas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * JPA entity mapped to the {@code offices} table.
 */
@Entity
@Table(name = "offices")
@Getter @Setter @NoArgsConstructor
@ToString(exclude = "employees")
public class Office {

    @Id
    @Column(name = "officeCode", length = 10)
    private String officeCode;

    @Column(nullable = false, length = 50)
    private String city;

    @Column(nullable = false, length = 50)
    private String phone;

    @Column(nullable = false, length = 50)
    private String addressLine1;

    @Column(length = 50)
    private String addressLine2;

    @Column(length = 50)
    private String state;

    @Column(nullable = false, length = 50)
    private String country;

    @Column(nullable = false, length = 15)
    private String postalCode;

    @Column(nullable = false, length = 10)
    private String territory;

    @OneToMany(mappedBy = "office", fetch = FetchType.LAZY)
    private List<Employee> employees;
}
