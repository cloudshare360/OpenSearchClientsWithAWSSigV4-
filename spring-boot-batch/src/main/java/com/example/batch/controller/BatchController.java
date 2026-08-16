package com.example.batch.controller;

import com.example.batch.model.EmployeeSyncItem;
import com.example.batch.service.OpenSearchBatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/batch")
public class BatchController {

    private static final Logger log = LoggerFactory.getLogger(BatchController.class);

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job employeeSyncJob;

    @Autowired
    private OpenSearchBatchService openSearchBatchService;

    @PostMapping("/run")
    public ResponseEntity<?> runBatchJob(@RequestBody(required = false) Map<String, Object> request) {
        try {
            log.info("Starting batch job with request: {}", request);
            
            JobParametersBuilder jobParametersBuilder = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .addString("source", "manual");
            
            if (request != null) {
                if (request.containsKey("operation")) {
                    jobParametersBuilder.addString("operation", (String) request.get("operation"));
                }
                if (request.containsKey("employeeId")) {
                    jobParametersBuilder.addLong("employeeId", (Long) request.get("employeeId"));
                }
            }

            JobExecution jobExecution = jobLauncher.run(employeeSyncJob, jobParametersBuilder.toJobParameters());
            
            Map<String, Object> response = new HashMap<>();
            response.put("jobId", jobExecution.getId());
            response.put("status", jobExecution.getStatus());
            response.put("startTime", jobExecution.getStartTime());
            response.put("endTime", jobExecution.getEndTime());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to start batch job", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getJobStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("message", "Batch service is running");
        status.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(status);
    }
}
