import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { EmployeeService } from '../../core/services/employee.service';
import { Employee, PagedResponse } from '../../shared/models/employee.model';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="search-page">
      <h2>Employee Search</h2>

      <div class="card mb-4">
        <div class="card-body">
          <form (ngSubmit)="onSearch()" class="row g-3">
            <div class="col-md-8">
              <input type="text" class="form-control" [(ngModel)]="searchQuery" name="searchQuery" placeholder="Search by name, email, position...">
            </div>
            <div class="col-md-4">
              <button type="submit" class="btn btn-primary w-100">
                <i class="bi bi-search"></i> Search
              </button>
            </div>
          </form>
        </div>
      </div>

      <div class="row mb-3" *ngIf="stats">
        <div class="col-md-3">
          <div class="card stats-card">
            <h3 class="text-primary">{{ stats.total || 0 }}</h3>
            <p>Total Employees</p>
          </div>
        </div>
        <div class="col-md-3">
          <div class="card stats-card">
            <h3 class="text-success">{{ stats.active || 0 }}</h3>
            <p>Active</p>
          </div>
        </div>
        <div class="col-md-3">
          <div class="card stats-card">
            <h3 class="text-info">{{ stats.departments || 0 }}</h3>
            <p>Departments</p>
          </div>
        </div>
        <div class="col-md-3">
          <div class="card stats-card">
            <h3 class="text-warning">{{ employees.totalElements || 0 }}</h3>
            <p>Results</p>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-header d-flex justify-content-between align-items-center">
          <h5 class="mb-0">
            <i class="bi bi-people"></i> Employees
            <span *ngIf="searchQuery" class="text-muted">- Search: "{{ searchQuery }}"</span>
          </h5>
          <a routerLink="/employees/new" class="btn btn-success btn-sm">
            <i class="bi bi-plus"></i> Add Employee
          </a>
        </div>
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
                  <th>Hire Date</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let employee of employees.content">
                  <td>{{ employee.id }}</td>
                  <td><strong>{{ employee.firstName }} {{ employee.lastName }}</strong></td>
                  <td>{{ employee.email }}</td>
                  <td><span class="badge bg-secondary">{{ employee.department }}</span></td>
                  <td>{{ employee.position }}</td>
                  <td>{{ employee.salary | currency }}</td>
                  <td>{{ employee.hireDate }}</td>
                  <td>
                    <div class="btn-group btn-group-sm">
                      <a [routerLink]="['/employees', employee.id]" class="btn btn-info">View</a>
                      <a [routerLink]="['/employees', employee.id, 'edit']" class="btn btn-warning">Edit</a>
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
export class SearchComponent implements OnInit {
  employees: PagedResponse<any> = { content: [], totalElements: 0, totalPages: 0, currentPage: 0 };
  searchQuery: string = '';
  stats: any = {};

  constructor(
    private employeeService: EmployeeService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadEmployees();
    this.loadStats();
  }

  loadEmployees(): void {
    this.employeeService.search({ query: this.searchQuery, page: 0, size: 10 }).subscribe({
      next: (data) => {
        this.employees = data;
      },
      error: (error) => {
        console.error('Error searching employees:', error);
      }
    });
  }

  loadStats(): void {
    this.employeeService.getStats().subscribe({
      next: (data) => {
        this.stats = data;
      },
      error: (error) => {
        console.error('Error loading stats:', error);
      }
    });
  }

  onSearch(): void {
    this.loadEmployees();
  }

  goToPage(page: number): void {
    this.employeeService.search({ query: this.searchQuery, page, size: 10 }).subscribe({
      next: (data) => {
        this.employees = data;
      },
      error: (error) => {
        console.error('Error searching employees:', error);
      }
    });
  }
}
