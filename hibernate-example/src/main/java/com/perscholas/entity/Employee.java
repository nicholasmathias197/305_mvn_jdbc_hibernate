package com.perscholas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * JPA entity mapped to the {@code employees} table.
 */
@Entity
@Table(name = "employees")
@Getter @Setter @NoArgsConstructor
@ToString(exclude = {"subordinates", "customers"})
public class Employee {

    @Id
    @Column(name = "employeeNumber")
    private int employeeNumber;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 10)
    private String extension;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 50)
    private String jobTitle;

    // ---- Relationships ----

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officeCode", referencedColumnName = "officeCode")
    private Office office;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportsTo", referencedColumnName = "employeeNumber")
    private Employee manager;

    @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
    private List<Employee> subordinates;

    @OneToMany(mappedBy = "salesRep", fetch = FetchType.LAZY)
    private List<Customer> customers;
}
