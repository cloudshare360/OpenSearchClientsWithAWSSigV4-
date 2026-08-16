import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Employee, EmployeeCreateRequest, EmployeeUpdateRequest, PagedResponse, SearchRequest } from '../models/employee.model';

@Injectable({
  providedIn: 'root'
})
export class EmployeeService {
  private baseUrl = `${environment.apiUrl}/employees`;

  constructor(private http: HttpClient) {}

  getEmployees(page: number = 0, size: number = 10): Observable<PagedResponse<Employee>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PagedResponse<Employee>>(this.baseUrl, { params });
  }

  getEmployee(id: number): Observable<Employee> {
    return this.http.get<Employee>(`${this.baseUrl}/${id}`);
  }

  createEmployee(employee: EmployeeCreateRequest): Observable<Employee> {
    return this.http.post<Employee>(this.baseUrl, employee);
  }

  updateEmployee(id: number, employee: EmployeeUpdateRequest): Observable<Employee> {
    return this.http.put<Employee>(`${this.baseUrl}/${id}`, employee);
  }

  deleteEmployee(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}/delete`);
  }

  search(request: SearchRequest): Observable<PagedResponse<Employee>> {
    let params = new HttpParams();
    if (request.query) params = params.set('q', request.query);
    if (request.department) params = params.set('department', request.department);
    if (request.page !== undefined) params = params.set('page', request.page.toString());
    if (request.size !== undefined) params = params.set('size', request.size.toString());
    return this.http.get<PagedResponse<Employee>>(`${this.baseUrl}/search`, { params });
  }

  getStats(): Observable<any> {
    return this.http.get(`${this.baseUrl}/stats`);
  }
}
