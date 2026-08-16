package com.example.batch.writer;

import com.example.batch.model.EmployeeSyncItem;
import com.example.batch.service.OpenSearchBatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OpenSearchBulkWriter implements ItemWriter<EmployeeSyncItem> {

    private static final Logger log = LoggerFactory.getLogger(OpenSearchBulkWriter.class);
    private final OpenSearchBatchService openSearchBatchService;

    @Autowired
    public OpenSearchBulkWriter(OpenSearchBatchService openSearchBatchService) {
        this.openSearchBatchService = openSearchBatchService;
    }

    @Override
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void write(List<? extends EmployeeSyncItem> items) throws Exception {
        if (items == null || items.isEmpty()) {
            return;
        }
        
        log.info("Writing {} items to OpenSearch", items.size());
        openSearchBatchService.bulkIndex(items);
    }
}
