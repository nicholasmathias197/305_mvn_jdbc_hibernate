# Lesson 1: Apache Maven

---

## Introduction to Maven

**Apache Maven** is a build automation and project management tool for Java projects. It handles:

- **Dependency management** — automatically downloads libraries (JARs) your project needs
- **Build lifecycle** — compiles, tests, packages, and deploys your code with simple commands
- **Project structure** — enforces a standard directory layout so every Java project looks the same
- **Reproducibility** — anyone can clone your project and build it identically

### Why Maven Matters

Before Maven, developers manually downloaded JAR files, placed them in a `/lib` folder, and configured classpaths by hand. If Library A needed Library B version 2.3, you had to figure that out yourself. Maven solves all of this automatically.

---

## Advantages of Using Maven

| Benefit | Explanation |
|---|---|
| **Convention over configuration** | Standard project layout means less setup |
| **Transitive dependencies** | If you need Library A, and A needs Library B, Maven fetches both |
| **Central repository** | Over 30 million artifacts available at [Maven Central](https://central.sonatype.com/) |
| **IDE integration** | IntelliJ, Eclipse, and VS Code all have first-class Maven support |
| **Consistent builds** | Same `mvn` commands work on every machine and CI/CD pipeline |

---

## Core Concepts of Maven

### 1. POM (Project Object Model)
Every Maven project has a `pom.xml` file at its root. This is the single source of truth for your project's configuration.

### 2. Coordinates (GAV)
Every artifact in Maven is identified by three values:
- **G**roupId — organization/company (e.g., `com.mysql`)
- **A**rtifactId — project name (e.g., `mysql-connector-j`)
- **V**ersion — release version (e.g., `9.2.0`)

### 3. Repositories
- **Local repository**: `~/.m2/repository` on your machine (cached downloads)
- **Central repository**: Maven Central — the default public repo
- **Remote repositories**: company-hosted repos (Nexus, Artifactory)

### 4. Dependencies
Libraries your project needs, declared in `pom.xml`. Maven downloads them automatically.

### 5. Plugins
Extensions that add build capabilities (compile, test, package, etc.).

---

## Installation of Maven

### Prerequisites
- **Java 21** (LTS) installed and `JAVA_HOME` set

### Step-by-Step (Windows)

1. **Download Maven** from https://maven.apache.org/download.cgi
   - Get the **Binary zip archive** (e.g., `apache-maven-3.9.9-bin.zip`)

2. **Extract** to a permanent location:
   ```
   C:\Program Files\Apache\maven
   ```

3. **Set environment variables:**
   - Add `MAVEN_HOME` = `C:\Program Files\Apache\maven`
   - Add `%MAVEN_HOME%\bin` to your `Path` variable

4. **Verify installation:**
   ```bash
   mvn -version
   ```
   You should see output like:
   ```
   Apache Maven 3.9.9
   Maven home: C:\Program Files\Apache\maven
   Java version: 21.0.x, vendor: Eclipse Adoptium
   ```

> **Tip:** If using IntelliJ IDEA, Maven is bundled — you can use the built-in version without separate installation.

---

## Overview of the Project Object Model (POM)

The `pom.xml` is an XML file that describes your project. Here's what each section does:

### Sample `pom.xml` for This Course

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <!-- YOUR PROJECT COORDINATES -->
    <groupId>com.perscholas</groupId>
    <artifactId>classicmodels-jdbc</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>ClassicModels JDBC Application</name>

    <!-- JAVA VERSION -->
    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <!-- DEPENDENCIES -->
    <dependencies>
        <!-- MySQL JDBC Driver -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>9.2.0</version>
        </dependency>
    </dependencies>

</project>
```

### Breaking Down the POM

| Element | Purpose |
|---|---|
| `<modelVersion>` | Always `4.0.0` — the POM schema version |
| `<groupId>` | Your organization's reverse domain (like a Java package) |
| `<artifactId>` | The name of this specific project |
| `<version>` | Your project version. `SNAPSHOT` = in development |
| `<packaging>` | Output format: `jar`, `war`, `pom` |
| `<properties>` | Variables reused throughout the POM |
| `<dependencies>` | List of libraries your project needs |

---

## Maven Build Lifecycle

Maven has a defined sequence of **phases**. When you run a phase, all preceding phases run first.

```
validate → compile → test → package → verify → install → deploy
```

| Phase | What It Does |
|---|---|
| `validate` | Checks that the project is correct and all info is available |
| `compile` | Compiles your `.java` source files into `.class` files |
| `test` | Runs unit tests using a testing framework (JUnit) |
| `package` | Bundles compiled code into a JAR/WAR file |
| `verify` | Runs integration tests and checks |
| `install` | Copies the JAR to your **local** Maven repository (`~/.m2/`) |
| `deploy` | Uploads the JAR to a **remote** repository |

### Common Commands

```bash
# Compile your code
mvn compile

# Run tests
mvn test

# Create a JAR file (in target/ folder)
mvn package

# Clean build artifacts (deletes target/ folder)
mvn clean

# Clean then package (most common combo)
mvn clean package

# Skip tests during build
mvn clean package -DskipTests
```

---

## Overview of Dependencies and Repositories

### Declaring a Dependency

To use any library, add it inside `<dependencies>` in your `pom.xml`:

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>9.2.0</version>
</dependency>
```

### Dependency Scope

| Scope | When Available | Example |
|---|---|---|
| `compile` (default) | Everywhere — compile, test, runtime | MySQL driver |
| `test` | Only during testing | JUnit |
| `provided` | Compile-time only (server provides it at runtime) | Servlet API |
| `runtime` | Test and runtime, not compile | JDBC drivers (in some setups) |

Example with scope:
```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.11.4</version>
    <scope>test</scope>
</dependency>
```

### How to Search for Dependencies

1. Go to **https://central.sonatype.com/** (Maven Central Search)
2. Search for the library name (e.g., "mysql connector")
3. Click the artifact to see available versions
4. Copy the `<dependency>` XML snippet into your `pom.xml`

> **Important:** Always use the **latest stable** version. Avoid versions marked `RC`, `beta`, or `alpha` for production code.

---

## Maven Plugins

Plugins perform the actual work in each build phase. Some are built-in; others you add explicitly.

### Common Plugins

| Plugin | Purpose |
|---|---|
| `maven-compiler-plugin` | Compiles Java source code |
| `maven-surefire-plugin` | Runs unit tests |
| `maven-jar-plugin` | Creates JAR files |
| `maven-shade-plugin` | Creates an "uber JAR" with all dependencies included |

### Example: Configuring the Compiler Plugin

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.13.0</version>
            <configuration>
                <source>21</source>
                <target>21</target>
            </configuration>
        </plugin>
    </plugins>
</build>
```

> In modern Maven (3.9+), setting `<maven.compiler.source>` and `<maven.compiler.target>` in `<properties>` is usually sufficient — you don't need to explicitly configure the compiler plugin.

---

## How Maven Works — Putting It All Together

```
1. You run:  mvn clean package

2. Maven reads pom.xml

3. Downloads dependencies from Maven Central → ~/.m2/repository/

4. Executes lifecycle phases in order:
   clean → validate → compile → test → package

5. Output: target/classicmodels-jdbc-1.0-SNAPSHOT.jar
```

### Standard Project Structure

Maven enforces this directory layout:

```
my-project/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/              ← Your application code
│   │   │   └── com/perscholas/
│   │   │       └── App.java
│   │   └── resources/         ← Config files, properties
│   │       └── application.properties
│   └── test/
│       ├── java/              ← Test code
│       │   └── com/perscholas/
│       │       └── AppTest.java
│       └── resources/         ← Test config files
└── target/                    ← Build output (generated)
```

---

## Hands-On: Create Your First Maven Project

### Using IntelliJ IDEA
1. **File → New → Project**
2. Select **Maven Archetype** on the left
3. Fill in:
   - Name: `classicmodels-jdbc`
   - GroupId: `com.perscholas`
   - ArtifactId: `classicmodels-jdbc`
   - Archetype: `maven-archetype-quickstart`
4. Click **Create**
5. Add the MySQL dependency to `pom.xml`
6. Click the Maven refresh icon (or right-click `pom.xml` → Maven → Reload Project)

### Using the Command Line
```bash
mvn archetype:generate \
  -DgroupId=com.perscholas \
  -DartifactId=classicmodels-jdbc \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DarchetypeVersion=1.5 \
  -DinteractiveMode=false
```

---

## Key Takeaways

1. Maven manages **dependencies** and **build lifecycle** for Java projects
2. The `pom.xml` is the heart of every Maven project
3. Dependencies are identified by **groupId:artifactId:version**
4. `mvn clean package` is the most common build command
5. Maven Central is where you find libraries to add to your project
6. The standard directory structure (`src/main/java`, `src/test/java`) is non-negotiable

---

## Version Reference (Current as of 2026)

| Tool/Library | Version | Notes |
|---|---|---|
| Java (JDK) | 21 LTS | Download from [Adoptium](https://adoptium.net/) |
| Apache Maven | 3.9.9 | Or use IDE bundled version |
| MySQL Connector/J | 9.2.0 | GroupId changed to `com.mysql` (was `mysql`) |
| JUnit 5 | 5.11.4 | Use `junit-jupiter` artifact |
