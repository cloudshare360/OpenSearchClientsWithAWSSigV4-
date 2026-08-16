# Spring Boot Getting Started

**Project:** OpenSearch Employee Search Platform  
**Purpose:** Quick start guide for Spring Boot  
**Version:** 1.0  
**Date:** 2026-08-16

---

## Prerequisites

- Java 21 JDK installed
- Maven 3.9+ installed
- IDE (IntelliJ IDEA, Eclipse, or VS Code)
- PostgreSQL 16 running

---

## Step 1: Create Project

### Using Spring Initializr
```bash
curl https://start.spring.io/starter.zip \
  -d dependencies=web,data-jpa,batch,actuator,security \
  -d javaVersion=21 \
  -d type=maven-project \
  -d name=spring-boot-rest \
  -o spring-boot-rest.zip

unzip spring-boot-rest.zip
cd spring-boot-rest
```

### Manual pom.xml Setup
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.4</version>
</parent>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-batch</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

---

## Step 2: Application Configuration

```yaml
# src/main/resources/application.yml
spring:
  application:
    name: spring-boot-rest
  datasource:
    url: jdbc:postgresql://localhost:5432/employeedb
    username: admin
    password: admin123
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

server:
  port: 8080
```

---

## Step 3: Create First REST Controller

```java
package com.example.demo;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hello")
public class HelloController {
    
    @GetMapping
    public String hello() {
        return "Hello, Spring Boot!";
    }
    
    @GetMapping("/{name}")
    public String helloName(@PathVariable String name) {
        return "Hello, " + name + "!";
    }
}
```

---

## Step 4: Run Application

```bash
# Using Maven
./mvnw spring-boot:run

# Using Java
java -jar target/demo-0.0.1-SNAPSHOT.jar

# Test the endpoint
curl http://localhost:8080/api/hello
curl http://localhost:8080/api/hello/John
```

---

## Step 5: Add Database Entity

```java
@Entity
@Table(name = "employees")
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    // Constructors, getters, setters
}
```

---

## Step 6: Create Repository

```java
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByDepartment(String department);
    
    @Query("SELECT e FROM Employee e WHERE e.firstName LIKE %:query% OR e.lastName LIKE %:query%")
    List<Employee> search(@Param("query") String query);
}
```

---

## Step 7: Create Service

```java
@Service
public class EmployeeService {
    
    private final EmployeeRepository repository;
    
    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }
    
    public List<Employee> getAll() {
        return repository.findAll();
    }
    
    public Employee create(Employee employee) {
        return repository.save(employee);
    }
}
```

---

## Step 8: Create REST Controller

```java
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    
    private final EmployeeService service;
    
    public EmployeeController(EmployeeService service) {
        this.service = service;
    }
    
    @GetMapping
    public List<Employee> getAll() {
        return service.getAll();
    }
    
    @PostMapping
    public Employee create(@RequestBody Employee employee) {
        return service.create(employee);
    }
}
```

---

## Step 9: Test the API

```bash
# Get all employees
curl http://localhost:8080/api/employees

# Create employee
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","email":"john@example.com","department":"Engineering"}'
```

---

## Project Structure

```
spring-boot-rest/
├── src/main/java/com/example/demo/
│   ├── DemoApplication.java
│   ├── controller/
│   │   └── EmployeeController.java
│   ├── model/
│   │   └── Employee.java
│   ├── repository/
│   │   └── EmployeeRepository.java
│   └── service/
│       └── EmployeeService.java
├── src/main/resources/
│   └── application.yml
└── pom.xml
```

---

## Next Steps

1. Learn [REST API Development](rest-api.md)
2. Study [Data JPA](data-jpa.md)
3. Understand [Batch Processing](batch.md)
4. Practice with [Testing](testing.md)

---

*Part of the OpenSearchClientsWithAWSSigV4 Learning Documentation*
