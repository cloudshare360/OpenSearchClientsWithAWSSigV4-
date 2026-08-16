package com.example.opensearch.controller;

import com.example.opensearch.model.Employee;
import com.example.opensearch.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Controller
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/")
    public String searchPage(Model model,
                             @RequestParam(defaultValue = "") String q,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Employee> employees;
        
        if (q != null && !q.isBlank()) {
            employees = employeeService.searchEmployees(q, pageable);
            model.addAttribute("searchQuery", q);
        } else {
            employees = employeeService.getAllEmployees(pageable);
        }
        
        model.addAttribute("employees", employees);
        model.addAttribute("stats", employeeService.getEmployeeStats());
        model.addAttribute("departments", new String[]{"Engineering", "Sales", "Marketing", "HR", "Finance"});
        model.addAttribute("pageTitle", "Employee Search");
        return "search";
    }

    @GetMapping("/employees/new")
    public String createForm(Model model) {
        model.addAttribute("employee", new Employee());
        model.addAttribute("pageTitle", "New Employee");
        return "form";
    }

    @GetMapping("/employees/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Employee employee = employeeService.getEmployeeById(id);
        model.addAttribute("employee", employee);
        model.addAttribute("pageTitle", "Edit Employee");
        return "form";
    }

    @GetMapping("/employees/{id}")
    public String viewEmployee(@PathVariable Long id, Model model) {
        Employee employee = employeeService.getEmployeeById(id);
        model.addAttribute("employee", employee);
        model.addAttribute("pageTitle", "Employee Details");
        return "details";
    }

    @PostMapping("/employees")
    public String createEmployee(@ModelAttribute Employee employee, RedirectAttributes redirectAttributes) {
        try {
            employeeService.createEmployee(employee);
            redirectAttributes.addFlashAttribute("success", "Employee created successfully!");
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create employee: " + e.getMessage());
            return "redirect:/employees/new";
        }
    }

    @PostMapping("/employees/{id}")
    public String updateEmployee(@PathVariable Long id, @ModelAttribute Employee employee, RedirectAttributes redirectAttributes) {
        try {
            employeeService.updateEmployee(id, employee);
            redirectAttributes.addFlashAttribute("success", "Employee updated successfully!");
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update employee: " + e.getMessage());
            return "redirect:/employees/" + id + "/edit";
        }
    }

    @PostMapping("/employees/{id}/delete")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            employeeService.deleteEmployee(id);
            redirectAttributes.addFlashAttribute("success", "Employee deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete employee: " + e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/api/employees")
    @ResponseBody
    public ResponseEntity<?> apiSearch(@RequestParam(defaultValue = "") String q,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Employee> employees = (q != null && !q.isBlank()) 
                    ? employeeService.searchEmployees(q, pageable)
                    : employeeService.getAllEmployees(pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", employees.getContent());
            response.put("totalElements", employees.getTotalElements());
            response.put("totalPages", employees.getTotalPages());
            response.put("currentPage", employees.getNumber());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
