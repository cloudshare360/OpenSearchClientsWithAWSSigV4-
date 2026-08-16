import { Routes } from '@angular/router';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { EmployeeListComponent } from './features/employee-list/employee-list.component';
import { EmployeeFormComponent } from './features/employee-form/employee-form.component';
import { EmployeeDetailsComponent } from './features/employee-details/employee-details.component';
import { SearchComponent } from './features/search/search.component';

export const routes: Routes = [
  { path: '', redirectTo: '/search', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'employees', component: EmployeeListComponent },
  { path: 'employees/new', component: EmployeeFormComponent },
  { path: 'employees/:id/edit', component: EmployeeFormComponent },
  { path: 'employees/:id', component: EmployeeDetailsComponent },
  { path: 'search', component: SearchComponent },
  { path: '**', redirectTo: '/search' }
];
