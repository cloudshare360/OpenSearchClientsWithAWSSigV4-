import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EmployeeService } from '../../core/services/employee.service';
import { Employee } from '../../shared/models/employee.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="dashboard">
      <h2>Dashboard</h2>
      <div class="row">
        <div class="col-md-3">
          <div class="card stats-card">
            <h3 class="text-primary">{{ stats.total || 0 }}</h3>
            <p>Total Employees</p>
          </div>
        </div>
        <div class="col-md-3">
          <div class="card stats-card">
            <h3 class="text-success">{{ stats.active || 0 }}</h3>
            <p>Active Employees</p>
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
            <h3 class="text-warning">{{ stats.recentHires || 0 }}</h3>
            <p>Recent Hires</p>
          </div>
        </div>
      </div>
      <div class="row mt-4">
        <div class="col-md-12">
          <div class="card">
            <div class="card-body">
              <h5 class="card-title">Quick Actions</h5>
              <a routerLink="/search" class="btn btn-primary me-2">Search Employees</a>
              <a routerLink="/employees/new" class="btn btn-success">Add Employee</a>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class DashboardComponent implements OnInit {
  stats: any = {};

  constructor(private employeeService: EmployeeService) {}

  ngOnInit(): void {
    this.loadStats();
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
}
