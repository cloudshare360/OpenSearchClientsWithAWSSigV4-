# Playwright UI Testing Guide

**Project:** OpenSearch Employee Search Platform  
**Purpose:** Learn Playwright for UI testing  
**Version:** 1.0  
**Date:** 2026-08-16

---

## What is Playwright?

Playwright is a Node.js library for browser automation and testing. It supports Chromium, Firefox, and WebKit.

### Key Concepts

1. **Test:** A test case
2. **Page:** Browser page/tab
3. **Locator:** Element selector
4. **Assertion:** Verification of expected state
5. **Fixture:** Setup/teardown logic

---

## Project Setup

### Prerequisites
```bash
# Install Node.js 20+
node --version

# Install Playwright
npm init playwright@latest
```

### Directory Structure
```
tests/
├── search/
│   ├── search-employees.spec.ts
│   └── advanced-search.spec.ts
├── employees/
│   ├── create-employee.spec.ts
│   ├── edit-employee.spec.ts
│   └── delete-employee.spec.ts
├── dashboard/
│   └── dashboard.spec.ts
└── global-setup.ts
```

---

## Basic Test Example

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
    
    // Submit form
    await page.click('button[type="submit"]');
    
    // Verify success
    await page.waitForSelector('text=Employee created successfully');
  });
});
```

---

## Common Operations

### Navigation
```typescript
// Go to URL
await page.goto('http://localhost:4200');

// Click link
await page.click('text=Employees');

// Go back
await page.goBack();
```

### Form Interaction
```typescript
// Fill input
await page.fill('#firstName', 'John');

// Select option
await page.selectOption('#department', 'Engineering');

// Check checkbox
await page.check('#isActive');

// Click button
await page.click('button[type="submit"]');
```

### Assertions
```typescript
// Text assertion
await expect(page.locator('h1')).toContainText('Employees');

// Count assertion
await expect(page.locator('.employee-card')).toHaveCount(10);

// Visibility assertion
await expect(page.locator('.spinner')).toBeVisible();
```

---

## Page Object Model

```typescript
// pages/employee.page.ts
export class EmployeePage {
  constructor(private page: Page) {}
  
  async goto() {
    await this.page.goto('http://localhost:4200/employees');
  }
  
  async search(query: string) {
    await this.page.fill('[data-testid="search-input"]', query);
    await this.page.click('[data-testid="search-button"]');
  }
  
  async createEmployee(employee: Employee) {
    await this.page.click('text=New Employee');
    await this.page.fill('#firstName', employee.firstName);
    await this.page.fill('#lastName', employee.lastName);
    await this.page.fill('#email', employee.email);
    await this.page.click('button[type="submit"]');
  }
}

// Test using page object
test('should create employee', async ({ page }) => {
  const employeePage = new EmployeePage(page);
  await employeePage.goto();
  await employeePage.createEmployee({
    firstName: 'John',
    lastName: 'Doe',
    email: 'john@example.com'
  });
});
```

---

## Running Tests

```bash
# Run all tests
npx playwright test

# Run specific test file
npx playwright test search.spec.ts

# Run in headed mode
npx playwright test --headed

# Run with UI
npx playwright test --ui

# Generate report
npx playwright show-report
```

---

## Best Practices

1. **Use Data Test IDs:** Stable selectors
2. **Wait for Elements:** Use proper waits
3. **Avoid Sleeps:** Use auto-waiting
4. **Page Object Model:** Reusable page objects
5. **Independent Tests:** Each test should be independent
6. **Clean Test Data:** Reset state between tests

---

## Next Steps

1. Complete [Playwright Basics](basics.md)
2. Learn [Selectors](selectors.md)
3. Study [Assertions](assertions.md)
4. Practice with sample tests

---

*Part of the OpenSearchClientsWithAWSSigV4 Learning Documentation*
