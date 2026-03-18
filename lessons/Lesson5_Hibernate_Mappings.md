# Lesson 6: Hibernate Mapping and Relationships

---

## Hibernate Mapping and Relationship Overview

In a relational database, tables are connected through **foreign keys**. In Java/Hibernate, the equivalent is **object references** and **collections**. Hibernate relationship annotations tell Hibernate how to translate between these two worlds.

### The classicmodels Relationships

```
productlines ──< products        (One ProductLine has Many Products)
customers ──< orders              (One Customer has Many Orders)
orders ──< orderdetails           (One Order has Many OrderDetails)
products ──< orderdetails         (One Product appears in Many OrderDetails)
offices ──< employees             (One Office has Many Employees)
employees ──< employees           (One Employee manages Many Employees — self-referencing)
customers >── employees           (Many Customers have One Sales Rep Employee)
customers ──< payments            (One Customer has Many Payments)
```

The symbols: `──<` means "one-to-many" (read left to right: one productline has many products).

---

## Annotations — Hibernate Mapping and Relationship

| Annotation | Purpose |
|---|---|
| `@ManyToOne` | This entity holds a foreign key pointing to one other entity |
| `@OneToMany` | This entity's table is referenced by many rows in another table |
| `@OneToOne` | One-to-one relationship between two entities |
| `@ManyToMany` | Many-to-many via a join table |
| `@JoinColumn` | Specifies the foreign key column name |
| `@MappedBy` | Indicates the inverse (non-owning) side of a bidirectional relationship |

---

## Hibernate Mapping and Relationship Example

Let's map the real classicmodels relationships step by step.

### Understanding "Owning Side"

In every bidirectional relationship, one side **owns** the relationship (has the foreign key column). The other side is the **inverse** and uses `mappedBy`.

**Rule of thumb:** The table with the foreign key column is the owning side.

| Relationship | Owning Side (has FK) | Inverse Side (mappedBy) |
|---|---|---|
| Customer ↔ Order | Order (has `customerNumber` FK) | Customer |
| ProductLine ↔ Product | Product (has `productLine` FK) | ProductLine |
| Office ↔ Employee | Employee (has `officeCode` FK) | Office |

---

## Unidirectional and Bidirectional Association/Mapping

### Unidirectional
Only one side knows about the relationship:

```java
// Order knows about Customer
@Entity
public class Order {
    @ManyToOne
    @JoinColumn(name = "customerNumber")
    private Customer customer;  // Order → Customer
}

// Customer does NOT know about Orders
@Entity
public class Customer {
    // No orders field — Customer can't navigate to its orders
}
```

### Bidirectional
Both sides know about each other:

```java
// Order knows about Customer (owning side)
@Entity
public class Order {
    @ManyToOne
    @JoinColumn(name = "customerNumber")
    private Customer customer;
}

// Customer also knows about its Orders (inverse side)
@Entity
public class Customer {
    @OneToMany(mappedBy = "customer")
    private List<Order> orders;
}
```

**When to use which:**
- **Unidirectional** is simpler — use it when you only need to navigate one direction
- **Bidirectional** when you need navigation both ways (e.g., from a Customer, get all their Orders)

---

## Many-to-One Relationship

**"Many of THIS entity belong to One of THAT entity."**

In classicmodels: Many **Orders** belong to one **Customer**. The `orders` table has a `customerNumber` foreign key column.

### Many-To-One Relationship Example

```java
package com.perscholas.model;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "customer")
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @Column(name = "orderNumber")
    private int orderNumber;

    @Column(name = "orderDate", nullable = false)
    private LocalDate orderDate;

    @Column(name = "requiredDate", nullable = false)
    private LocalDate requiredDate;

    @Column(name = "shippedDate")
    private LocalDate shippedDate;

    @Column(name = "status", nullable = false, length = 15)
    private String status;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    // MANY Orders belong to ONE Customer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerNumber", nullable = false)
    private Customer customer;
}
```

**What's happening:**
- `@ManyToOne` — tells Hibernate: "Many Order rows point to one Customer"
- `@JoinColumn(name = "customerNumber")` — the foreign key column in the `orders` table
- `fetch = FetchType.LAZY` — don't load the Customer until we actually access it (performance optimization)

### Using It

```java
try (EntityManager em = emf.createEntityManager()) {
    Order order = em.find(Order.class, 10100);

    // Navigate from Order → Customer
    System.out.println("Order #" + order.getOrderNumber());
    System.out.println("Customer: " + order.getCustomer().getCustomerName());
    System.out.println("Status: " + order.getStatus());
}
```

---

## One-to-Many Relationship

**"One of THIS entity has Many of THAT entity."**

This is the **inverse side** of Many-to-One. One **Customer** has many **Orders**.

### One-to-Many Relationship Example

```java
package com.perscholas.model;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"customerNumber", "customerName", "country"})
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

    @Column(name = "creditLimit", precision = 10, scale = 2)
    private BigDecimal creditLimit;

    // ONE Customer has MANY Orders
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<Order> orders;

    // ONE Customer has MANY Payments
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<Payment> payments;

    // MANY Customers are assigned to ONE Employee (sales rep)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salesRepEmployeeNumber")
    private Employee salesRep;
}
```

**Key points:**
- `mappedBy = "customer"` — tells Hibernate: "The `customer` field in the `Order` class owns this relationship. I'm just the inverse."
- `FetchType.LAZY` — the list of orders is NOT loaded from the database until you call `customer.getOrders()`. This prevents loading thousands of rows when you just need a customer name.

### Using It

```java
try (EntityManager em = emf.createEntityManager()) {
    Customer customer = em.find(Customer.class, 141);  // Euro+ Shopping Channel

    System.out.println("Customer: " + customer.getCustomerName());
    System.out.println("Number of orders: " + customer.getOrders().size());

    // Print each order
    for (Order order : customer.getOrders()) {
        System.out.printf("  Order #%d — %s — %s%n",
            order.getOrderNumber(), order.getOrderDate(), order.getStatus());
    }
}
```

---

## One-to-One Relationship

**"One of THIS entity maps to exactly One of THAT entity."**

The classicmodels database doesn't have a natural one-to-one relationship, but this pattern is common in real applications (e.g., User ↔ UserProfile, Employee ↔ ParkingSpot).

### One-to-One Relationship Example

Let's create a conceptual example. Imagine each office had exactly one manager stored in a separate table:

```java
@Entity
@Table(name = "office_details")
public class OfficeDetail {

    @Id
    @Column(name = "officeCode")
    private String officeCode;

    @Column(name = "managerName")
    private String managerName;

    @Column(name = "floorCount")
    private int floorCount;

    // ONE OfficeDetail belongs to ONE Office
    @OneToOne
    @JoinColumn(name = "officeCode", referencedColumnName = "officeCode")
    private Office office;
}
```

```java
@Entity
@Table(name = "offices")
public class Office {

    @Id
    @Column(name = "officeCode")
    private String officeCode;

    // ... other fields ...

    // ONE Office has ONE OfficeDetail (inverse side)
    @OneToOne(mappedBy = "office")
    private OfficeDetail officeDetail;
}
```

### A Real classicmodels Example: Employee Self-Reference

The `employees` table has a `reportsTo` column that references another employee. This is essentially a **many-to-one** (many employees report to one manager), but let's map it:

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"employeeNumber", "firstName", "lastName", "jobTitle"})
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @Column(name = "employeeNumber")
    private int employeeNumber;

    @Column(name = "lastName", nullable = false, length = 50)
    private String lastName;

    @Column(name = "firstName", nullable = false, length = 50)
    private String firstName;

    @Column(name = "extension", nullable = false, length = 10)
    private String extension;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "jobTitle", nullable = false, length = 50)
    private String jobTitle;

    @Column(name = "VacationHours")
    private Integer vacationHours;

    // MANY Employees report to ONE manager (self-referencing)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportsTo")
    private Employee manager;

    // ONE manager has MANY direct reports
    @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
    private List<Employee> directReports;

    // MANY Employees work in ONE Office
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officeCode", nullable = false)
    private Office office;

    // ONE Employee is the sales rep for MANY Customers
    @OneToMany(mappedBy = "salesRep", fetch = FetchType.LAZY)
    private List<Customer> customers;
}
```

### Querying the Employee Hierarchy

```java
try (EntityManager em = emf.createEntityManager()) {
    Employee employee = em.find(Employee.class, 1165);  // Leslie Jennings

    System.out.println("Employee: " + employee.getFirstName() + " " + employee.getLastName());
    System.out.println("Reports to: " + employee.getManager().getFirstName()
                       + " " + employee.getManager().getLastName());
    System.out.println("Office: " + employee.getOffice().getCity());
    System.out.println("Customers assigned: " + employee.getCustomers().size());
}
```

---

## Many-to-Many Relationship

**"Many of THIS entity relate to Many of THAT entity — through a join table."**

In classicmodels, the `orderdetails` table acts as a join table between `orders` and `products`. However, `orderdetails` has its own data columns (`quantityOrdered`, `priceEach`), so it's better modeled as two @ManyToOne relationships rather than a pure @ManyToMany.

### Pure @ManyToMany Example

If `orderdetails` were just a link table with no extra columns:

```java
@Entity
@Table(name = "orders")
public class Order {

    @Id
    private int orderNumber;

    @ManyToMany
    @JoinTable(
        name = "orderdetails",                                    // join table name
        joinColumns = @JoinColumn(name = "orderNumber"),           // FK to this entity
        inverseJoinColumns = @JoinColumn(name = "productCode")     // FK to the other entity
    )
    private List<Product> products;
}

@Entity
@Table(name = "products")
public class Product {

    @Id
    private String productCode;

    @ManyToMany(mappedBy = "products")
    private List<Order> orders;
}
```

### Real-World Pattern: Join Entity with Extra Columns

Since `orderdetails` has `quantityOrdered`, `priceEach`, and `orderLineNumber`, we model it as a separate entity with a **composite key**:

```java
package com.perscholas.model;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.io.Serializable;

// Composite key class
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class OrderDetailId implements Serializable {
    @Column(name = "orderNumber")
    private int orderNumber;

    @Column(name = "productCode")
    private String productCode;
}

// The join entity with extra data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"order", "product"})
@Entity
@Table(name = "orderdetails")
public class OrderDetail {

    @EmbeddedId
    private OrderDetailId id;

    @Column(name = "quantityOrdered", nullable = false)
    private int quantityOrdered;

    @Column(name = "priceEach", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceEach;

    @Column(name = "orderLineNumber", nullable = false)
    private short orderLineNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("orderNumber")
    @JoinColumn(name = "orderNumber")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productCode")
    @JoinColumn(name = "productCode")
    private Product product;
}
```

Then on Order and Product:

```java
// In Order.java
@OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
private List<OrderDetail> orderDetails;

// In Product.java
@OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
private List<OrderDetail> orderDetails;
```

### Querying Order Details

```java
try (EntityManager em = emf.createEntityManager()) {
    Order order = em.find(Order.class, 10100);

    System.out.println("Order #" + order.getOrderNumber() + " — " + order.getStatus());
    System.out.println("Customer: " + order.getCustomer().getCustomerName());
    System.out.println("Items:");

    for (OrderDetail detail : order.getOrderDetails()) {
        System.out.printf("  %s — Qty: %d — Price: $%.2f — Line Total: $%.2f%n",
            detail.getProduct().getProductName(),
            detail.getQuantityOrdered(),
            detail.getPriceEach(),
            detail.getPriceEach().multiply(BigDecimal.valueOf(detail.getQuantityOrdered()))
        );
    }
}
```

---

## Complete Entity Map for classicmodels

Here's how all entities connect:

```java
// Office.java
@Entity @Table(name = "offices")
public class Office {
    @Id private String officeCode;
    private String city, phone, addressLine1, addressLine2, state, country, postalCode, territory;

    @OneToMany(mappedBy = "office", fetch = FetchType.LAZY)
    private List<Employee> employees;
}

// Employee.java
@Entity @Table(name = "employees")
public class Employee {
    @Id private int employeeNumber;
    private String lastName, firstName, extension, email, jobTitle;
    private Integer vacationHours;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "officeCode")
    private Office office;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "reportsTo")
    private Employee manager;

    @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
    private List<Employee> directReports;

    @OneToMany(mappedBy = "salesRep", fetch = FetchType.LAZY)
    private List<Customer> customers;
}

// Customer.java
@Entity @Table(name = "customers")
public class Customer {
    @Id private int customerNumber;
    // ... string fields ...

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "salesRepEmployeeNumber")
    private Employee salesRep;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<Order> orders;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<Payment> payments;
}

// Order.java
@Entity @Table(name = "orders")
public class Order {
    @Id private int orderNumber;
    private LocalDate orderDate, requiredDate, shippedDate;
    private String status, comments;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "customerNumber")
    private Customer customer;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<OrderDetail> orderDetails;
}

// ProductLine.java
@Entity @Table(name = "productlines")
public class ProductLine {
    @Id private String productLine;
    private String textDescription;

    @OneToMany(mappedBy = "productLine", fetch = FetchType.LAZY)
    private List<Product> products;
}

// Product.java
@Entity @Table(name = "products")
public class Product {
    @Id private String productCode;
    private String productName, productScale, productVendor, productDescription;
    private short quantityInStock;
    private BigDecimal buyPrice, msrp;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "productLine")
    private ProductLine productLine;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<OrderDetail> orderDetails;
}

// Payment.java
@Entity @Table(name = "payments")
public class Payment {
    // Composite key: customerNumber + checkNumber
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "customerNumber")
    private Customer customer;

    private String checkNumber;
    private LocalDate paymentDate;
    private BigDecimal amount;
}
```

---

## Fetch Types: LAZY vs EAGER

| FetchType | Behavior | Use When |
|---|---|---|
| `LAZY` (recommended) | Related data loaded only when accessed | Default for collections (`@OneToMany`, `@ManyToMany`) |
| `EAGER` | Related data loaded immediately with the parent | Rarely — only for small, always-needed data |

### The N+1 Problem

```java
// This generates 1 query to get all customers + N queries (one per customer) to get orders
List<Customer> customers = em.createQuery("FROM Customer", Customer.class).getResultList();
for (Customer c : customers) {
    System.out.println(c.getOrders().size());  // Each .getOrders() fires a separate SQL query!
}
```

### Solution: JOIN FETCH

```java
// This generates ONE query with a JOIN
List<Customer> customers = em.createQuery(
    "SELECT c FROM Customer c JOIN FETCH c.orders WHERE c.country = :country",
    Customer.class
).setParameter("country", "USA").getResultList();
```

---

## Key Takeaways

1. **`@ManyToOne`** = this entity has a foreign key column → it's the **owning side**
2. **`@OneToMany(mappedBy)`** = the inverse side — just a navigation convenience
3. **`mappedBy`** value = the field name on the owning side (not the column name!)
4. Always use **`FetchType.LAZY`** for collections to avoid loading unnecessary data
5. Use **`JOIN FETCH`** in HQL when you know you need related data
6. For join tables with extra columns, create a **separate entity** with `@EmbeddedId`
7. Exclude relationship fields from `@ToString` and `@EqualsAndHashCode` to avoid infinite loops
8. The table with the **foreign key** is always the owning side
