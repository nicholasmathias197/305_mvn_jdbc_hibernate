# Lesson 7: H2 In-Memory Database

---

## In-Memory Database

An **in-memory database** runs entirely in RAM — no files on disk, no installation, no separate server process. It's created when your application starts and destroyed when it stops.

### Why Use an In-Memory Database?

| Use Case | Why |
|---|---|
| **Unit testing** | Each test gets a fresh database — no leftover data from previous runs |
| **Integration testing** | Test Hibernate entities and queries without needing MySQL running |
| **Prototyping** | Spin up a database instantly to try ideas |
| **CI/CD pipelines** | No database server needed on the build machine |
| **Development** | Work offline without a MySQL installation |

### In-Memory vs. Real Database

| Feature | In-Memory (H2) | Server (MySQL) |
|---|---|---|
| Speed | Extremely fast (RAM) | Fast (disk + cache) |
| Persistence | Data lost on shutdown | Data persists |
| Setup | Zero — just a Maven dependency | Install, configure, create users |
| Scale | Limited by RAM | Production-ready |
| Use | Testing & development | Production |

---

## H2 In-Memory Database

**H2** is the most popular in-memory database for Java. It:

- Is written in pure Java
- Supports most SQL standards
- Has a MySQL compatibility mode
- Works with JDBC, JPA, and Hibernate
- Comes as a single JAR file (~2.5 MB)
- Includes a built-in web console for browsing data

---

## H2 Database — Execution Modes

H2 can run in three modes:

| Mode | JDBC URL | Description |
|---|---|---|
| **In-Memory** | `jdbc:h2:mem:testdb` | Data lives only in RAM. Gone when the connection closes. |
| **Embedded** | `jdbc:h2:~/mydb` | Stores data in files on disk. No separate server needed. |
| **Server** | `jdbc:h2:tcp://localhost/~/mydb` | Runs as a separate process. Multiple apps can connect. |

**For this course, we use In-Memory mode for testing.**

---

## Install H2-In Memory Database

There's nothing to install! Just add the Maven dependency and H2 is available.

---

## H2 Database Commands

H2 supports standard SQL. In MySQL compatibility mode, most of your classicmodels SQL works unchanged.

### MySQL Compatibility Mode

Add `;MODE=MySQL` to the JDBC URL:
```
jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1
```

| URL Parameter | Purpose |
|---|---|
| `mem:testdb` | In-memory database named "testdb" |
| `MODE=MySQL` | H2 behaves like MySQL (data types, functions, syntax) |
| `DB_CLOSE_DELAY=-1` | Keep the database alive as long as the JVM runs (default: database is destroyed when the last connection closes) |

### Common H2 SQL

```sql
-- Create a table (same as MySQL)
CREATE TABLE IF NOT EXISTS customers (
    customerNumber INT PRIMARY KEY,
    customerName VARCHAR(50) NOT NULL,
    country VARCHAR(50) NOT NULL
);

-- Insert data (same as MySQL)
INSERT INTO customers VALUES (1, 'Test Customer', 'USA');

-- Query data (same as MySQL)
SELECT * FROM customers WHERE country = 'USA';

-- Show all tables
SHOW TABLES;
```

### H2 Web Console (Optional)

H2 includes a web-based SQL console. To use it during development:

```java
// Start the H2 web console (opens in browser on port 8082)
org.h2.tools.Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
```

Then open `http://localhost:8082` in your browser and connect with your JDBC URL.

---

## H2 Database Maven Dependency

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.3.232</version>
    <scope>test</scope>
</dependency>
```

> **`<scope>test</scope>`** — H2 is only available during testing. Your production code uses MySQL. This is the standard pattern: MySQL for production, H2 for tests.

---

## Using H2 with JDBC

Here's a simple JDBC example using H2 in-memory:

```java
package com.perscholas;

import java.sql.*;

public class H2JDBCDemo {
    public static void main(String[] args) throws SQLException {
        // No installation needed — this just works!
        String url = "jdbc:h2:mem:testdb;MODE=MySQL";
        String user = "sa";       // H2 default username
        String password = "";     // H2 default (empty) password

        try (Connection conn = DriverManager.getConnection(url, user, password)) {

            // Create a table
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("""
                    CREATE TABLE customers (
                        customerNumber INT PRIMARY KEY,
                        customerName VARCHAR(50) NOT NULL,
                        city VARCHAR(50),
                        country VARCHAR(50) NOT NULL,
                        creditLimit DECIMAL(10,2)
                    )
                    """);
                System.out.println("Table created.");
            }

            // Insert some data
            String insertSql = "INSERT INTO customers VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, 103);
                pstmt.setString(2, "Atelier graphique");
                pstmt.setString(3, "Nantes");
                pstmt.setString(4, "France");
                pstmt.setBigDecimal(5, new java.math.BigDecimal("21000.00"));
                pstmt.executeUpdate();

                pstmt.setInt(1, 112);
                pstmt.setString(2, "Signal Gift Stores");
                pstmt.setString(3, "Las Vegas");
                pstmt.setString(4, "USA");
                pstmt.setBigDecimal(5, new java.math.BigDecimal("71800.00"));
                pstmt.executeUpdate();

                System.out.println("Data inserted.");
            }

            // Query
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM customers")) {
                while (rs.next()) {
                    System.out.printf("%d — %s — %s%n",
                        rs.getInt("customerNumber"),
                        rs.getString("customerName"),
                        rs.getString("country"));
                }
            }
        }
        // Database is destroyed here — connection closed, data gone!
    }
}
```

---

## Using H2 with Hibernate for Testing

This is the primary use case: run your Hibernate entity tests without MySQL.

### Test `persistence.xml`

Create `src/test/resources/META-INF/persistence.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence
             https://jakarta.ee/xml/ns/persistence/persistence_3_2.xsd"
             version="3.2">

    <persistence-unit name="classicmodels-test-pu" transaction-type="RESOURCE_LOCAL">
        <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>

        <properties>
            <!-- H2 in-memory database -->
            <property name="jakarta.persistence.jdbc.url"
                      value="jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1"/>
            <property name="jakarta.persistence.jdbc.user" value="sa"/>
            <property name="jakarta.persistence.jdbc.password" value=""/>
            <property name="jakarta.persistence.jdbc.driver" value="org.h2.Driver"/>

            <!-- Hibernate creates tables from your entities -->
            <property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"/>
            <property name="hibernate.hbm2ddl.auto" value="create-drop"/>
            <property name="hibernate.show_sql" value="true"/>
            <property name="hibernate.format_sql" value="true"/>
        </properties>
    </persistence-unit>
</persistence>
```

**Key differences from the MySQL `persistence.xml`:**
- URL: `jdbc:h2:mem:testdb` instead of `jdbc:mysql://localhost:3306/classicmodels`
- Driver: `org.h2.Driver` instead of `com.mysql.cj.jdbc.Driver`
- Dialect: `H2Dialect` instead of `MySQLDialect`
- `hbm2ddl.auto`: `create-drop` — Hibernate creates all tables from your `@Entity` classes on startup and drops them on shutdown

### JUnit 5 Test Example

```java
package com.perscholas.dao;

import com.perscholas.model.Customer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomerEntityTest {

    private static EntityManagerFactory emf;
    private EntityManager em;

    @BeforeAll
    static void setUpFactory() {
        // Uses the test persistence.xml (H2 in-memory)
        emf = Persistence.createEntityManagerFactory("classicmodels-test-pu");
    }

    @BeforeEach
    void setUp() {
        em = emf.createEntityManager();

        // Insert test data before each test
        em.getTransaction().begin();

        Customer c1 = new Customer();
        c1.setCustomerNumber(103);
        c1.setCustomerName("Atelier graphique");
        c1.setContactLastName("Schmitt");
        c1.setContactFirstName("Carine");
        c1.setPhone("40.32.2555");
        c1.setAddressLine1("54, rue Royale");
        c1.setCity("Nantes");
        c1.setCountry("France");
        c1.setCreditLimit(new BigDecimal("21000.00"));
        em.persist(c1);

        Customer c2 = new Customer();
        c2.setCustomerNumber(112);
        c2.setCustomerName("Signal Gift Stores");
        c2.setContactLastName("King");
        c2.setContactFirstName("Jean");
        c2.setPhone("7025551838");
        c2.setAddressLine1("8489 Strong St.");
        c2.setCity("Las Vegas");
        c2.setState("NV");
        c2.setCountry("USA");
        c2.setCreditLimit(new BigDecimal("71800.00"));
        em.persist(c2);

        em.getTransaction().commit();
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Customer").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    @AfterAll
    static void tearDownFactory() {
        emf.close();
    }

    @Test
    @DisplayName("Find customer by ID returns correct customer")
    void testFindById() {
        Customer customer = em.find(Customer.class, 103);

        assertNotNull(customer);
        assertEquals("Atelier graphique", customer.getCustomerName());
        assertEquals("France", customer.getCountry());
    }

    @Test
    @DisplayName("Find customer by ID returns null for non-existent ID")
    void testFindByIdNotFound() {
        Customer customer = em.find(Customer.class, 99999);
        assertNull(customer);
    }

    @Test
    @DisplayName("HQL query finds customers by country")
    void testFindByCountry() {
        List<Customer> usaCustomers = em.createQuery(
            "FROM Customer c WHERE c.country = :country", Customer.class)
            .setParameter("country", "USA")
            .getResultList();

        assertEquals(1, usaCustomers.size());
        assertEquals("Signal Gift Stores", usaCustomers.get(0).getCustomerName());
    }

    @Test
    @DisplayName("Persist and retrieve a new customer")
    void testInsertCustomer() {
        em.getTransaction().begin();

        Customer newCustomer = new Customer();
        newCustomer.setCustomerNumber(999);
        newCustomer.setCustomerName("Test Company");
        newCustomer.setContactLastName("Doe");
        newCustomer.setContactFirstName("John");
        newCustomer.setPhone("555-0000");
        newCustomer.setAddressLine1("123 Test St");
        newCustomer.setCity("Denver");
        newCustomer.setCountry("USA");
        newCustomer.setCreditLimit(new BigDecimal("50000.00"));

        em.persist(newCustomer);
        em.getTransaction().commit();

        // Verify it was saved
        Customer retrieved = em.find(Customer.class, 999);
        assertNotNull(retrieved);
        assertEquals("Test Company", retrieved.getCustomerName());
        assertEquals(new BigDecimal("50000.00"), retrieved.getCreditLimit());
    }

    @Test
    @DisplayName("Update customer credit limit")
    void testUpdateCustomer() {
        em.getTransaction().begin();

        Customer customer = em.find(Customer.class, 103);
        customer.setCreditLimit(new BigDecimal("99999.99"));

        em.getTransaction().commit();

        // Re-read and verify
        Customer updated = em.find(Customer.class, 103);
        assertEquals(new BigDecimal("99999.99"), updated.getCreditLimit());
    }

    @Test
    @DisplayName("Delete customer removes from database")
    void testDeleteCustomer() {
        em.getTransaction().begin();

        Customer customer = em.find(Customer.class, 112);
        em.remove(customer);

        em.getTransaction().commit();

        assertNull(em.find(Customer.class, 112));
    }

    @Test
    @DisplayName("Count customers returns correct number")
    void testCountCustomers() {
        Long count = em.createQuery("SELECT COUNT(c) FROM Customer c", Long.class)
            .getSingleResult();

        assertEquals(2L, count);
    }
}
```

---

## Complete Test Project POM

Here's a `pom.xml` that supports both MySQL (production) and H2 (testing):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>
    <groupId>com.perscholas</groupId>
    <artifactId>classicmodels-hibernate</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Hibernate 6 (includes Jakarta Persistence API) -->
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>6.6.5.Final</version>
        </dependency>

        <!-- MySQL Driver (production) -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>9.2.0</version>
        </dependency>

        <!-- H2 Database (testing only) -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>2.3.232</version>
            <scope>test</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.36</version>
            <scope>provided</scope>
        </dependency>

        <!-- JUnit 5 -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.11.4</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

### Project Structure

```
classicmodels-hibernate/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/perscholas/
│   │   │   ├── App.java
│   │   │   ├── model/
│   │   │   │   ├── Customer.java
│   │   │   │   ├── Employee.java
│   │   │   │   ├── Order.java
│   │   │   │   └── Product.java
│   │   │   ├── dao/
│   │   │   │   ├── CustomerDAO.java
│   │   │   │   └── CustomerDAOImpl.java
│   │   │   └── util/
│   │   │       └── JPAUtil.java
│   │   └── resources/
│   │       └── META-INF/
│   │           └── persistence.xml          ← MySQL config
│   └── test/
│       ├── java/com/perscholas/
│       │   └── dao/
│       │       └── CustomerEntityTest.java
│       └── resources/
│           └── META-INF/
│               └── persistence.xml          ← H2 config (overrides main)
```

When you run `mvn test`, Maven uses the **test** `persistence.xml` (H2). When you run the app normally, it uses the **main** `persistence.xml` (MySQL).

---

## Loading classicmodels Test Data

For more realistic tests, you can load the classicmodels data into H2 using a SQL script:

### Create `src/test/resources/test-data.sql`

```sql
INSERT INTO customers (customerNumber, customerName, contactLastName, contactFirstName,
    phone, addressLine1, city, country, creditLimit)
VALUES
    (103, 'Atelier graphique', 'Schmitt', 'Carine', '40.32.2555', '54, rue Royale', 'Nantes', 'France', 21000.00),
    (112, 'Signal Gift Stores', 'King', 'Jean', '7025551838', '8489 Strong St.', 'Las Vegas', 'USA', 71800.00),
    (124, 'Mini Gifts Distributors Ltd.', 'Nelson', 'Susan', '4155551450', '5677 Strong St.', 'San Rafael', 'USA', 210500.00);
```

### Load it in your test setup

```java
@BeforeAll
static void setUpFactory() {
    emf = Persistence.createEntityManagerFactory("classicmodels-test-pu");

    // Load test data from SQL script
    try (EntityManager em = emf.createEntityManager()) {
        em.getTransaction().begin();
        em.createNativeQuery(
            new String(
                CustomerEntityTest.class.getResourceAsStream("/test-data.sql").readAllBytes()
            )
        ).executeUpdate();
        em.getTransaction().commit();
    } catch (Exception e) {
        throw new RuntimeException("Failed to load test data", e);
    }
}
```

---

## Key Takeaways

1. **H2** is a Java in-memory database — zero installation, just a Maven dependency
2. Use H2 for **testing**, MySQL for **production**
3. `<scope>test</scope>` ensures H2 is only available during tests
4. `DB_CLOSE_DELAY=-1` keeps the database alive between connections
5. `MODE=MySQL` makes H2 behave like MySQL
6. `hbm2ddl.auto = create-drop` — Hibernate builds the schema from your entities automatically
7. Separate `persistence.xml` in `src/test/resources/` overrides the production config during tests
8. Tests run fast, isolated, and don't require a running MySQL server

---

## Version Reference

| Component | Version | Coordinates |
|---|---|---|
| H2 Database | 2.3.232 | `com.h2database:h2` |
| Hibernate ORM | 6.6.5.Final | `org.hibernate.orm:hibernate-core` |
| JUnit 5 | 5.11.4 | `org.junit.jupiter:junit-jupiter` |
| Lombok | 1.18.36 | `org.projectlombok:lombok` |
| MySQL Connector/J | 9.2.0 | `com.mysql:mysql-connector-j` |
| Java | 21 LTS | |
