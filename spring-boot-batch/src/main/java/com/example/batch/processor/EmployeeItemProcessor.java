package com.example.batch.processor;

import com.example.batch.model.EmployeeSyncItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class EmployeeItemProcessor implements ItemProcessor<EmployeeSyncItem, EmployeeSyncItem> {

    private static final Logger log = LoggerFactory.getLogger(EmployeeItemProcessor.class);

    @Override
    public EmployeeSyncItem process(EmployeeSyncItem item) {
        log.debug("Processing employee: {} ({})", item.getId(), item.getOperation());
        // Add transformation logic here if needed
        // For now, pass through unchanged
        return item;
    }
}
