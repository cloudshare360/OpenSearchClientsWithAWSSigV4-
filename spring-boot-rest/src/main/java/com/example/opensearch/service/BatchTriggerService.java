package com.example.opensearch.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BatchTriggerService {

    private static final Logger log = LoggerFactory.getLogger(BatchTriggerService.class);
    private final RestTemplate restTemplate;

    public BatchTriggerService(@Qualifier("restTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
    public void triggerBatchSync(String operation, Long employeeId) {
        try {
            org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<>(
                "{\"operation\":\"" + operation + "\",\"employeeId\":" + employeeId + ",\"timestamp\":" + System.currentTimeMillis() + "}",
                new HttpHeaders() {{ setContentType(MediaType.APPLICATION_JSON); }}
            );

            ResponseEntity<String> response = restTemplate.exchange(
                "http://spring-boot-batch:8081/batch/run",
                HttpMethod.POST,
                request,
                String.class
            );

            log.info("Batch sync triggered successfully: {}", response.getBody());
        } catch (Exception e) {
            log.warn("Batch sync trigger failed: {}", e.getMessage());
            // Don't throw exception - CRUD should succeed even if batch sync fails temporarily
        }
    }
}
