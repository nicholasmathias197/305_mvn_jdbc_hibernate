# Lesson 5: Project Lombok

---

## Overview of Project Lombok

**Project Lombok** is a Java library that eliminates boilerplate code using annotations. It automatically generates getters, setters, constructors, `toString()`, `equals()`, `hashCode()`, and more — at compile time.

### The Problem

Look at our `Customer` entity from the previous lessons. The class has 13 fields, which means:
- 13 getter methods
- 13 setter methods
- A `toString()` method
- Constructors
- Potentially `equals()` and `hashCode()`

That's easily **100+ lines** of repetitive code that adds no value. You didn't write it — your IDE generated it. And if you add a field, you have to regenerate everything.

### The Solution

With Lombok, the same class goes from ~150 lines to ~30 lines. Lombok generates all the boilerplate at compile time, so the bytecode (.class file) contains everything — you just don't have to look at it.

---

## How Project Lombok Works

```
┌──────────────────────────────────────────────┐
│  Your Source Code (.java)                    │
│  @Getter @Setter                             │
│  private String customerName;                │
└──────────┬───────────────────────────────────┘
           │ javac compiles
           ▼
┌──────────────────────────────────────────────┐
│  Lombok Annotation Processor                 │
│  (runs during compilation)                   │
│                                              │
│  Generates:                                  │
│  - getCustomerName()                         │
│  - setCustomerName(String customerName)      │
└──────────┬───────────────────────────────────┘
           │
           ▼
┌──────────────────────────────────────────────┐
│  Compiled Bytecode (.class)                  │
│  Contains the generated methods              │
└──────────────────────────────────────────────┘
```

Lombok uses Java's **annotation processing** API. It hooks into the compiler and modifies the Abstract Syntax Tree (AST) to add methods before the `.class` file is produced.

> **Your source code stays clean. The compiled code has everything.**

---

## Project Lombok Maven Dependencies

Add Lombok to your `pom.xml`:

```xml
<dependencies>
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.36</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### Why `<scope>provided</scope>`?
Lombok is only needed at **compile time** to generate code. At runtime, the generated methods exist in the bytecode — Lombok itself is not needed. The `provided` scope means Maven includes it during compilation but doesn't package it into your final JAR.

### IDE Setup

Your IDE needs the Lombok plugin to understand the generated code:

**IntelliJ IDEA:**
1. Go to **File → Settings → Plugins**
2. Search for **Lombok**
3. Install the plugin and restart
4. Go to **File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors**
5. Check **Enable annotation processing**

**VS Code:**
1. Install the **Extension Pack for Java** (includes Lombok support)
2. Annotation processing is enabled by default

---

## Important Annotations from Lombok

| Annotation | Generates |
|---|---|
| `@Getter` | Getter methods for all fields (or one field) |
| `@Setter` | Setter methods for all fields (or one field) |
| `@ToString` | `toString()` method |
| `@EqualsAndHashCode` | `equals()` and `hashCode()` methods |
| `@NoArgsConstructor` | No-argument constructor |
| `@AllArgsConstructor` | Constructor with all fields as parameters |
| `@RequiredArgsConstructor` | Constructor with `final` and `@NonNull` fields only |
| `@Data` | Combines `@Getter`, `@Setter`, `@ToString`, `@EqualsAndHashCode`, `@RequiredArgsConstructor` |
| `@Builder` | Builder pattern for object creation |

---

## Lombok — @Getter and @Setter Annotation

### On Individual Fields

```java
import lombok.Getter;
import lombok.Setter;

public class Customer {
    @Getter @Setter
    private int customerNumber;

    @Getter @Setter
    private String customerName;

    @Getter  // Read-only — no setter
    private String createdDate;
}
```

Lombok generates:
```java
public int getCustomerNumber() { return this.customerNumber; }
public void setCustomerNumber(int customerNumber) { this.customerNumber = customerNumber; }
public String getCustomerName() { return this.customerName; }
public void setCustomerName(String customerName) { this.customerName = customerName; }
public String getCreatedDate() { return this.createdDate; }
```

### On the Class (applies to ALL fields)

```java
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Customer {
    private int customerNumber;
    private String customerName;
    private String contactLastName;
    private String contactFirstName;
    // ... all fields get getters and setters automatically
}
```

### Controlling Access Level

```java
@Setter(AccessLevel.PROTECTED)  // setter is protected, not public
private String internalCode;

@Setter(AccessLevel.NONE)  // no setter generated (read-only even with class-level @Setter)
private String id;
```

---

## Lombok — @ToString Annotation

```java
import lombok.ToString;

@ToString
public class Customer {
    private int customerNumber;
    private String customerName;
    private String city;
    private String country;
}
```

Generates:
```java
@Override
public String toString() {
    return "Customer(customerNumber=103, customerName=Atelier graphique, city=Nantes, country=France)";
}
```

### Customizing @ToString

```java
// Exclude sensitive fields
@ToString(exclude = {"phone", "addressLine1", "addressLine2"})
public class Customer { ... }

// Include only specific fields
@ToString(of = {"customerNumber", "customerName", "country"})
public class Customer { ... }
```

---

## Lombok — Constructor Annotations

### @NoArgsConstructor
```java
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Customer {
    private int customerNumber;
    private String customerName;
}
```
Generates: `public Customer() {}`

> **JPA/Hibernate requires a no-arg constructor.** This annotation ensures you always have one.

### @AllArgsConstructor
```java
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Customer {
    private int customerNumber;
    private String customerName;
    private String city;
}
```
Generates: `public Customer(int customerNumber, String customerName, String city) { ... }`

### @RequiredArgsConstructor
Only includes `final` fields and fields annotated with `@NonNull`:
```java
import lombok.RequiredArgsConstructor;
import lombok.NonNull;

@RequiredArgsConstructor
public class Customer {
    private final int customerNumber;     // included (final)
    @NonNull private String customerName; // included (@NonNull)
    private String city;                  // NOT included
}
```
Generates: `public Customer(int customerNumber, String customerName) { ... }`

---

## Lombok — @Data for Additional Methods

`@Data` is the all-in-one annotation. It combines:
- `@Getter` (all fields)
- `@Setter` (all non-final fields)
- `@ToString`
- `@EqualsAndHashCode`
- `@RequiredArgsConstructor`

### Before Lombok (~150 lines)

```java
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

    public Customer() {}

    // 13 getters... (39 lines)
    // 13 setters... (39 lines)
    // toString()... (15 lines)
    // equals()... (20 lines)
    // hashCode()... (15 lines)
}
```

### After Lombok (~20 lines)

```java
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
```

**That's it.** Lombok generates all getters, setters, `toString()`, `equals()`, `hashCode()`, no-arg constructor, and all-args constructor at compile time.

---

## @Data Warning with JPA Entities

When using `@Data` on JPA entities with relationships (`@OneToMany`, `@ManyToOne`), be careful:

- `@EqualsAndHashCode` includes ALL fields by default, including relationship collections
- This can cause **infinite loops** (Customer → Orders → Customer → ...)
- This can trigger **lazy loading** unexpectedly

### Solution: Exclude relationships

```java
@Data
@EqualsAndHashCode(exclude = {"orders"})  // Exclude the relationship collection
@ToString(exclude = {"orders"})           // Also exclude from toString
@Entity
@Table(name = "customers")
public class Customer {
    // ...

    @OneToMany(mappedBy = "customer")
    private List<Order> orders;
}
```

Or better yet, use `@Getter` and `@Setter` separately instead of `@Data` for entities with relationships:

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"customerNumber", "customerName"})
@Entity
@Table(name = "customers")
public class Customer { ... }
```

---

## Complete Entity Example: Employee with Lombok

```java
package com.perscholas.model;

import lombok.*;
import jakarta.persistence.*;

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

    @Column(name = "officeCode", nullable = false, length = 10)
    private String officeCode;

    @Column(name = "reportsTo")
    private Integer reportsTo;

    @Column(name = "jobTitle", nullable = false, length = 50)
    private String jobTitle;

    @Column(name = "VacationHours")
    private Integer vacationHours;
}
```

---

## Key Takeaways

1. **Lombok eliminates boilerplate** — getters, setters, constructors, toString, equals, hashCode
2. It works at **compile time** — no runtime overhead
3. Add it as a `provided` dependency in Maven
4. **Install the IDE plugin** or your IDE won't understand the generated code
5. `@Data` = `@Getter` + `@Setter` + `@ToString` + `@EqualsAndHashCode` + `@RequiredArgsConstructor`
6. For JPA entities with relationships, prefer `@Getter`/`@Setter` over `@Data` to avoid infinite loops
7. Always include `@NoArgsConstructor` on JPA entities (Hibernate requires it)

---

## Version Reference

| Component | Version |
|---|---|
| Lombok | 1.18.36 |
| Maven scope | `provided` |
| IntelliJ Plugin | Install from Marketplace → search "Lombok" |
