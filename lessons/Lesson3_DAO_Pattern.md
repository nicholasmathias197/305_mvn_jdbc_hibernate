# Lesson 3: Design Patterns & Data Access Object (DAO) Pattern

---

## Overview of Design Patterns

A **design pattern** is a proven, reusable solution to a common problem in software design. Think of it as a blueprint — not actual code, but a template for how to solve a problem.

### Why Design Patterns Matter
- **Don't reinvent the wheel** — these solutions have been tested for decades
- **Common vocabulary** — say "DAO pattern" and every developer knows what you mean
- **Maintainability** — patterns produce organized, changeable code
- **Separation of concerns** — each class has one clear responsibility

### Categories of Design Patterns

| Category | Purpose | Examples |
|---|---|---|
| **Creational** | How objects are created | Singleton, Factory, Builder |
| **Structural** | How objects are composed | Adapter, Decorator, Facade |
| **Behavioral** | How objects communicate | Observer, Strategy, Iterator |
| **Architectural** | How layers are organized | MVC, DAO, Repository |

The **DAO pattern** falls into the architectural category — it structures how your application accesses data.

---

## Usage of Design Patterns

In a real application, you don't just dump all your code into `main()`. You organize it into layers:

```
┌─────────────────────┐
│  Presentation Layer  │  ← UI / Console / Web Controller
├─────────────────────┤
│  Business Logic      │  ← Rules, calculations, validation
├─────────────────────┤
│  Data Access Layer   │  ← DAO — talks to the database
├─────────────────────┤
│  Database            │  ← MySQL (classicmodels)
└─────────────────────┘
```

Each layer only talks to the layer directly below it. The presentation layer should **never** write SQL or create database connections.

---

## Data Access Object (DAO) Pattern

The **DAO pattern** provides an abstract interface to the database. It separates the "how do I get data?" logic from the "what do I do with the data?" logic.

### The Problem DAO Solves

Without DAO, database code is scattered everywhere:

```java
// BAD — SQL mixed with business logic and presentation
public static void main(String[] args) {
    Connection conn = DriverManager.getConnection(url, user, pass);
    ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM customers WHERE country='USA'");
    while (rs.next()) {
        // calculating, formatting, displaying — all in one place
        double credit = rs.getDouble("creditLimit");
        if (credit > 50000) {
            System.out.println("VIP: " + rs.getString("customerName"));
        }
    }
}
```

**Problems with this approach:**
- Can't reuse the query elsewhere
- Can't switch databases without rewriting everything
- Can't test business logic without a database
- One massive file that does everything

---

## Data Access Object Pattern Components

The DAO pattern has **four components**:

```
┌──────────────────┐     ┌──────────────────┐
│  DAO Interface    │     │  Model / POJO     │
│  (CustomerDAO)    │     │  (Customer)       │
└────────┬─────────┘     └──────────────────┘
         │ implements              ▲
┌────────┴─────────┐              │ returns/accepts
│  DAO Impl         │──────────────┘
│  (CustomerDAOImpl)│
└────────┬─────────┘
         │ uses
┌────────┴─────────┐
│  Database         │
│  (classicmodels)  │
└──────────────────┘
```

### 1. Model / POJO (Plain Old Java Object)
A Java class that represents a row in a database table. Each field maps to a column.

### 2. DAO Interface
Defines **what operations** are available (CRUD), without specifying how they're implemented.

### 3. DAO Implementation
The class that **actually writes the JDBC code** to perform the operations.

### 4. Client / Service
The business logic code that uses the DAO — it never sees SQL or connections.

---

## Applying OOP and Data Access Object to JDBC Application

Let's build a complete DAO for the `customers` table in classicmodels.

### Step 1: Create the Model (POJO)

```java
package com.perscholas.model;

import java.math.BigDecimal;

public class Customer {
    private int customerNumber;
    private String customerName;
    private String contactLastName;
    private String contactFirstName;
    private String phone;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Integer salesRepEmployeeNumber;
    private BigDecimal creditLimit;

    // No-argument constructor
    public Customer() {}

    // Constructor with key fields
    public Customer(int customerNumber, String customerName, String contactLastName,
                    String contactFirstName, String phone, String addressLine1,
                    String city, String country) {
        this.customerNumber = customerNumber;
        this.customerName = customerName;
        this.contactLastName = contactLastName;
        this.contactFirstName = contactFirstName;
        this.phone = phone;
        this.addressLine1 = addressLine1;
        this.city = city;
        this.country = country;
    }

    // Getters and Setters for all fields
    public int getCustomerNumber() { return customerNumber; }
    public void setCustomerNumber(int customerNumber) { this.customerNumber = customerNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getContactLastName() { return contactLastName; }
    public void setContactLastName(String contactLastName) { this.contactLastName = contactLastName; }

    public String getContactFirstName() { return contactFirstName; }
    public void setContactFirstName(String contactFirstName) { this.contactFirstName = contactFirstName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public Integer getSalesRepEmployeeNumber() { return salesRepEmployeeNumber; }
    public void setSalesRepEmployeeNumber(Integer salesRepEmployeeNumber) {
        this.salesRepEmployeeNumber = salesRepEmployeeNumber;
    }

    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }

    @Override
    public String toString() {
        return "Customer{" +
               "customerNumber=" + customerNumber +
               ", customerName='" + customerName + '\'' +
               ", city='" + city + '\'' +
               ", country='" + country + '\'' +
               ", creditLimit=" + creditLimit +
               '}';
    }
}
```

> Notice we use `Integer` (wrapper) for `salesRepEmployeeNumber` because it can be `NULL` in the database. Primitive `int` can't hold null.

### Step 2: Create the DAO Interface

```java
package com.perscholas.dao;

import com.perscholas.model.Customer;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CustomerDAO {

    // CREATE
    void insert(Customer customer) throws SQLException;

    // READ
    Optional<Customer> findById(int customerNumber) throws SQLException;
    List<Customer> findAll() throws SQLException;
    List<Customer> findByCountry(String country) throws SQLException;

    // UPDATE
    void update(Customer customer) throws SQLException;

    // DELETE
    void delete(int customerNumber) throws SQLException;
}
```

> The interface uses `Optional<Customer>` for `findById` — this is modern Java style. It forces the caller to handle the case where the customer doesn't exist, instead of returning `null`.

### Step 3: Create the DAO Implementation

```java
package com.perscholas.dao;

import com.perscholas.db.ConnectionManager;
import com.perscholas.model.Customer;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDAOImpl implements CustomerDAO {

    // --- CREATE ---
    @Override
    public void insert(Customer c) throws SQLException {
        String sql = """
            INSERT INTO customers (customerNumber, customerName, contactLastName,
                contactFirstName, phone, addressLine1, addressLine2, city,
                state, postalCode, country, salesRepEmployeeNumber, creditLimit)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, c.getCustomerNumber());
            pstmt.setString(2, c.getCustomerName());
            pstmt.setString(3, c.getContactLastName());
            pstmt.setString(4, c.getContactFirstName());
            pstmt.setString(5, c.getPhone());
            pstmt.setString(6, c.getAddressLine1());
            pstmt.setString(7, c.getAddressLine2());
            pstmt.setString(8, c.getCity());
            pstmt.setString(9, c.getState());
            pstmt.setString(10, c.getPostalCode());
            pstmt.setString(11, c.getCountry());

            if (c.getSalesRepEmployeeNumber() != null) {
                pstmt.setInt(12, c.getSalesRepEmployeeNumber());
            } else {
                pstmt.setNull(12, Types.INTEGER);
            }

            pstmt.setBigDecimal(13, c.getCreditLimit());
            pstmt.executeUpdate();
        }
    }

    // --- READ (single) ---
    @Override
    public Optional<Customer> findById(int customerNumber) throws SQLException {
        String sql = "SELECT * FROM customers WHERE customerNumber = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerNumber);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToCustomer(rs));
            }
        }
        return Optional.empty();
    }

    // --- READ (all) ---
    @Override
    public List<Customer> findAll() throws SQLException {
        String sql = "SELECT * FROM customers ORDER BY customerName";
        List<Customer> customers = new ArrayList<>();

        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        }
        return customers;
    }

    // --- READ (by country) ---
    @Override
    public List<Customer> findByCountry(String country) throws SQLException {
        String sql = "SELECT * FROM customers WHERE country = ? ORDER BY customerName";
        List<Customer> customers = new ArrayList<>();

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, country);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        }
        return customers;
    }

    // --- UPDATE ---
    @Override
    public void update(Customer c) throws SQLException {
        String sql = """
            UPDATE customers SET customerName = ?, contactLastName = ?,
                contactFirstName = ?, phone = ?, addressLine1 = ?, addressLine2 = ?,
                city = ?, state = ?, postalCode = ?, country = ?,
                salesRepEmployeeNumber = ?, creditLimit = ?
            WHERE customerNumber = ?
            """;

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, c.getCustomerName());
            pstmt.setString(2, c.getContactLastName());
            pstmt.setString(3, c.getContactFirstName());
            pstmt.setString(4, c.getPhone());
            pstmt.setString(5, c.getAddressLine1());
            pstmt.setString(6, c.getAddressLine2());
            pstmt.setString(7, c.getCity());
            pstmt.setString(8, c.getState());
            pstmt.setString(9, c.getPostalCode());
            pstmt.setString(10, c.getCountry());

            if (c.getSalesRepEmployeeNumber() != null) {
                pstmt.setInt(11, c.getSalesRepEmployeeNumber());
            } else {
                pstmt.setNull(11, Types.INTEGER);
            }

            pstmt.setBigDecimal(12, c.getCreditLimit());
            pstmt.setInt(13, c.getCustomerNumber());
            pstmt.executeUpdate();
        }
    }

    // --- DELETE ---
    @Override
    public void delete(int customerNumber) throws SQLException {
        String sql = "DELETE FROM customers WHERE customerNumber = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerNumber);
            pstmt.executeUpdate();
        }
    }

    // --- Helper: map a ResultSet row to a Customer object ---
    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
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

        c.setCreditLimit(rs.getBigDecimal("creditLimit"));
        return c;
    }
}
```

### Step 4: Use the DAO in Your Application

```java
package com.perscholas;

import com.perscholas.dao.CustomerDAO;
import com.perscholas.dao.CustomerDAOImpl;
import com.perscholas.model.Customer;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class App {
    public static void main(String[] args) {
        CustomerDAO customerDAO = new CustomerDAOImpl();

        try {
            // READ — Find a customer by ID
            Optional<Customer> customer = customerDAO.findById(103);
            customer.ifPresent(c -> System.out.println("Found: " + c));

            // READ — List all French customers
            List<Customer> frenchCustomers = customerDAO.findByCountry("France");
            System.out.println("\nFrench customers: " + frenchCustomers.size());
            frenchCustomers.forEach(c ->
                System.out.println("  " + c.getCustomerName() + " — " + c.getCity()));

            // CREATE — Insert a new customer
            Customer newCustomer = new Customer(
                999, "Per Scholas Training Co.", "Smith", "Jane",
                "555-0199", "100 Learning Ave", "Denver", "USA"
            );
            newCustomer.setCreditLimit(new BigDecimal("50000.00"));
            customerDAO.insert(newCustomer);
            System.out.println("\nInserted customer 999");

            // UPDATE — Change the credit limit
            newCustomer.setCreditLimit(new BigDecimal("75000.00"));
            customerDAO.update(newCustomer);
            System.out.println("Updated customer 999's credit limit");

            // DELETE — Remove the test customer
            customerDAO.delete(999);
            System.out.println("Deleted customer 999");

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }
}
```

---

## Before DAO — Process of Java JDBC API

Without DAO, every part of your app writes its own JDBC code:

```
┌────────────────────────────────────────────────────────────┐
│                    main() or Service                        │
│                                                            │
│  DriverManager.getConnection(...)                          │
│  PreparedStatement pstmt = conn.prepareStatement(...)     │
│  ResultSet rs = pstmt.executeQuery()                      │
│  while (rs.next()) { /* build objects, do logic */ }       │
│  conn.close()                                              │
│                                                            │
│  // Same pattern repeated for EVERY query                  │
│  // SQL is everywhere, duplicated, hard to maintain        │
└────────────────────────────────────────────────────────────┘
```

**Problems:**
- Connection code duplicated in every method
- SQL scattered across the entire codebase
- Business logic mixed with data access
- Impossible to unit test without a real database
- Switching databases means rewriting the whole app

---

## After DAO — Process of Java JDBC API

With DAO, the structure is clean and layered:

```
┌─────────────────┐
│   App / Main     │  Knows about: CustomerDAO, Customer
│                  │  Does NOT know about: SQL, Connection, ResultSet
├─────────────────┤
│   CustomerDAO    │  Interface — defines CRUD operations
├─────────────────┤
│ CustomerDAOImpl  │  The ONLY place with JDBC/SQL code
├─────────────────┤
│ ConnectionMgr    │  The ONLY place with connection details
├─────────────────┤
│   MySQL DB       │
└─────────────────┘
```

**Benefits:**
- **Single Responsibility** — each class does one thing
- **Testability** — mock the DAO interface for unit tests
- **Flexibility** — swap `CustomerDAOImpl` with `CustomerHibernateDAO` without changing business logic
- **Maintainability** — all SQL for customers is in one file

---

## Project Structure

```
classicmodels-jdbc/
├── pom.xml
└── src/main/java/com/perscholas/
    ├── App.java                    ← Entry point
    ├── db/
    │   └── ConnectionManager.java  ← Database connection factory
    ├── model/
    │   ├── Customer.java           ← POJO for customers table
    │   ├── Employee.java           ← POJO for employees table
    │   └── Product.java            ← POJO for products table
    └── dao/
        ├── CustomerDAO.java        ← Interface
        ├── CustomerDAOImpl.java    ← JDBC implementation
        ├── EmployeeDAO.java
        ├── EmployeeDAOImpl.java
        ├── ProductDAO.java
        └── ProductDAOImpl.java
```

---

## Key Takeaways

1. **Design patterns** are reusable solutions — not code to copy-paste, but structures to follow
2. The **DAO pattern** separates data access from business logic
3. Four components: **Model** (POJO), **DAO Interface**, **DAO Implementation**, **Client**
4. The DAO interface defines **what** operations exist; the implementation defines **how**
5. Use `Optional<T>` for methods that might not find a result
6. Use `rs.wasNull()` to handle nullable columns mapped to primitive types
7. The `mapResultSetToCustomer()` helper keeps code DRY (Don't Repeat Yourself)
