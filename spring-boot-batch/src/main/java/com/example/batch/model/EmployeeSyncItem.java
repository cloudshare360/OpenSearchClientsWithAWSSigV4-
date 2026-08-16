package com.example.batch.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EmployeeSyncItem {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private String position;
    private BigDecimal salary;
    private LocalDate hireDate;
    private Boolean isActive;
    private String operation; // CREATE, UPDATE, DELETE

    public EmployeeSyncItem() {}

    public EmployeeSyncItem(Long id, String firstName, String lastName, String email, String department, String position, BigDecimal salary, LocalDate hireDate, Boolean isActive, String operation) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
        this.position = position;
        this.salary = salary;
        this.hireDate = hireDate;
        this.isActive = isActive;
        this.operation = operation;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
}
