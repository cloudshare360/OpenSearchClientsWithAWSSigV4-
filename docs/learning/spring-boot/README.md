# Spring Boot Learning Guide

**Project:** OpenSearch Employee Search Platform  
**Purpose:** Learn Spring Boot for this project  
**Version:** 1.0  
**Date:** 2026-08-16

---

## What is Spring Boot?

Spring Boot is a Java-based framework for building production-ready applications quickly. It simplifies configuration and setup, allowing developers to focus on business logic.

### Key Concepts for This Project

1. **Spring Boot Starter:** Pre-configured dependencies
2. **Auto-configuration:** Automatic setup based on classpath
3. **Spring Boot Actuator:** Monitoring and management endpoints
4. **Spring Data JPA:** Database access abstraction
5. **Spring Batch:** Batch processing framework
6. **Spring Security:** Authentication and authorization

---

## Getting Started

### 1. Project Setup

```bash
# Create new Spring Boot project
curl https://start.spring.io/starter.zip \
  -d dependencies=web,data-jpa,batch,actuator \
  -d javaVersion=21 \
  -d type=maven-project \
  -o demo.zip

unzip demo.zip
cd demo
```

### 2. Application Properties

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

### 3. First REST Controller

```java
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    
    @GetMapping
    public List<Employee> getAll() {
        return List.of();
    }
    
    @PostMapping
    public Employee create(@RequestBody Employee employee) {
        return employee;
    }
}
```

---

## Key Topics for This Project

### Spring Data JPA

**What it is:** Abstraction layer for database access

**Key Concepts:**
- `@Entity` - Marks a class as a database entity
- `@Repository` - Marks a class as a data access layer
- `JpaRepository` - Interface for CRUD operations
- `@Query` - Custom query definitions

**Example:**
```java
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByDepartment(String department);
    
    @Query("SELECT e FROM Employee e WHERE e.firstName LIKE %:query%")
    List<Employee> search(@Param("query") String query);
}
```

### Spring Batch

**What it is:** Framework for batch processing

**Key Concepts:**
- `Job` - A batch process
- `Step` - A phase of a job
- `ItemReader` - Reads data
- `ItemProcessor` - Processes data
- `ItemWriter` - Writes data
- `Chunk` - Processes data in chunks

**Example:**
```java
@Bean
public Job employeeSyncJob(JobBuilderFactory jobs, Step step) {
    return jobs.get("employeeSyncJob")
        .incrementer(new RunIdIncrementer())
        .flow(step)
        .end()
        .build();
}

@Bean
public Step step(StepBuilderFactory steps, ItemReader reader, 
                 ItemProcessor processor, ItemWriter writer) {
    return steps.get("employeeSyncStep")
        .<Employee, Employee>chunk(100)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build();
}
```

### Spring Boot Actuator

**What it is:** Production-ready features for monitoring

**Key Endpoints:**
- `/actuator/health` - Application health
- `/actuator/info` - Application info
- `/actuator/metrics` - Application metrics
- `/actuator/env` - Environment properties

**Configuration:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

### Spring Retry

**What it is:** Retry mechanism for failed operations

**Usage:**
```java
@Service
public class EmployeeService {
    
    @Retryable(value = {OpenSearchException.class}, maxAttempts = 3)
    public List<Employee> search(String query) {
        // Retry on OpenSearchException up to 3 times
    }
}
```

---

## Project Structure

```
spring-boot-rest/
├── src/main/java/com/example/opensearch/
│   ├── config/
│   │   ├── AwsConfig.java
│   │   ├── OpenSearchConfig.java
│   │   └── WebConfig.java
│   ├── controller/
│   │   └── EmployeeController.java
│   ├── model/
│   │   └── Employee.java
│   ├── repository/
│   │   └── EmployeeJpaRepository.java
│   ├── service/
│   │   ├── EmployeeService.java
│   │   └── OpenSearchService.java
│   └── OpenSearchRestApplication.java
├── src/main/resources/
│   ├── application.yml
│   └── templates/
└── pom.xml
```

---

## Common Patterns in This Project

### 1. Service Layer Pattern

```java
@Service
public class EmployeeService {
    private final EmployeeRepository repository;
    
    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }
    
    @Transactional
    public Employee create(Employee employee) {
        return repository.save(employee);
    }
}
```

### 2. DTO Pattern

```java
public record EmployeeRequest(String firstName, String lastName, String email) {}
public record EmployeeResponse(Long id, String firstName, String lastName) {}
```

### 3. Exception Handling

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }
}
```

---

## Testing Spring Boot

### Unit Testing

```java
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    
    @Mock
    private EmployeeRepository repository;
    
    @InjectMocks
    private EmployeeService service;
    
    @Test
    void shouldCreateEmployee() {
        // Given
        Employee employee = new Employee("John", "Doe");
        when(repository.save(any())).thenReturn(employee);
        
        // When
        Employee result = service.create(employee);
        
        // Then
        assertEquals("John", result.getFirstName());
    }
}
```

### Integration Testing

```java
@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldReturnEmployee() throws Exception {
        mockMvc.perform(get("/api/employees/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("John"));
    }
}
```

---

## Best Practices

1. **Use Constructor Injection** - More testable and immutable
2. **Keep Controllers Thin** - Business logic in services
3. **Use DTOs** - Don't expose entities directly
4. **Handle Exceptions** - Use @RestControllerAdvice
5. **Validate Input** - Use @Valid and constraints
6. **Use Profiles** - Separate dev, test, prod configs
7. **Log Appropriately** - Use SLF4J with proper levels

---

## Next Steps

1. Complete [Spring Boot Getting Started](getting-started.md)
2. Learn [REST API Development](rest-api.md)
3. Study [Data JPA](data-jpa.md)
4. Practice with sample projects

---

*Part of the OpenSearchClientsWithAWSSigV4 Learning Documentation*
