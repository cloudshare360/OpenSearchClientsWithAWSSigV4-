import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
      <div class="container-fluid">
        <a class="navbar-brand" routerLink="/search">
          <i class="bi bi-search"></i> Employee Search
        </a>
        <div class="navbar-nav">
          <a class="nav-link" routerLink="/search" routerLinkActive="active">Search</a>
          <a class="nav-link" routerLink="/employees" routerLinkActive="active">Employees</a>
          <a class="nav-link" routerLink="/dashboard" routerLinkActive="active">Dashboard</a>
          <a class="nav-link" routerLink="/employees/new">New Employee</a>
        </div>
      </div>
    </nav>
    <div class="container mt-4">
      <router-outlet></router-outlet>
    </div>
  `
})
export class AppComponent {
  title = 'Employee Search';
}
