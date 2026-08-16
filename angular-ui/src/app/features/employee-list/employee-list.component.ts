import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { EmployeeService } from '../../core/services/employee.service';
import { Employee, PagedResponse } from '../../shared/models/employee.model';

@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="employee-list">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <h2>Employees</h2>
        <a routerLink="/employees/new" class="btn btn-success">
          <i class="bi bi-plus"></i> Add Employee
        </a>
      </div>

      <div class="card mb-3">
        <div class="card-body">
          <form (ngSubmit)="onSearch()" class="row g-3">
            <div class="col-md-8">
              <input type="text" class="form-control" [(ngModel)]="searchQuery" name="searchQuery" placeholder="Search employees...">
            </div>
            <div class="col-md-4">
              <button type="submit" class="btn btn-primary w-100">Search</button>
            </div>
          </form>
        </div>
      </div>

      <div class="card" *ngIf="!loading">
        <div class="card-body">
          <div class="table-responsive">
            <table class="table table-hover">
              <thead class="table-light">
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Department</th>
                  <th>Position</th>
                  <th>Salary</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let employee of employees.content">
                  <td>{{ employee.id }}</td>
                  <td>{{ employee.firstName }} {{ employee.lastName }}</td>
                  <td>{{ employee.email }}</td>
                  <td><span class="badge bg-secondary">{{ employee.department }}</span></td>
                  <td>{{ employee.position }}</td>
                  <td>{{ employee.salary | currency }}</td>
                  <td>
                    <div class="btn-group btn-group-sm">
                      <a [routerLink]="['/employees', employee.id]" class="btn btn-info">View</a>
                      <a [routerLink]="['/employees', employee.id, 'edit']" class="btn btn-warning">Edit</a>
                      <button class="btn btn-danger" (click)="onDelete(employee)">Delete</button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <nav *ngIf="employees.totalPages > 1" class="mt-3">
            <ul class="pagination justify-content-center">
              <li class="page-item" [class.disabled]="employees.currentPage === 0">
                <a class="page-link" (click)="goToPage(employees.currentPage - 1)">Previous</a>
              </li>
              <li class="page-item active">
                <span class="page-link">{{ employees.currentPage + 1 }} / {{ employees.totalPages }}</span>
              </li>
              <li class="page-item" [class.disabled]="employees.currentPage === employees.totalPages - 1">
                <a class="page-link" (click)="goToPage(employees.currentPage + 1)">Next</a>
              </li>
            </ul>
          </nav>
        </div>
      </div>
    </div>
  `
})
export class EmployeeListComponent implements OnInit {
  employees: PagedResponse<Employee> = { content: [], totalElements: 0, totalPages: 0, currentPage: 0 };
  searchQuery: string = '';
  loading: boolean = false;
  currentPage: number = 0;

  constructor(
    private employeeService: EmployeeService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadEmployees();
  }

  loadEmployees(): void {
    this.loading = true;
    this.employeeService.getEmployees(this.currentPage, 10).subscribe({
      next: (data) => {
        this.employees = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading employees:', error);
        this.loading = false;
      }
    });
  }

  onSearch(): void {
    if (this.searchQuery.trim()) {
      this.employeeService.search({ query: this.searchQuery, page: 0, size: 10 }).subscribe({
        next: (data) => {
          this.employees = data;
        },
        error: (error) => {
          console.error('Error searching employees:', error);
        }
      });
    } else {
      this.loadEmployees();
    }
  }

  goToPage(page: number): void {
    this.currentPage = page;
    if (this.searchQuery.trim()) {
      this.onSearch();
    } else {
      this.loadEmployees();
    }
  }

  onDelete(employee: Employee): void {
    if (confirm(`Are you sure you want to delete ${employee.firstName} ${employee.lastName}?`)) {
      this.employeeService.deleteEmployee(employee.id).subscribe({
        next: () => {
          this.loadEmployees();
        },
        error: (error) => {
          console.error('Error deleting employee:', error);
          alert('Failed to delete employee');
        }
      });
    }
  }
}
