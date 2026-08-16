# Testing Learning Guide

**Project:** OpenSearch Employee Search Platform  
**Purpose:** Learn testing strategies for this project  
**Version:** 1.0  
**Date:** 2026-08-16

---

## Testing Strategy

This project uses a comprehensive testing strategy covering unit tests, integration tests, and UI tests.

### Testing Pyramid

```
        ┌─────────┐
        │   E2E   │  Playwright UI Tests
        │   Tests │
        ├─────────┤
        │Integration│ Cucumber REST API Tests
        │  Tests   │
        ├─────────┤
        │  Unit    │ JUnit + Mockito Tests
        │  Tests   │
        └─────────┘
```

### Test Coverage Goals
- **Unit Tests:** 100% coverage for business logic
- **Integration Tests:** All REST API endpoints
- **UI Tests:** Critical user journeys

---

## Testing Tools

| Tool | Purpose | Version |
|------|---------|---------|
| **JUnit 5** | Unit testing framework | 5.10+ |
| **Mockito** | Mocking framework | 5.7+ |
| **Spring Test** | Spring Boot test support | 3.3+ |
| **Cucumber** | BDD integration tests | 7.14+ |
| **Playwright** | UI automation | 1.40+ |
| **JaCoCo** | Code coverage | 0.8+ |

---

## Unit Testing

### Spring Boot REST Service Example

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
    
    @Test
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        Employee employee = new Employee("John", "Doe", "existing@example.com");
        when(repository.existsByEmail("existing@example.com")).thenReturn(true);
        
        // When/Then
        assertThrows(DuplicateEmailException.class, () -> {
            service.create(employee);
        });
    }
}
```

### Spring Boot Batch Test Example

```java
@SpringBootTest
@SpringBatchTest
class EmployeeSyncJobTest {
    
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    
    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;
    
    @Test
    void shouldSyncEmployees() throws Exception {
        // Given
        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();
        
        // When
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        
        // Then
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
    }
}
```

---

## Integration Testing with Cucumber

### Feature File
```gherkin
Feature: Employee Management API
  As an API user
  I want to manage employees
  So that I can maintain employee records

  Scenario: Create a new employee
    Given the API is available
    When I create an employee with:
      | firstName | John     |
      | lastName  | Doe      |
      | email     | john@example.com |
      | department | Engineering |
      | position   | Software Engineer |
    Then the employee is created successfully
    And the response status is 201
    And the employee has an ID

  Scenario: Search employees
    Given the following employees exist:
      | firstName | lastName | department |
      | John      | Doe      | Engineering |
      | Jane      | Smith    | Sales      |
    When I search for "John"
    Then I should find 1 employee
    And the employee name is "John Doe"

  Scenario: Update employee
    Given an employee exists with ID 1
    When I update the employee's position to "Senior Engineer"
    Then the employee is updated successfully
    And the response status is 200
```

### Step Definitions
```java
@Given("the API is available")
public void theApiIsAvailable() {
    // Setup test environment
    restAssured.setBaseUri("http://localhost:8080");
}

@When("I create an employee with:")
public void iCreateAnEmployeeWith(DataTable table) {
    Map<String, String> data = table.asMap(String.class, String.class);
    
    response = given()
        .contentType(JSON)
        .body(data)
    .when()
        .post("/api/employees");
}

@Then("the employee is created successfully")
public void theEmployeeIsCreatedSuccessfully() {
    assertEquals(201, response.getStatusCode());
}
```

---

## UI Testing with Playwright

### Test Example
```typescript
import { test, expect } from '@playwright/test';

test.describe('Employee Search', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('http://localhost:4200');
  });

  test('should search for employees', async ({ page }) => {
    // Fill search input
    await page.fill('[data-testid="search-input"]', 'John');
    
    // Click search button
    await page.click('[data-testid="search-button"]');
    
    // Wait for results
    await page.waitForSelector('[data-testid="employee-card"]');
    
    // Verify results
    const results = await page.locator('[data-testid="employee-card"]').count();
    expect(results).toBeGreaterThan(0);
  });

  test('should create new employee', async ({ page }) => {
    // Navigate to create form
    await page.click('text=New Employee');
    
    // Fill form
    await page.fill('#firstName', 'John');
    await page.fill('#lastName', 'Doe');
    await page.fill('#email', 'john@example.com');
    await page.selectOption('#department', 'Engineering');
    await page.fill('#position', 'Software Engineer');
    
    // Submit form
    await page.click('button[type="submit"]');
    
    // Verify success message
    await page.waitForSelector('text=Employee created successfully');
  });
});
```

### Page Object Model
```typescript
class EmployeePage {
  constructor(private page: Page) {}
  
  async search(query: string) {
    await this.page.fill('[data-testid="search-input"]', query);
    await this.page.click('[data-testid="search-button"]');
  }
  
  async createEmployee(employee: Employee) {
    await this.page.click('text=New Employee');
    await this.page.fill('#firstName', employee.firstName);
    await this.page.fill('#lastName', employee.lastName);
    await this.page.fill('#email', employee.email);
    await this.page.selectOption('#department', employee.department);
    await this.page.fill('#position', employee.position);
    await this.page.click('button[type="submit"]');
  }
}
```

---

## Batch Integration Testing

### Test Configuration
```java
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
class EmployeeSyncJobIntegrationTest {
    
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Test
    void shouldSyncEmployeesToOpenSearch() throws Exception {
        // Given - Create test employees
        Employee employee1 = new Employee("John", "Doe", "john@example.com");
        Employee employee2 = new Employee("Jane", "Smith", "jane@example.com");
        employeeRepository.saveAll(List.of(employee1, employee2));
        
        // When - Run batch job
        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        
        // Then - Verify job completed
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
        
        // Verify OpenSearch index (would need test container)
        // SearchRequest searchRequest = new SearchRequest.Builder()
        //     .index("employees")
        //     .query(q -> q.matchAll(m -> m))
        //     .build();
        // SearchResponse<Employee> response = openSearchClient.search(searchRequest);
        // assertEquals(2, response.hits().total().value());
    }
}
```

---

## Test Data Management

### Using Test Containers
```java
@Testcontainers
@SpringBootTest
class EmployeeRepositoryTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

### Using @Sql for Test Data
```java
@SpringBootTest
@Sql(scripts = "/test-data/employees.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class EmployeeServiceTest {
    
    @Test
    void shouldFindAllEmployees() {
        List<Employee> employees = employeeService.getAll();
        assertEquals(10, employees.size());
    }
}
```

---

## Code Coverage

### JaCoCo Configuration
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <configuration>
        <outputDirectory>${project.build.directory}/jacoco</outputDirectory>
        <excludes>
            <exclude>**/config/**</exclude>
            <exclude>**/dto/**</exclude>
        </excludes>
    </configuration>
</plugin>
```

### Coverage Goals
- **Line Coverage:** > 80%
- **Branch Coverage:** > 70%
- **Critical Paths:** 100%

---

## Running Tests

### Maven Commands
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=EmployeeServiceTest

# Run with coverage
mvn clean test jacoco:report

# Skip tests
mvn install -DskipTests
```

### Cucumber Tests
```bash
# Run Cucumber tests
mvn test -Dcucumber.options="--tags @smoke"

# Generate Cucumber report
mvn test -Dcucumber.options="--plugin pretty"
```

### Playwright Tests
```bash
# Run all UI tests
npx playwright test

# Run specific test file
npx playwright test search.spec.ts

# Run with UI
npx playwright test --ui
```

---

## Best Practices

1. **Write Tests First:** TDD approach
2. **Test Behavior:** Not implementation details
3. **Use Descriptive Names:** Clear test method names
4. **Arrange-Act-Assert:** Follow AAA pattern
5. **One Assert Per Test:** Focus on single behavior
6. **Mock External Dependencies:** Database, APIs, file system
7. **Use Test Containers:** For integration tests
8. **Clean Up Test Data:** Don't leave test artifacts

---

## Next Steps

1. Complete [Unit Testing](unit-testing/junit.md)
2. Learn [Cucumber BDD](cucumber/basics.md)
3. Study [Playwright](playwright/basics.md)
4. Practice with sample tests

---

*Part of the OpenSearchClientsWithAWSSigV4 Learning Documentation*
