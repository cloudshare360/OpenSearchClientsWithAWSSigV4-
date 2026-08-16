# Cucumber REST API Testing Guide

**Project:** OpenSearch Employee Search Platform  
**Purpose:** Learn Cucumber for REST API testing  
**Version:** 1.0  
**Date:** 2026-08-16

---

## What is Cucumber?

Cucumber is a BDD (Behavior-Driven Development) tool that lets you write tests in plain language (Gherkin) and automate them.

### Key Concepts

1. **Feature:** A feature/functionality being tested
2. **Scenario:** A specific test case
3. **Steps:** Individual actions in a scenario
4. **Step Definitions:** Code that executes steps
5. **Hooks:** Setup/teardown logic

---

## Gherkin Syntax

```gherkin
Feature: Employee Management
  As a user
  I want to manage employees
  So that I can maintain employee records

  Scenario: Create employee
    Given the API is available
    When I create an employee
    Then the employee is created
```

---

## Project Setup

### Maven Dependencies
```xml
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-java</artifactId>
    <version>7.14.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-junit</artifactId>
    <version>7.14.0</version>
    <scope>test</scope>
</dependency>
```

### Directory Structure
```
src/test/resources/features/
├── employee/
│   ├── create-employee.feature
│   ├── search-employees.feature
│   └── update-employee.feature
```

---

## Example Feature

```gherkin
Feature: Employee Search
  As an API user
  I want to search employees
  So that I can find employee information

  Background:
    Given the API is available at "http://localhost:8080/api"

  Scenario: Search by name
    Given the following employees exist:
      | firstName | lastName | department |
      | John      | Doe      | Engineering |
      | Jane      | Smith    | Sales      |
    When I search for "John"
    Then I should find 1 employee
    And the employee name is "John Doe"

  Scenario: Search with no results
    When I search for "Nonexistent"
    Then I should find 0 employees
```

---

## Step Definitions

```java
public class EmployeeStepDefinitions {
    
    private Response response;
    private List<Employee> employees;
    private String baseUrl = "http://localhost:8080/api";
    
    @Given("the API is available at {string}")
    public void theApiIsAvailableAt(String url) {
        this.baseUrl = url;
    }
    
    @Given("the following employees exist:")
    public void theFollowingEmployeesExist(DataTable table) {
        // Setup test data
    }
    
    @When("I search for {string}")
    public void iSearchFor(String query) {
        response = given()
            .accept(ContentType.JSON)
            .when()
            .get(baseUrl + "/employees/search?q=" + query)
            .then()
            .extract().response();
    }
    
    @Then("I should find {int} employee")
    public void iShouldFindEmployee(int count) {
        List<Employee> results = response.jsonPath().getList("content");
        assertEquals(count, results.size());
    }
    
    @Then("the employee name is {string}")
    public void theEmployeeNameIs(String name) {
        String firstName = response.jsonPath().getString("content[0].firstName");
        assertTrue(name.contains(firstName));
    }
}
```

---

## Running Cucumber Tests

```bash
# Run all features
mvn test -Dcucumber.options="src/test/resources/features"

# Run specific feature
mvn test -Dcucumber.options="src/test/resources/features/employee/search-employees.feature"

# Generate report
mvn test -Dcucumber.options="--plugin pretty"
```

---

## Best Practices

1. **Write Scenarios from User Perspective:** Focus on behavior
2. **Use Background:** For common setup
3. **Keep Steps Independent:** Each step should be self-contained
4. **Use Data Tables:** For multiple inputs
5. **Avoid Technical Details:** Write in plain language

---

## Next Steps

1. Complete [Cucumber Basics](basics.md)
2. Learn [Writing Feature Files](feature-files.md)
3. Study [Step Definitions](step-defs.md)
4. Practice with sample features

---

*Part of the OpenSearchClientsWithAWSSigV4 Learning Documentation*
