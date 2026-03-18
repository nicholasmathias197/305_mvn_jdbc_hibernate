# ClassicModels — Hibernate / JPA Example

A runnable Maven project demonstrating **Hibernate ORM 6** with the `classicmodels` MySQL database.

## Tech Stack

| Tool | Version |
|------|---------|
| Java | 21 LTS |
| Maven | 3.9.9 |
| Hibernate ORM | 6.6.5.Final |
| Jakarta Persistence | 3.2 (bundled) |
| MySQL Connector/J | 9.2.0 |
| Project Lombok | 1.18.36 |
| JUnit 5 | 5.11.4 |
| H2 Database | 2.3.232 |

## Prerequisites

1. **Java 21** and **Maven 3.9+** installed.
2. MySQL running on `localhost:3306` with the `classicmodels` schema loaded.
3. Update `persistence.xml` if your MySQL credentials differ from `root` / `password`.

## Project Structure

```
hibernate-example/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/perscholas/
│   │   │   ├── App.java                    # Main demo
│   │   │   ├── util/JPAUtil.java           # EntityManagerFactory singleton
│   │   │   └── entity/                     # JPA entity classes
│   │   │       ├── Customer.java
│   │   │       ├── Employee.java
│   │   │       ├── Office.java
│   │   │       ├── Product.java
│   │   │       └── ProductLine.java
│   │   └── resources/META-INF/
│   │       └── persistence.xml             # MySQL config
│   └── test/
│       ├── java/com/perscholas/
│       │   └── entity/CustomerEntityTest.java
│       └── resources/META-INF/
│           └── persistence.xml             # H2 in-memory config
```

## Running

```bash
mvn compile exec:java -Dexec.mainClass="com.perscholas.App"
```

## Key Concepts

- **Entity mapping** with `@Entity`, `@Table`, `@Id`, `@Column`
- **Relationships** — `@ManyToOne` / `@OneToMany` between entities
- **Lombok** — `@Getter`, `@Setter`, `@NoArgsConstructor`, `@ToString`
- **EntityManager** CRUD — `persist`, `find`, `merge`, `remove`
- **HQL / JPQL** queries
- **H2 testing** — in-memory database with `MODE=MySQL` for unit tests
