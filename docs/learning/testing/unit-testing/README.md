# Unit Testing Guide

**Project:** OpenSearch Employee Search Platform  
**Purpose:** Learn unit testing strategies  
**Version:** 1.0  
**Date:** 2026-08-16

---

## What is Unit Testing?

Unit testing is the practice of testing individual components or functions in isolation from the rest of the system.

### Key Concepts

1. **Unit:** Smallest testable part of code (method, function)
2. **Test Case:** Single test scenario
3. **Assertion:** Verification of expected outcome
4. **Mock:** Simulated dependency
5. **Stub:** Predefined response

---

## Tools

| Tool | Purpose |
|------|---------|
| **JUnit 5** | Test framework |
| **Mockito** | Mocking framework |
| **Spring Test** | Spring Boot test support |
| **JaCoCo** | Code coverage |

---

## Spring Boot REST Testing

### Service Test Example
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
        Employee employee = new Employee("John", "Doe", "john@example.com");
        when(repository.save(any())).thenReturn(employee);
        
        // When
        Employee result = service.create(employee);
        
        // Then
        assertEquals("John", result.getFirstName());
        verify(repository).save(any());
    }
}
```

### Controller Test Example
```java
@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private EmployeeService employeeService;
    
    @Test
    void shouldReturnAllEmployees() throws Exception {
        // Given
        List<Employee> employees = List.of(
            new Employee("John", "Doe", "john@example.com")
        );
        when(employeeService.getAll()).thenReturn(employees);
        
        // When/Then
        mockMvc.perform(get("/api/employees"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].firstName").value("John"));
    }
}
```

---

## Spring Boot Batch Testing

```java
@SpringBootTest
@SpringBatchTest
class EmployeeSyncJobTest {
    
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    
    @Test
    void shouldCompleteJob() throws Exception {
        // Given
        JobParameters params = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();
        
        // When
        JobExecution execution = jobLauncherTestUtils.launchJob(params);
        
        // Then
        assertEquals(ExitStatus.COMPLETED, execution.getExitStatus());
    }
}
```

---

## Coverage Goals

| Module | Target |
|--------|--------|
| Controllers | 100% |
| Services | 100% |
| Repositories | 80% |
| Batch Jobs | 90% |

---

## Running Tests

```bash
# Run all tests
mvn test

# Run with coverage
mvn clean test jacoco:report

# View report
open target/site/jacoco/index.html
```

---

## Best Practices

1. **Test Behavior:** Not implementation details
2. **One Assert Per Test:** Focus on single behavior
3. **Use Descriptive Names:** Clear test method names
4. **Arrange-Act-Assert:** Follow AAA pattern
5. **Mock External Dependencies:** Database, APIs

---

*Part of the OpenSearchClientsWithAWSSigV4 Learning Documentation*
