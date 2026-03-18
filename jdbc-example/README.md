# ClassicModels JDBC Example

A complete JDBC application demonstrating the **DAO (Data Access Object) pattern** with the classicmodels MySQL database.

## Prerequisites

- **Java 21** (LTS) — [Download from Adoptium](https://adoptium.net/)
- **Maven 3.9+** — [Download](https://maven.apache.org/download.cgi) or use IntelliJ's bundled version
- **MySQL 8+** running locally with the `classicmodels` database loaded

## Setup

1. Make sure MySQL is running and the `classicmodels` database is loaded:
   ```sql
   SOURCE /path/to/classicmodels.sql;
   ```

2. Update the database credentials in `src/main/java/com/perscholas/db/ConnectionManager.java`:
   ```java
   private static final String USER = "root";
   private static final String PASSWORD = "your_password_here";
   ```

3. Build and run:
   ```bash
   mvn clean compile exec:java -Dexec.mainClass="com.perscholas.App"
   ```

   Or run tests:
   ```bash
   mvn clean test
   ```

## Project Structure

```
src/main/java/com/perscholas/
├── App.java                         — Main entry point, demonstrates all DAO operations
├── db/
│   └── ConnectionManager.java       — Database connection factory
├── model/
│   ├── Customer.java                — POJO for the customers table
│   ├── Employee.java                — POJO for the employees table
│   └── Product.java                 — POJO for the products table
└── dao/
    ├── CustomerDAO.java             — Interface defining CRUD operations
    ├── CustomerDAOImpl.java         — JDBC implementation of CustomerDAO
    ├── EmployeeDAO.java             — Interface for employee operations
    ├── EmployeeDAOImpl.java         — JDBC implementation of EmployeeDAO
    ├── ProductDAO.java              — Interface for product operations
    └── ProductDAOImpl.java          — JDBC implementation of ProductDAO
```

## Key Concepts Demonstrated

- **JDBC Connectivity** — `DriverManager.getConnection()`
- **PreparedStatement** — parameterized queries to prevent SQL injection
- **try-with-resources** — automatic resource cleanup
- **DAO Pattern** — separation of data access from business logic
- **POJO/Model classes** — plain Java objects representing database rows
- **Optional** — modern Java null-safe return types
