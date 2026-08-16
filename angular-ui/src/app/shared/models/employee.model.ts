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
  createdAt: string;
  updatedAt: string;
}

export interface EmployeeCreateRequest {
  firstName: string;
  lastName: string;
  email: string;
  department: string;
  position: string;
  salary?: number;
  hireDate: string;
  isActive?: boolean;
}

export interface EmployeeUpdateRequest extends EmployeeCreateRequest {
  id: number;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
}

export interface SearchRequest {
  query?: string;
  department?: string;
  position?: string;
  minSalary?: number;
  maxSalary?: number;
  page?: number;
  size?: number;
}
