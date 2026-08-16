package com.example.opensearch.service;

import com.example.opensearch.model.Employee;
import com.example.opensearch.repository.EmployeeJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);
    private final EmployeeJpaRepository employeeRepository;
    private final OpenSearchService openSearchService;
    private final RestTemplate restTemplate;

    @Value("${app.batch.trigger.enabled:false}")
    private boolean batchTriggerEnabled;

    @Value("${app.batch.trigger.url:http://spring-boot-batch:8081/batch/run}")
    private String batchTriggerUrl;

    public EmployeeService(EmployeeJpaRepository employeeRepository,
                           OpenSearchService openSearchService,
                           @Qualifier("restTemplate") RestTemplate restTemplate) {
        this.employeeRepository = employeeRepository;
        this.openSearchService = openSearchService;
        this.restTemplate = restTemplate;
    }

    @Transactional(readOnly = true)
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Employee> searchEmployees(String query, Pageable pageable) {
        return employeeRepository.search(query, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Employee> getEmployeesByDepartment(String department, Pageable pageable) {
        return employeeRepository.findByDepartment(department, pageable);
    }

    @Transactional(readOnly = true)
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
    }

    @Transactional
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public Employee createEmployee(Employee employee) {
        Employee saved = employeeRepository.save(employee);
        log.info("Created employee with id: {}", saved.getId());
        triggerBatchSync("CREATE", saved.getId());
        return saved;
    }

    @Transactional
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public Employee updateEmployee(Long id, Employee employeeDetails) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        
        existing.setFirstName(employeeDetails.getFirstName());
        existing.setLastName(employeeDetails.getLastName());
        existing.setEmail(employeeDetails.getEmail());
        existing.setDepartment(employeeDetails.getDepartment());
        existing.setPosition(employeeDetails.getPosition());
        existing.setSalary(employeeDetails.getSalary());
        existing.setHireDate(employeeDetails.getHireDate());
        existing.setIsActive(employeeDetails.getIsActive());

        Employee updated = employeeRepository.save(existing);
        log.info("Updated employee with id: {}", updated.getId());
        triggerBatchSync("UPDATE", updated.getId());
        return updated;
    }

    @Transactional
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void deleteEmployee(Long id) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        existing.setIsActive(false);
        employeeRepository.save(existing);
        log.info("Soft deleted employee with id: {}", id);
        triggerBatchSync("DELETE", id);
    }

    private void triggerBatchSync(String operation, Long employeeId) {
        if (!batchTriggerEnabled) {
            log.debug("Batch trigger disabled, skipping sync for operation: {} on employee: {}", operation, employeeId);
            return;
        }

        try {
            Map<String, Object> request = new HashMap<>();
            request.put("operation", operation);
            request.put("employeeId", employeeId);
            request.put("timestamp", System.currentTimeMillis());

            log.info("Triggering batch sync for operation: {} on employee: {}", operation, employeeId);
            restTemplate.postForEntity(batchTriggerUrl, request, String.class);
        } catch (Exception e) {
            log.warn("Failed to trigger batch sync: {}", e.getMessage());
            // Don't fail the main transaction if batch sync fails
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getEmployeeStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", employeeRepository.count());
        stats.put("active", employeeRepository.findByIsActive(true, Pageable.unpaged()).getTotalElements());
        stats.put("departments", employeeRepository.findAll().stream()
                .map(Employee::getDepartment)
                .distinct()
                .count());
        return stats;
    }
}
