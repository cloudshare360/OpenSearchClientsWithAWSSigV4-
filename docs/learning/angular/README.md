# Angular Learning Guide

**Project:** OpenSearch Employee Search Platform  
**Purpose:** Learn Angular for this project  
**Version:** 1.0  
**Date:** 2026-08-16

---

## What is Angular?

Angular is a TypeScript-based web application framework developed by Google. It uses component-based architecture and is well-suited for building enterprise-scale applications.

### Key Concepts for This Project

1. **Components:** Building blocks of the UI
2. **Services:** Business logic and data access
3. **Modules:** Organization of components and services
4. **Routing:** Navigation between pages
5. **Forms:** User input handling
6. **HTTP Client:** API communication

---

## Project Structure

```
angular-ui/
├── src/
│   ├── app/
│   │   ├── core/
│   │   │   ├── services/
│   │   │   │   └── employee.service.ts
│   │   │   └── interceptors/
│   │   │       └── auth.interceptor.ts
│   │   ├── shared/
│   │   │   ├── models/
│   │   │   │   └── employee.model.ts
│   │   │   └── components/
│   │   │       ├── confirm-dialog/
│   │   │       └── loading-spinner/
│   │   ├── features/
│   │   │   ├── dashboard/
│   │   │   ├── employee-list/
│   │   │   ├── employee-form/
│   │   │   ├── employee-details/
│   │   │   └── search/
│   │   ├── app.component.ts
│   │   └── app.routes.ts
│   ├── environments/
│   │   ├── environment.ts
│   │   └── environment.prod.ts
│   └── index.html
├── package.json
├── tsconfig.json
└── angular.json
```

---

## Key Concepts

### Components
Components are the building blocks of Angular applications. Each component has:
- **Selector:** HTML tag used to include the component
- **Template:** HTML structure
- **Class:** TypeScript logic
- **Styles:** CSS styling

**Example:**
```typescript
@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="employee-list">
      <h2>Employees</h2>
      <div *ngFor="let employee of employees">
        {{ employee.firstName }} {{ employee.lastName }}
      </div>
    </div>
  `
})
export class EmployeeListComponent {
  employees: Employee[] = [];
  
  constructor(private employeeService: EmployeeService) {}
  
  ngOnInit() {
    this.loadEmployees();
  }
  
  loadEmployees() {
    this.employeeService.getEmployees().subscribe(data => {
      this.employees = data;
    });
  }
}
```

### Services
Services handle business logic and data operations. They are singletons that can be injected into components.

**Example:**
```typescript
@Injectable({
  providedIn: 'root'
})
export class EmployeeService {
  private baseUrl = 'http://localhost:8080/api/employees';
  
  constructor(private http: HttpClient) {}
  
  getEmployees(): Observable<Employee[]> {
    return this.http.get<Employee[]>(this.baseUrl);
  }
  
  createEmployee(employee: Employee): Observable<Employee> {
    return this.http.post<Employee>(this.baseUrl, employee);
  }
}
```

### Routing
Angular Router enables navigation between different views.

**Example:**
```typescript
export const routes: Routes = [
  { path: '', redirectTo: '/search', pathMatch: 'full' },
  { path: 'employees', component: EmployeeListComponent },
  { path: 'employees/new', component: EmployeeFormComponent },
  { path: 'employees/:id', component: EmployeeDetailsComponent }
];
```

### Forms
Angular provides two types of forms:
1. **Template-driven forms:** Simple forms using ngModel
2. **Reactive forms:** More control with FormBuilder

**Example (Reactive):**
```typescript
export class EmployeeFormComponent {
  employeeForm: FormGroup;
  
  constructor(private fb: FormBuilder) {
    this.employeeForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      department: ['', Validators.required]
    });
  }
  
  onSubmit() {
    if (this.employeeForm.valid) {
      console.log(this.employeeForm.value);
    }
  }
}
```

### HTTP Client
Angular's HttpClient is used for API communication.

**Example:**
```typescript
@Injectable({ providedIn: 'root' })
export class EmployeeService {
  constructor(private http: HttpClient) {}
  
  getEmployees(): Observable<Employee[]> {
    return this.http.get<Employee[]>('/api/employees');
  }
  
  createEmployee(employee: CreateEmployeeRequest): Observable<Employee> {
    return this.http.post<Employee>('/api/employees', employee);
  }
}
```

---

## Project-Specific Implementation

### Environment Configuration
```typescript
// environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  batchUrl: 'http://localhost:8081/batch'
};

// environment.prod.ts
export const environment = {
  production: true,
  apiUrl: '/api',
  batchUrl: '/batch'
};
```

### Employee Service
```typescript
@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private baseUrl = `${environment.apiUrl}/employees`;
  
  constructor(private http: HttpClient) {}
  
  getEmployees(page: number = 0, size: number = 10) {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PagedResponse<Employee>>(this.baseUrl, { params });
  }
  
  search(query: string) {
    const params = new HttpParams().set('q', query);
    return this.http.get<PagedResponse<Employee>>(`${this.baseUrl}/search`, { params });
  }
}
```

### Search Component
```typescript
@Component({
  selector: 'app-search',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="search-page">
      <h2>Employee Search</h2>
      <form (ngSubmit)="onSearch()">
        <input [(ngModel)]="searchQuery" name="searchQuery" 
               placeholder="Search employees...">
        <button type="submit">Search</button>
      </form>
      
      <div *ngFor="let employee of employees">
        {{ employee.firstName }} {{ employee.lastName }}
      </div>
    </div>
  `
})
export class SearchComponent {
  searchQuery = '';
  employees: Employee[] = [];
  
  constructor(private employeeService: EmployeeService) {}
  
  onSearch() {
    this.employeeService.search(this.searchQuery).subscribe(data => {
      this.employees = data.content;
    });
  }
}
```

---

## Common Patterns in This Project

### 1. Service Layer Pattern
All API calls go through services:
```typescript
// Component calls service
// Service calls API
// Result returned to component
```

### 2. Model/Interface Pattern
TypeScript interfaces define data structures:
```typescript
export interface Employee {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  department: string;
  position: string;
  salary: number;
  hireDate: string;
  isActive: boolean;
}
```

### 3. Environment Pattern
Use environment files for configuration:
```typescript
// Development
apiUrl: 'http://localhost:8080/api'

// Production
apiUrl: '/api'
```

---

## Best Practices

1. **Use Standalone Components:** Angular 17+ default
2. **Keep Components Small:** Single responsibility
3. **Use Services:** Don't put API logic in components
4. **Handle Loading States:** Show spinners during API calls
5. **Handle Errors:** Display user-friendly error messages
6. **Use Reactive Forms:** For complex forms
7. **Use TrackBy:** For ngFor performance

---

## Next Steps

1. Complete [Angular Basics](basics.md)
2. Learn [Component Development](components.md)
3. Study [Services](services.md)
4. Practice with sample components

---

*Part of the OpenSearchClientsWithAWSSigV4 Learning Documentation*
