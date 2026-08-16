package com.example.opensearch.repository;

import com.example.opensearch.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeJpaRepository extends JpaRepository<Employee, Long> {

    Page<Employee> findByDepartment(String department, Pageable pageable);

    Page<Employee> findByIsActive(Boolean isActive, Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE " +
           "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.position) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Employee> search(@Param("query") String query, Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE e.salary >= :minSalary AND e.salary <= :maxSalary")
    Page<Employee> findBySalaryRange(@Param("minSalary") BigDecimal minSalary, 
                                      @Param("maxSalary") BigDecimal maxSalary, 
                                      Pageable pageable);

    List<Employee> findByHireDateBetween(LocalDate startDate, LocalDate endDate);
}
