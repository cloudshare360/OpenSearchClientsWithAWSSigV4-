# Angular Basics

**Project:** OpenSearch Employee Search Platform  
**Purpose:** Learn Angular fundamentals  
**Version:** 1.0  
**Date:** 2026-08-16

---

## What is Angular?

Angular is a TypeScript-based web application framework. It uses a component-based architecture to build scalable web applications.

### Key Concepts

1. **Component:** Building block with template, class, and styles
2. **Template:** HTML that defines the view
3. **Class:** TypeScript code with logic
4. **Module:** Container for components and services
5. **Service:** Reusable business logic
6. **Dependency Injection:** Built-in DI system

---

## Project Setup

### Prerequisites
- Node.js 20+
- npm or yarn

### Create New Project
```bash
# Install Angular CLI
npm install -g @angular/cli@17

# Create new project
ng new employee-search-ui --standalone --routing --style=css

# Navigate to project
cd employee-search-ui

# Serve application
ng serve
```

### Project Structure
```
employee-search-ui/
├── src/
│   ├── app/
│   │   ├── app.component.ts
│   │   ├── app.component.html
│   │   ├── app.component.css
│   │   ├── app.config.ts
│   │   └── app.routes.ts
│   ├── index.html
│   ├── main.ts
│   └── styles.css
├── angular.json
├── package.json
└── tsconfig.json
```

---

## First Component

### Component Class
```typescript
import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <h1>Hello {{ title }}!</h1>
    <button (click)="increment()">Count: {{ count }}</button>
  `
})
export class AppComponent {
  title = 'Employee Search';
  count = 0;
  
  increment() {
    this.count++;
  }
}
```

### Component with Separate Files
```typescript
// app.component.ts
import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'Employee Search';
}
```

```html
<!-- app.component.html -->
<h1>{{ title }}</h1>
```

---

## Data Binding

### Interpolation
```html
<h1>{{ title }}</h1>
<p>{{ employee.firstName }}</p>
```

### Property Binding
```html
<img [src]="employee.photoUrl">
<a [href]="employee.profileUrl">Profile</a>
```

### Event Binding
```html
<button (click)="onSave()">Save</button>
<input (input)="onSearch($event)">
```

### Two-Way Binding
```html
<input [(ngModel)]="employee.firstName">
```

---

## Directives

### Structural Directives
```html
<!-- *ngIf -->
<div *ngIf="employee">Employee exists</div>

<!-- *ngFor -->
<div *ngFor="let employee of employees">
  {{ employee.firstName }}
</div>

<!-- *ngSwitch -->
<div [ngSwitch]="department">
  <p *ngSwitchCase="'Engineering'">Engineering Dept</p>
  <p *ngSwitchDefault>Other Dept</p>
</div>
```

### Attribute Directives
```html
<!-- ngClass -->
<div [ngClass]="{'active': isActive, 'disabled': isDisabled}">

<!-- ngStyle -->
<div [ngStyle]="{'color': color, 'font-size': fontSize + 'px'}">
```

---

## Services and Dependency Injection

### Create Service
```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Employee {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
}

@Injectable({
  providedIn: 'root'
})
export class EmployeeService {
  private apiUrl = 'http://localhost:8080/api/employees';
  
  constructor(private http: HttpClient) {}
  
  getEmployees(): Observable<Employee[]> {
    return this.http.get<Employee[]>(this.apiUrl);
  }
  
  getEmployee(id: number): Observable<Employee> {
    return this.http.get<Employee>(`${this.apiUrl}/${id}`);
  }
}
```

### Use Service in Component
```typescript
@Component({
  selector: 'app-employee-list',
  standalone: true,
  template: `
    <div *ngFor="let employee of employees">
      {{ employee.firstName }} {{ employee.lastName }}
    </div>
  `
})
export class EmployeeListComponent {
  employees: Employee[] = [];
  
  constructor(private employeeService: EmployeeService) {}
  
  ngOnInit() {
    this.employeeService.getEmployees().subscribe(data => {
      this.employees = data;
    });
  }
}
```

---

## Routing

### Configure Routes
```typescript
// app.routes.ts
import { Routes } from '@angular/router';
import { EmployeeListComponent } from './features/employee-list/employee-list.component';
import { EmployeeFormComponent } from './features/employee-form/employee-form.component';

export const routes: Routes = [
  { path: '', redirectTo: '/employees', pathMatch: 'full' },
  { path: 'employees', component: EmployeeListComponent },
  { path: 'employees/new', component: EmployeeFormComponent }
];
```

### Navigation Links
```html
<nav>
  <a routerLink="/employees">Employees</a>
  <a routerLink="/employees/new">New Employee</a>
</nav>

<router-outlet></router-outlet>
```

---

## HTTP Client

### GET Request
```typescript
this.http.get('/api/employees').subscribe(data => {
  console.log(data);
});
```

### POST Request
```typescript
this.http.post('/api/employees', {
  firstName: 'John',
  lastName: 'Doe'
}).subscribe(data => {
  console.log('Created:', data);
});
```

### With Parameters
```typescript
const params = new HttpParams()
  .set('page', '0')
  .set('size', '10');

this.http.get('/api/employees', { params }).subscribe(data => {
  console.log(data);
});
```

### Error Handling
```typescript
this.http.get('/api/employees').subscribe({
  next: (data) => console.log(data),
  error: (error) => console.error('Error:', error),
  complete: () => console.log('Complete')
});
```

---

## Forms

### Template-Driven Forms
```html
<form #employeeForm="ngForm" (ngSubmit)="onSubmit(employeeForm)">
  <input name="firstName" [(ngModel)]="employee.firstName" required>
  <input name="email" [(ngModel)]="employee.email" type="email">
  <button type="submit" [disabled]="!employeeForm.valid">Save</button>
</form>
```

### Reactive Forms
```typescript
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

export class EmployeeFormComponent {
  employeeForm: FormGroup;
  
  constructor(private fb: FormBuilder) {
    this.employeeForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]]
    });
  }
  
  onSubmit() {
    if (this.employeeForm.valid) {
      console.log(this.employeeForm.value);
    }
  }
}
```

---

## Best Practices

1. **Use Standalone Components:** Angular 17+ default
2. **Keep Components Small:** Single responsibility
3. **Use Services:** For API calls and business logic
4. **Use Interfaces:** Define data models
5. **Handle Loading States:** Show spinners during API calls
6. **Handle Errors:** Display user-friendly messages
7. **Use Environment Files:** For API URLs

---

## Next Steps

1. Learn [Components](components.md)
2. Study [Services](services.md)
3. Understand [Routing](routing.md)
4. Practice with [Forms](forms.md)

---

*Part of the OpenSearchClientsWithAWSSigV4 Learning Documentation*
