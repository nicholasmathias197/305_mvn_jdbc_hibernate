# Lesson 4: Hibernate & JPA (Java Persistence API)

---

## Object Relational Mapping (ORM)

**ORM** (Object Relational Mapping) is a technique that maps Java objects to database tables automatically. Instead of writing SQL and manually converting `ResultSet` rows into objects (like we did with JDBC), an ORM framework does it for you.

### The Mismatch Problem

| Java World | Database World |
|---|---|
| Objects (Customer, Employee) | Tables (customers, employees) |
| Fields (customerName) | Columns (customerName) |
| References (customer.getOrders()) | Foreign keys (customerNumber) |
| Inheritance (class Manager extends Employee) | No built-in concept |
| Collections (List, Set) | Join tables |

This difference is called the **Object-Relational Impedance Mismatch**. ORM frameworks bridge this gap.

---

## Java Persistence API (JPA)

**JPA** is a **specification** (a set of interfaces and annotations) that defines how Java objects should be mapped to database tables. JPA itself is NOT code — it's a standard.

### Key Point: JPA vs Hibernate

| | JPA | Hibernate |
|---|---|---|
| **What** | Specification (interfaces) | Implementation (actual code) |
| **Analogy** | JDBC interface `Connection` | MySQL's implementation of `Connection` |
| **Package** | `jakarta.persistence.*` | `org.hibernate.*` |
| **Can run alone?** | No — needs an implementation | Yes — implements JPA |

> **Important:** JPA moved from `javax.persistence` to `jakarta.persistence` starting with Jakarta EE 9. All modern versions use `jakarta.*`. If you see `javax.persistence` in old tutorials, it's outdated.

---

## Overview of Java Hibernate

**Hibernate** is the most popular JPA implementation. It's a full ORM framework that:

- Maps Java classes to database tables using annotations
- Generates SQL automatically (SELECT, INSERT, UPDATE, DELETE)
- Manages object lifecycles (when to save, when to update)
- Provides a query language (HQL) that works with objects, not tables
- Handles caching for better performance

### Why Hibernate Over Raw JDBC?

| Raw JDBC | Hibernate |
|---|---|
| Write SQL for every operation | SQL generated automatically |
| Manually map ResultSet → Object | Automatic mapping via annotations |
| Manage connections yourself | Connection pooling built-in |
| Database-specific SQL | Database-agnostic (switch MySQL → PostgreSQL easily) |
| Boilerplate code everywhere | Clean, minimal code |

---

## Hibernate with JPA

In modern Java development, you **always use JPA annotations** with Hibernate as the implementation. This means:

- Your entity classes use `@Entity`, `@Table`, `@Column` from `jakarta.persistence`
- Your queries use JPA's `EntityManager`
- Hibernate runs behind the scenes, but your code is portable

If you ever need to switch from Hibernate to EclipseLink (another JPA provider), your code stays the same.

---

## Hibernate Application Architecture

```
┌─────────────────────┐
│  Java Application    │
│  (Your Code)         │
├─────────────────────┤
│  JPA API             │  ← EntityManager, @Entity, etc.
│  (jakarta.persistence)│
├─────────────────────┤
│  Hibernate ORM       │  ← Implements JPA, generates SQL
│  (org.hibernate)     │
├─────────────────────┤
│  JDBC Driver         │  ← MySQL Connector/J
├─────────────────────┤
│  MySQL Database      │  ← classicmodels
└─────────────────────┘
```

### Persistent Objects

A **persistent object** (or **entity**) is a Java object that Hibernate tracks and synchronizes with the database. It has three states:

| State | Description |
|---|---|
| **Transient** | `new Customer()` — just created, Hibernate doesn't know about it |
| **Persistent** | Saved to DB and tracked by Hibernate — changes auto-sync |
| **Detached** | Was persistent, but the Session/EntityManager is closed |

### Configuration

Hibernate needs to know: what database? what driver? what credentials? This is configured in either:
- `hibernate.cfg.xml` (traditional)
- `persistence.xml` (JPA standard — preferred)

---

## Hibernate Configuration Files

### Option 1: `persistence.xml` (JPA Standard — Recommended)

Create this file at `src/main/resources/META-INF/persistence.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence
             https://jakarta.ee/xml/ns/persistence/persistence_3_2.xsd"
             version="3.2">

    <persistence-unit name="classicmodels-pu" transaction-type="RESOURCE_LOCAL">
        <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>

        <properties>
            <!-- Database connection -->
            <property name="jakarta.persistence.jdbc.url"
                      value="jdbc:mysql://localhost:3306/classicmodels"/>
            <property name="jakarta.persistence.jdbc.user" value="root"/>
            <property name="jakarta.persistence.jdbc.password" value="your_password_here"/>
            <property name="jakarta.persistence.jdbc.driver" value="com.mysql.cj.jdbc.Driver"/>

            <!-- Hibernate settings -->
            <property name="hibernate.dialect" value="org.hibernate.dialect.MySQLDialect"/>
            <property name="hibernate.show_sql" value="true"/>
            <property name="hibernate.format_sql" value="true"/>
            <property name="hibernate.hbm2ddl.auto" value="validate"/>
        </properties>
    </persistence-unit>
</persistence>
```

### Option 2: `hibernate.cfg.xml` (Hibernate-specific)

Create at `src/main/resources/hibernate.cfg.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE hibernate-configuration PUBLIC
    "-//Hibernate/Hibernate Configuration DTD 3.0//EN"
    "http://www.hibernate.org/dtd/hibernate-configuration-3.0.dtd">

<hibernate-configuration>
    <session-factory>
        <property name="hibernate.connection.url">jdbc:mysql://localhost:3306/classicmodels</property>
        <property name="hibernate.connection.username">root</property>
        <property name="hibernate.connection.password">your_password_here</property>
        <property name="hibernate.connection.driver_class">com.mysql.cj.jdbc.Driver</property>
        <property name="hibernate.dialect">org.hibernate.dialect.MySQLDialect</property>
        <property name="hibernate.show_sql">true</property>
        <property name="hibernate.format_sql">true</property>
        <property name="hibernate.hbm2ddl.auto">validate</property>

        <!-- Register entity classes -->
        <mapping class="com.perscholas.model.Customer"/>
        <mapping class="com.perscholas.model.Employee"/>
    </session-factory>
</hibernate-configuration>
```

---

## Specify Exact Database Dialect

The **dialect** tells Hibernate which SQL flavor to generate. Hibernate 6+ auto-detects the dialect in most cases, but you can be explicit:

| Database | Dialect Class |
|---|---|
| MySQL 8+ | `org.hibernate.dialect.MySQLDialect` |
| PostgreSQL | `org.hibernate.dialect.PostgreSQLDialect` |
| H2 | `org.hibernate.dialect.H2Dialect` |
| Oracle | `org.hibernate.dialect.OracleDialect` |
| SQL Server | `org.hibernate.dialect.SQLServerDialect` |

> In Hibernate 6+, the old version-specific dialects (`MySQL8Dialect`, `MySQL5InnoDBDialect`) are **deprecated**. Just use `MySQLDialect` — Hibernate detects the version automatically.

---

## Hibernate Configuration Properties

| Property | Values | Meaning |
|---|---|---|
| `hibernate.show_sql` | `true` / `false` | Print generated SQL to console |
| `hibernate.format_sql` | `true` / `false` | Pretty-print the SQL |
| `hibernate.hbm2ddl.auto` | See below | How Hibernate manages the schema |
| `hibernate.dialect` | Dialect class name | Which SQL dialect to use |

### `hbm2ddl.auto` Values

| Value | What It Does | When to Use |
|---|---|---|
| `validate` | Checks entities match the DB schema, throws error if not | **Production, and this course** |
| `update` | Adds new columns/tables, never drops | Development |
| `create` | Drops and recreates tables on every startup | Testing only |
| `create-drop` | Same as `create`, but also drops on shutdown | Unit tests |
| `none` | Does nothing | Production |

> **For this course, use `validate`** — our `classicmodels` schema already exists. We don't want Hibernate modifying it.

---

## Setting up Hibernate

### Maven Dependencies (`pom.xml`)

```xml
<properties>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>

<dependencies>
    <!-- Hibernate 6 (includes JPA API) -->
    <dependency>
        <groupId>org.hibernate.orm</groupId>
        <artifactId>hibernate-core</artifactId>
        <version>6.6.5.Final</version>
    </dependency>

    <!-- MySQL JDBC Driver -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>9.2.0</version>
    </dependency>

    <!-- JUnit 5 for testing -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.11.4</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

> **Note:** Hibernate 6 bundles the Jakarta Persistence API, so you don't need a separate JPA dependency.

---

## Entity/POJO/Model Classes

An **entity** is a POJO annotated with JPA annotations so Hibernate knows how to map it to a table.

### Customer Entity

```java
package com.perscholas.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @Column(name = "customerNumber")
    private int customerNumber;

    @Column(name = "customerName", nullable = false, length = 50)
    private String customerName;

    @Column(name = "contactLastName", nullable = false, length = 50)
    private String contactLastName;

    @Column(name = "contactFirstName", nullable = false, length = 50)
    private String contactFirstName;

    @Column(name = "phone", nullable = false, length = 50)
    private String phone;

    @Column(name = "addressLine1", nullable = false, length = 50)
    private String addressLine1;

    @Column(name = "addressLine2", length = 50)
    private String addressLine2;

    @Column(name = "city", nullable = false, length = 50)
    private String city;

    @Column(name = "state", length = 50)
    private String state;

    @Column(name = "postalCode", length = 15)
    private String postalCode;

    @Column(name = "country", nullable = false, length = 50)
    private String country;

    @Column(name = "salesRepEmployeeNumber")
    private Integer salesRepEmployeeNumber;

    @Column(name = "creditLimit", precision = 10, scale = 2)
    private BigDecimal creditLimit;

    // No-arg constructor required by JPA
    public Customer() {}

    // Getters and setters...
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
        return "Customer{" + customerNumber + ", '" + customerName + "', " + city + ", " + country + "}";
    }
}
```

---

## Hibernate Application Workflow

```
1. Create EntityManagerFactory (once per application)
2. Create EntityManager (once per operation/transaction)
3. Begin transaction
4. Perform operations (persist, find, merge, remove)
5. Commit transaction
6. Close EntityManager
7. Close EntityManagerFactory (on app shutdown)
```

---

## Hibernate Annotations

| Annotation | Purpose |
|---|---|
| `@Entity` | Marks a class as a database entity |
| `@Table(name = "...")` | Maps to a specific table (optional if class name matches) |
| `@Id` | Marks the primary key field |
| `@GeneratedValue` | Auto-generate PK values (not used here — classicmodels has manual PKs) |
| `@Column(name = "...")` | Maps a field to a specific column |
| `@Transient` | Field is NOT mapped to any column (ignored by Hibernate) |

---

## Session Factory in Hibernate

The **SessionFactory** (Hibernate-specific) or **EntityManagerFactory** (JPA standard) is the heavyweight object that creates sessions/entity managers. You create it **once** and reuse it.

### Using JPA EntityManagerFactory (Recommended)

```java
package com.perscholas.util;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAUtil {
    private static final EntityManagerFactory emf =
        Persistence.createEntityManagerFactory("classicmodels-pu");

    public static EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
```

### Using Hibernate SessionFactory (Alternative)

```java
package com.perscholas.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        return new Configuration()
            .configure("hibernate.cfg.xml")
            .addAnnotatedClass(com.perscholas.model.Customer.class)
            .addAnnotatedClass(com.perscholas.model.Employee.class)
            .buildSessionFactory();
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && sessionFactory.isOpen()) {
            sessionFactory.close();
        }
    }
}
```

---

## Session in Hibernate / EntityManager in JPA

The **Session** (Hibernate) or **EntityManager** (JPA) is the main object you use for database operations. It's lightweight and short-lived.

### Important Session/EntityManager Methods

| JPA (EntityManager) | Hibernate (Session) | What It Does |
|---|---|---|
| `persist(entity)` | `save(entity)` | INSERT — save a new entity |
| `find(Class, id)` | `get(Class, id)` | SELECT by primary key |
| `merge(entity)` | `merge(entity)` | UPDATE — sync detached entity |
| `remove(entity)` | `delete(entity)` | DELETE — remove entity |
| `createQuery(hql)` | `createQuery(hql)` | Create an HQL query |

---

## Complete CRUD Example with EntityManager

```java
package com.perscholas;

import com.perscholas.model.Customer;
import com.perscholas.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.util.List;

public class HibernateApp {
    public static void main(String[] args) {
        EntityManagerFactory emf = JPAUtil.getEntityManagerFactory();

        // --- READ: Find by ID ---
        try (EntityManager em = emf.createEntityManager()) {
            Customer customer = em.find(Customer.class, 103);
            System.out.println("Found: " + customer);
        }

        // --- READ: Query all USA customers ---
        try (EntityManager em = emf.createEntityManager()) {
            List<Customer> usaCustomers = em.createQuery(
                "SELECT c FROM Customer c WHERE c.country = :country ORDER BY c.customerName",
                Customer.class
            ).setParameter("country", "USA").getResultList();

            System.out.println("\nUSA Customers: " + usaCustomers.size());
            usaCustomers.forEach(c -> System.out.println("  " + c));
        }

        // --- CREATE: Insert new customer ---
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Customer newCustomer = new Customer();
            newCustomer.setCustomerNumber(999);
            newCustomer.setCustomerName("Per Scholas Training Co.");
            newCustomer.setContactLastName("Smith");
            newCustomer.setContactFirstName("Jane");
            newCustomer.setPhone("555-0199");
            newCustomer.setAddressLine1("100 Learning Ave");
            newCustomer.setCity("Denver");
            newCustomer.setCountry("USA");
            newCustomer.setCreditLimit(new BigDecimal("50000.00"));

            em.persist(newCustomer);
            em.getTransaction().commit();
            System.out.println("\nInserted customer 999");
        }

        // --- UPDATE: Modify credit limit ---
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Customer customer = em.find(Customer.class, 999);
            customer.setCreditLimit(new BigDecimal("75000.00"));
            // No explicit save needed — Hibernate auto-detects changes to persistent objects!

            em.getTransaction().commit();
            System.out.println("Updated customer 999's credit limit");
        }

        // --- DELETE: Remove test customer ---
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Customer customer = em.find(Customer.class, 999);
            em.remove(customer);

            em.getTransaction().commit();
            System.out.println("Deleted customer 999");
        }

        JPAUtil.shutdown();
    }
}
```

> **Key insight:** When an entity is **persistent** (fetched within an active transaction), Hibernate automatically detects changes and generates UPDATE SQL at commit time. This is called **dirty checking**.

---

## Hibernate Query Object / Hibernate Query Language (HQL)

**HQL** (Hibernate Query Language) looks like SQL but operates on **entity classes and fields**, not tables and columns.

### SQL vs HQL

| SQL | HQL |
|---|---|
| `SELECT * FROM customers` | `FROM Customer` |
| `SELECT * FROM customers WHERE country = 'USA'` | `FROM Customer WHERE country = 'USA'` |
| `SELECT customerName FROM customers` | `SELECT c.customerName FROM Customer c` |
| Uses table/column names | Uses class/field names |

---

## HQL Interfaces

| Interface | Purpose |
|---|---|
| `TypedQuery<T>` | A type-safe query that returns entities of type T |
| `Query` | A general query (not type-safe — avoid when possible) |

---

## Execution HQL Queries

### Select All

```java
List<Customer> customers = em.createQuery("FROM Customer", Customer.class)
    .getResultList();
```

### With WHERE clause and parameters

```java
List<Customer> customers = em.createQuery(
    "FROM Customer c WHERE c.country = :country AND c.creditLimit > :limit",
    Customer.class)
    .setParameter("country", "USA")
    .setParameter("limit", new BigDecimal("50000"))
    .getResultList();
```

### Select specific fields

```java
List<String> names = em.createQuery(
    "SELECT c.customerName FROM Customer c WHERE c.city = :city",
    String.class)
    .setParameter("city", "NYC")
    .getResultList();
```

### Single result

```java
Long count = em.createQuery(
    "SELECT COUNT(c) FROM Customer c WHERE c.country = :country",
    Long.class)
    .setParameter("country", "France")
    .getSingleResult();
```

### Pagination

```java
List<Customer> page = em.createQuery("FROM Customer c ORDER BY c.customerName", Customer.class)
    .setFirstResult(0)   // offset (skip 0 rows)
    .setMaxResults(10)    // limit (return 10 rows)
    .getResultList();
```

---

## HQL Methods

| Method | Returns | Use When |
|---|---|---|
| `getResultList()` | `List<T>` | Expecting multiple results |
| `getSingleResult()` | `T` | Expecting exactly one result (throws if 0 or >1) |
| `setParameter(name, value)` | `Query` | Bind a named parameter (`:name`) |
| `setFirstResult(offset)` | `Query` | Pagination — skip rows |
| `setMaxResults(limit)` | `Query` | Pagination — limit rows returned |
| `executeUpdate()` | `int` | For UPDATE/DELETE HQL statements |

---

## Problem with HQL and SQL

### SQL Injection in HQL

Just like JDBC, **never concatenate strings** into HQL:

```java
// BAD — vulnerable to HQL injection
String hql = "FROM Customer WHERE customerName = '" + userInput + "'";
```

```java
// GOOD — use named parameters
String hql = "FROM Customer WHERE customerName = :name";
em.createQuery(hql, Customer.class).setParameter("name", userInput);
```

### Other Issues
- HQL is **not compile-time checked** — typos in field names cause runtime errors
- For type-safe queries, consider the **JPA Criteria API** (more verbose but compile-safe)

---

## Overview of Hibernate Named Queries

**Named queries** are HQL queries defined once on the entity class using annotations, then referenced by name throughout your application.

### Defining Named Queries

```java
@Entity
@Table(name = "customers")
@NamedQueries({
    @NamedQuery(
        name = "Customer.findByCountry",
        query = "FROM Customer c WHERE c.country = :country ORDER BY c.customerName"
    ),
    @NamedQuery(
        name = "Customer.findHighCredit",
        query = "FROM Customer c WHERE c.creditLimit > :limit ORDER BY c.creditLimit DESC"
    ),
    @NamedQuery(
        name = "Customer.countByCountry",
        query = "SELECT COUNT(c) FROM Customer c WHERE c.country = :country"
    )
})
public class Customer {
    // ... fields, getters, setters
}
```

### Using Named Queries

```java
// Find all French customers
List<Customer> french = em.createNamedQuery("Customer.findByCountry", Customer.class)
    .setParameter("country", "France")
    .getResultList();

// Count USA customers
Long usaCount = em.createNamedQuery("Customer.countByCountry", Long.class)
    .setParameter("country", "USA")
    .getSingleResult();
```

---

## Advantages of Named Queries

| Advantage | Explanation |
|---|---|
| **Validated at startup** | Hibernate checks the HQL syntax when the app starts — errors caught early |
| **Reusable** | Define once, use anywhere by name |
| **Centralized** | All queries for an entity are in one place |
| **Cacheable** | Hibernate can pre-compile named queries for better performance |
| **Organized** | Query naming convention (`Entity.operation`) keeps things consistent |

---

## JDBC vs. Java Hibernate

| Feature | JDBC | Hibernate |
|---|---|---|
| **SQL** | Write it yourself | Auto-generated |
| **Mapping** | Manual (`rs.getString()`) | Automatic (`@Entity`, `@Column`) |
| **Boilerplate** | High — connection, statement, result set, close | Low — just annotations and EntityManager |
| **Database portable** | You write vendor-specific SQL | Hibernate generates correct SQL per dialect |
| **Caching** | None — every query hits the DB | First-level cache (Session) + optional second-level |
| **Relationships** | Manual JOINs | `@OneToMany`, `@ManyToOne` annotations |
| **Transactions** | `conn.setAutoCommit(false)`, manual | `em.getTransaction().begin()/.commit()` |
| **Learning curve** | Simple concepts, verbose code | More concepts, less code |
| **Performance control** | Full control — you write the SQL | Must understand lazy loading, N+1 problem |
| **Best for** | Simple apps, learning SQL | Complex apps with many entities and relationships |

### When to Use JDBC
- Small applications with few tables
- Need maximum performance control
- Learning how databases work

### When to Use Hibernate
- Applications with many related entities
- Need database portability
- Want rapid development with less boilerplate

---

## Key Takeaways

1. **JPA** is the specification; **Hibernate** is the implementation
2. Use `jakarta.persistence.*` annotations (not `javax.*`)
3. `EntityManagerFactory` is created once; `EntityManager` is per-operation
4. Hibernate auto-detects changes to persistent objects (**dirty checking**)
5. **HQL** operates on classes/fields, not tables/columns
6. Always use **named parameters** (`:paramName`) — never concatenate strings
7. **Named queries** are validated at startup and reusable across the app
8. `hibernate.hbm2ddl.auto = validate` for existing databases like classicmodels

---

## Version Reference

| Component | Version | Coordinates |
|---|---|---|
| Hibernate ORM | 6.6.5.Final | `org.hibernate.orm:hibernate-core` |
| Jakarta Persistence API | 3.2 | Bundled with Hibernate 6 |
| MySQL Connector/J | 9.2.0 | `com.mysql:mysql-connector-j` |
| Java | 21 LTS | |
