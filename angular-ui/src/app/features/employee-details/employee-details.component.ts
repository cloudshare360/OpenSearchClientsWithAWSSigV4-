import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { EmployeeService } from '../../core/services/employee.service';
import { Employee } from '../../shared/models/employee.model';

@Component({
  selector: 'app-employee-details',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="employee-details" *ngIf="employee">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <h2>Employee Details</h2>
        <div>
          <a [routerLink]="['/employees', employee.id, 'edit']" class="btn btn-warning me-2">Edit</a>
          <a routerLink="/employees" class="btn btn-secondary">Back</a>
        </div>
      </div>

      <div class="card">
        <div class="card-body">
          <div class="row mb-3">
            <div class="col-md-6">
              <strong>Employee ID:</strong> {{ employee.id }}
            </div>
            <div class="col-md-6">
              <strong>Status:</strong>
              <span class="badge" [class.bg-success]="employee.isActive" [class.bg-danger]="!employee.isActive">
                {{ employee.isActive ? 'Active' : 'Inactive' }}
              </span>
            </div>
          </div>
          <div class="row mb-3">
            <div class="col-md-6">
              <strong>First Name:</strong> {{ employee.firstName }}
            </div>
            <div class="col-md-6">
              <strong>Last Name:</strong> {{ employee.lastName }}
            </div>
          </div>
          <div class="row mb-3">
            <div class="col-md-12">
              <strong>Email:</strong> {{ employee.email }}
            </div>
          </div>
          <div class="row mb-3">
            <div class="col-md-6">
              <strong>Department:</strong> {{ employee.department }}
            </div>
            <div class="col-md-6">
              <strong>Position:</strong> {{ employee.position }}
            </div>
          </div>
          <div class="row mb-3">
            <div class="col-md-6">
              <strong>Salary:</strong> {{ employee.salary | currency }}
            </div>
            <div class="col-md-6">
              <strong>Hire Date:</strong> {{ employee.hireDate }}
            </div>
          </div>
          <div class="row mb-3">
            <div class="col-md-6">
              <strong>Created At:</strong> {{ employee.createdAt }}
            </div>
            <div class="col-md-6">
              <strong>Updated At:</strong> {{ employee.updatedAt }}
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class EmployeeDetailsComponent implements OnInit {
  employee: Employee | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private employeeService: EmployeeService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.params['id'];
    if (id) {
      this.loadEmployee(id);
    }
  }

  loadEmployee(id: number): void {
    this.employeeService.getEmployee(id).subscribe({
      next: (data) => {
        this.employee = data;
      },
      error: (error) => {
        console.error('Error loading employee:', error);
        this.router.navigate(['/employees']);
      }
    });
  }
}
