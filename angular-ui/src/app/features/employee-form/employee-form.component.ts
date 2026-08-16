import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { EmployeeService } from '../../core/services/employee.service';
import { Employee } from '../../shared/models/employee.model';

@Component({
  selector: 'app-employee-form',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  template: `
    <div class="employee-form">
      <h2>{{ isEdit ? 'Edit Employee' : 'New Employee' }}</h2>
      <div class="card">
        <div class="card-body">
          <form [formGroup]="employeeForm" (ngSubmit)="onSubmit()">
            <div class="row">
              <div class="col-md-6 mb-3">
                <label class="form-label">First Name *</label>
                <input type="text" class="form-control" formControlName="firstName" [class.is-invalid]="isInvalid('firstName')">
                <div class="invalid-feedback" *ngIf="isInvalid('firstName')">First name is required</div>
              </div>
              <div class="col-md-6 mb-3">
                <label class="form-label">Last Name *</label>
                <input type="text" class="form-control" formControlName="lastName" [class.is-invalid]="isInvalid('lastName')">
                <div class="invalid-feedback" *ngIf="isInvalid('lastName')">Last name is required</div>
              </div>
            </div>
            <div class="mb-3">
              <label class="form-label">Email *</label>
              <input type="email" class="form-control" formControlName="email" [class.is-invalid]="isInvalid('email')">
              <div class="invalid-feedback" *ngIf="isInvalid('email')">Valid email is required</div>
            </div>
            <div class="row">
              <div class="col-md-6 mb-3">
                <label class="form-label">Department *</label>
                <select class="form-select" formControlName="department" [class.is-invalid]="isInvalid('department')">
                  <option value="">Select Department</option>
                  <option value="Engineering">Engineering</option>
                  <option value="Sales">Sales</option>
                  <option value="Marketing">Marketing</option>
                  <option value="HR">HR</option>
                  <option value="Finance">Finance</option>
                </select>
                <div class="invalid-feedback" *ngIf="isInvalid('department')">Department is required</div>
              </div>
              <div class="col-md-6 mb-3">
                <label class="form-label">Position *</label>
                <input type="text" class="form-control" formControlName="position" [class.is-invalid]="isInvalid('position')">
                <div class="invalid-feedback" *ngIf="isInvalid('position')">Position is required</div>
              </div>
            </div>
            <div class="row">
              <div class="col-md-6 mb-3">
                <label class="form-label">Salary</label>
                <input type="number" class="form-control" formControlName="salary" step="0.01">
              </div>
              <div class="col-md-6 mb-3">
                <label class="form-label">Hire Date *</label>
                <input type="date" class="form-control" formControlName="hireDate" [class.is-invalid]="isInvalid('hireDate')">
                <div class="invalid-feedback" *ngIf="isInvalid('hireDate')">Hire date is required</div>
              </div>
            </div>
            <div class="mb-3">
              <div class="form-check">
                <input class="form-check-input" type="checkbox" formControlName="isActive" id="isActive">
                <label class="form-check-label" for="isActive">Active</label>
              </div>
            </div>
            <div class="d-flex gap-2">
              <button type="submit" class="btn btn-primary" [disabled]="employeeForm.invalid || loading">
                {{ loading ? 'Saving...' : (isEdit ? 'Update' : 'Create') }}
              </button>
              <a routerLink="/employees" class="btn btn-secondary">Cancel</a>
            </div>
          </form>
        </div>
      </div>
    </div>
  `
})
export class EmployeeFormComponent implements OnInit {
  employeeForm: FormGroup;
  isEdit: boolean = false;
  loading: boolean = false;
  employeeId?: number;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private employeeService: EmployeeService
  ) {
    this.employeeForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      department: ['', Validators.required],
      position: ['', Validators.required],
      salary: [null],
      hireDate: ['', Validators.required],
      isActive: [true]
    });
  }

  ngOnInit(): void {
    this.employeeId = this.route.snapshot.params['id'];
    if (this.employeeId) {
      this.isEdit = true;
      this.loadEmployee();
    }
  }

  loadEmployee(): void {
    this.loading = true;
    this.employeeService.getEmployee(this.employeeId!).subscribe({
      next: (employee) => {
        this.employeeForm.patchValue({
          firstName: employee.firstName,
          lastName: employee.lastName,
          email: employee.email,
          department: employee.department,
          position: employee.position,
          salary: employee.salary,
          hireDate: employee.hireDate,
          isActive: employee.isActive
        });
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading employee:', error);
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.employeeForm.invalid) return;

    this.loading = true;
    const formData = this.employeeForm.value;

    if (this.isEdit && this.employeeId) {
      this.employeeService.updateEmployee(this.employeeId, { ...formData, id: this.employeeId }).subscribe({
        next: () => {
          this.router.navigate(['/employees']);
        },
        error: (error) => {
          console.error('Error updating employee:', error);
          alert('Failed to update employee');
          this.loading = false;
        }
      });
    } else {
      this.employeeService.createEmployee(formData).subscribe({
        next: () => {
          this.router.navigate(['/employees']);
        },
        error: (error) => {
          console.error('Error creating employee:', error);
          alert('Failed to create employee');
          this.loading = false;
        }
      });
    }
  }

  isInvalid(fieldName: string): boolean {
    const field = this.employeeForm.get(fieldName);
    return field ? field.invalid && (field.dirty || field.touched) : false;
  }
}
