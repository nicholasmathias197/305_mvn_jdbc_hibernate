# Lesson 2: Java Database Connectivity (JDBC)

---

## Java Database Connectivity Driver/API

**JDBC** (Java Database Connectivity) is Java's standard API for connecting to and interacting with relational databases. It's part of the JDK — no extra frameworks needed.

### What JDBC Does
- Connects your Java application to a database (MySQL, PostgreSQL, Oracle, etc.)
- Sends SQL queries and receives results
- Handles transactions (commit, rollback)

### The JDBC Driver
Each database vendor provides a **JDBC driver** — a JAR file that implements the JDBC interfaces for their specific database. For MySQL, this is **MySQL Connector/J**.

| Database | Driver Artifact | Current GroupId |
|---|---|---|
| MySQL | `mysql-connector-j` | `com.mysql` |
| PostgreSQL | `postgresql` | `org.postgresql` |
| H2 | `h2` | `com.h2database` |

> **Note:** MySQL's driver groupId changed from `mysql` to `com.mysql` in version 8.0.31+. Always use the new coordinates.

---

## JDBC Basic Architecture

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  Your Java   │────▶│  JDBC API    │────▶│   MySQL      │
│  Application │     │  (java.sql)  │     │   Database   │
└──────────────┘     └──────────────┘     └──────────────┘
                           │
                     ┌─────┴─────┐
                     │  MySQL    │
                     │  Driver   │
                     │  (JAR)    │
                     └───────────┘
```

### Key JDBC Interfaces (all in `java.sql` package)

| Interface | Purpose |
|---|---|
| `DriverManager` | Manages database drivers and creates connections |
| `Connection` | Represents an active connection to the database |
| `Statement` | Sends SQL to the database |
| `PreparedStatement` | Sends parameterized SQL (prevents SQL injection!) |
| `ResultSet` | Holds the rows returned by a query |

---

## Download/Install MySQL JDBC Driver

Since we're using **Maven**, we don't download JARs manually. Add this to your `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>9.2.0</version>
    </dependency>
</dependencies>
```

After adding this, Maven automatically downloads the driver to `~/.m2/repository/com/mysql/mysql-connector-j/9.2.0/`.

> **No need to use `Class.forName("com.mysql.cj.jdbc.Driver")`** — modern JDBC (4.0+) auto-discovers drivers via the ServiceLoader mechanism.

---

## Java Database Connectivity Steps

Here's the complete flow, step by step:

### Step 1: Set Up the Connection URL

The JDBC URL format for MySQL:
```
jdbc:mysql://hostname:port/database_name
```

For our classicmodels database running locally:
```
jdbc:mysql://localhost:3306/classicmodels
```

### Step 2: Open a Database Connection

```java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCDemo {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/classicmodels";
        String user = "root";
        String password = "your_password_here";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connected to classicmodels database!");
            System.out.println("MySQL version: " + conn.getMetaData().getDatabaseProductVersion());
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
    }
}
```

> **Always use try-with-resources** (`try (Connection conn = ...)`) to automatically close connections. Unclosed connections cause resource leaks and can crash your application.

### Step 3: Send Statements to the Database

```java
try (Connection conn = DriverManager.getConnection(url, user, password);
     Statement stmt = conn.createStatement()) {

    ResultSet rs = stmt.executeQuery("SELECT customerNumber, customerName FROM customers LIMIT 5");

    while (rs.next()) {
        int id = rs.getInt("customerNumber");
        String name = rs.getString("customerName");
        System.out.println(id + " - " + name);
    }

} catch (SQLException e) {
    System.err.println("Error: " + e.getMessage());
}
```

### Common Methods of Statement Interface

| Method | Use When | Returns |
|---|---|---|
| `executeQuery(sql)` | SELECT statements | `ResultSet` |
| `executeUpdate(sql)` | INSERT, UPDATE, DELETE | `int` (rows affected) |
| `execute(sql)` | Any SQL / unsure of type | `boolean` |

### Step 4: Process the ResultSet

The `ResultSet` is like a cursor pointing to rows. Use `next()` to move forward.

```java
ResultSet rs = stmt.executeQuery(
    "SELECT customerName, city, country FROM customers WHERE country = 'USA'"
);

while (rs.next()) {
    // Access by column name (preferred — more readable)
    String name = rs.getString("customerName");
    String city = rs.getString("city");
    String country = rs.getString("country");

    System.out.printf("%-40s %-20s %s%n", name, city, country);
}
```

### ResultSet Getter Methods

| Method | Java Type | SQL Type |
|---|---|---|
| `getString()` | String | VARCHAR, CHAR, TEXT |
| `getInt()` | int | INT, INTEGER |
| `getLong()` | long | BIGINT |
| `getDouble()` | double | DOUBLE, FLOAT |
| `getBigDecimal()` | BigDecimal | DECIMAL |
| `getDate()` | java.sql.Date | DATE |
| `getTimestamp()` | Timestamp | DATETIME, TIMESTAMP |
| `getBoolean()` | boolean | BIT, BOOLEAN |

### Step 5: Close the Connection

With **try-with-resources**, closing is automatic. The resources are closed in reverse order of declaration:

```java
// All three are auto-closed when the try block exits
try (Connection conn = DriverManager.getConnection(url, user, password);
     Statement stmt = conn.createStatement();
     ResultSet rs = stmt.executeQuery("SELECT * FROM offices")) {

    while (rs.next()) {
        System.out.println(rs.getString("city") + ", " + rs.getString("country"));
    }
}
// rs, stmt, and conn are ALL closed here — even if an exception occurred
```

---

## Overview of Prepared Statements

**PreparedStatement** is the most important JDBC concept for real-world applications. It solves two critical problems:

1. **SQL Injection Prevention** — user input is never concatenated into SQL
2. **Performance** — the database can cache and reuse the query plan

### The Problem: SQL Injection

```java
// NEVER DO THIS — vulnerable to SQL injection!
String userInput = "'; DROP TABLE customers; --";
String sql = "SELECT * FROM customers WHERE customerName = '" + userInput + "'";
stmt.executeQuery(sql);
// This would execute: SELECT * FROM customers WHERE customerName = ''; DROP TABLE customers; --'
```

### The Solution: PreparedStatement

```java
// SAFE — parameters are escaped automatically
String sql = "SELECT * FROM customers WHERE customerName = ?";
try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
    pstmt.setString(1, userInput);  // Parameter index starts at 1
    ResultSet rs = pstmt.executeQuery();

    while (rs.next()) {
        System.out.println(rs.getString("customerName"));
    }
}
```

The `?` is a **placeholder**. The database treats the parameter as a literal value — never as SQL code.

---

## Adding Placeholders to the Statement

Each `?` in your SQL is a placeholder that must be set before execution. Index starts at **1** (not 0).

```java
String sql = "SELECT * FROM customers WHERE country = ? AND creditLimit > ?";

try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
    pstmt.setString(1, "USA");           // First ?
    pstmt.setBigDecimal(2, new BigDecimal("50000.00"));  // Second ?

    ResultSet rs = pstmt.executeQuery();
    while (rs.next()) {
        System.out.printf("%-40s $%,.2f%n",
            rs.getString("customerName"),
            rs.getBigDecimal("creditLimit"));
    }
}
```

### PreparedStatement Setter Methods

| Method | Java Type | SQL Type |
|---|---|---|
| `setString(index, value)` | String | VARCHAR |
| `setInt(index, value)` | int | INT |
| `setLong(index, value)` | long | BIGINT |
| `setDouble(index, value)` | double | DOUBLE |
| `setBigDecimal(index, value)` | BigDecimal | DECIMAL |
| `setDate(index, value)` | java.sql.Date | DATE |
| `setNull(index, sqlType)` | null | Any |

---

## Prepared Statement With DML (INSERT, UPDATE, DELETE)

### INSERT Example

```java
String sql = "INSERT INTO offices (officeCode, city, phone, addressLine1, country, postalCode, territory) "
           + "VALUES (?, ?, ?, ?, ?, ?, ?)";

try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
    pstmt.setString(1, "8");
    pstmt.setString(2, "Denver");
    pstmt.setString(3, "+1 303 555 0100");
    pstmt.setString(4, "1600 Broadway");
    pstmt.setString(5, "USA");
    pstmt.setString(6, "80202");
    pstmt.setString(7, "NA");

    int rowsInserted = pstmt.executeUpdate();
    System.out.println(rowsInserted + " row(s) inserted.");
}
```

### UPDATE Example

```java
String sql = "UPDATE employees SET VacationHours = ? WHERE employeeNumber = ?";

try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
    pstmt.setInt(1, 30);
    pstmt.setInt(2, 1002);

    int rowsUpdated = pstmt.executeUpdate();
    System.out.println(rowsUpdated + " row(s) updated.");
}
```

### DELETE Example

```java
String sql = "DELETE FROM offices WHERE officeCode = ?";

try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
    pstmt.setString(1, "8");

    int rowsDeleted = pstmt.executeUpdate();
    System.out.println(rowsDeleted + " row(s) deleted.");
}
```

---

## Separate Class for Queries

Real applications don't put SQL in `main()`. Create a dedicated class to manage database connections and queries.

### ConnectionManager.java

```java
package com.perscholas.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {
    private static final String URL = "jdbc:mysql://localhost:3306/classicmodels";
    private static final String USER = "root";
    private static final String PASSWORD = "your_password_here";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
```

### CustomerQueries.java

```java
package com.perscholas.queries;

import com.perscholas.db.ConnectionManager;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerQueries {

    public List<String> getCustomersByCountry(String country) throws SQLException {
        String sql = "SELECT customerName FROM customers WHERE country = ? ORDER BY customerName";
        List<String> names = new ArrayList<>();

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, country);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                names.add(rs.getString("customerName"));
            }
        }
        return names;
    }

    public int countCustomersAboveCreditLimit(BigDecimal limit) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM customers WHERE creditLimit > ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBigDecimal(1, limit);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }
}
```

### App.java (Main class)

```java
package com.perscholas;

import com.perscholas.queries.CustomerQueries;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class App {
    public static void main(String[] args) {
        CustomerQueries queries = new CustomerQueries();

        try {
            List<String> usaCustomers = queries.getCustomersByCountry("USA");
            System.out.println("USA Customers: " + usaCustomers.size());
            usaCustomers.forEach(System.out::println);

            int highCredit = queries.countCustomersAboveCreditLimit(new BigDecimal("100000"));
            System.out.println("\nCustomers with credit > $100,000: " + highCredit);

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }
}
```

---

## Complete Working Example with classicmodels

Here's a full runnable example that queries several tables:

```java
package com.perscholas;

import java.sql.*;

public class ClassicModelsDemo {
    private static final String URL = "jdbc:mysql://localhost:3306/classicmodels";
    private static final String USER = "root";
    private static final String PASSWORD = "your_password_here";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {

            // 1. List all offices
            System.out.println("=== OFFICES ===");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT officeCode, city, country FROM offices")) {
                while (rs.next()) {
                    System.out.printf("  Office %s: %s, %s%n",
                        rs.getString("officeCode"),
                        rs.getString("city"),
                        rs.getString("country"));
                }
            }

            // 2. Find employees in a specific office (parameterized)
            System.out.println("\n=== EMPLOYEES IN SAN FRANCISCO ===");
            String empSql = "SELECT firstName, lastName, jobTitle FROM employees WHERE officeCode = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(empSql)) {
                pstmt.setString(1, "1");  // Office code 1 = San Francisco
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    System.out.printf("  %s %s — %s%n",
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("jobTitle"));
                }
            }

            // 3. Product lines and product counts
            System.out.println("\n=== PRODUCTS PER LINE ===");
            String prodSql = """
                SELECT pl.productLine, COUNT(p.productCode) AS productCount
                FROM productlines pl
                LEFT JOIN products p ON pl.productLine = p.productLine
                GROUP BY pl.productLine
                ORDER BY productCount DESC
                """;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(prodSql)) {
                while (rs.next()) {
                    System.out.printf("  %-20s %d products%n",
                        rs.getString("productLine"),
                        rs.getInt("productCount"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
```

---

## Key Takeaways

1. **JDBC** is Java's built-in API for database access — no framework required
2. The JDBC driver is a **Maven dependency** — never download JARs manually
3. **Always use PreparedStatement** with `?` placeholders — never concatenate user input into SQL
4. **Always use try-with-resources** to auto-close Connection, Statement, and ResultSet
5. Separate your database connection logic from your query logic
6. `executeQuery()` for SELECT, `executeUpdate()` for INSERT/UPDATE/DELETE

---

## Version Reference

| Component | Value |
|---|---|
| Java | 21 LTS |
| MySQL Connector/J | `com.mysql:mysql-connector-j:9.2.0` |
| JDBC URL | `jdbc:mysql://localhost:3306/classicmodels` |
| Driver class (auto-loaded) | `com.mysql.cj.jdbc.Driver` |
