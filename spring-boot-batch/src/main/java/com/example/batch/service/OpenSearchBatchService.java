package com.example.batch.service;

import com.example.batch.model.EmployeeSyncItem;
import com.example.batch.config.SecretsManagerCredentialProvider;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.transport.aws.AwsSdk2Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenSearchBatchService {

    private static final Logger log = LoggerFactory.getLogger(OpenSearchBatchService.class);
    private final OpenSearchClient openSearchClient;
    private final AwsSdk2Transport awsSdk2Transport;
    private final SecretsManagerCredentialProvider credentialProvider;
    private final String indexName;

    public OpenSearchBatchService(@Qualifier("awsSdk2Transport") AwsSdk2Transport awsSdk2Transport,
                                  SecretsManagerCredentialProvider credentialProvider,
                                  @Value("${aws.opensearch.index:employees}") String indexName) throws IOException {
        this.awsSdk2Transport = awsSdk2Transport;
        this.credentialProvider = credentialProvider;
        this.indexName = indexName;
        this.openSearchClient = new OpenSearchClient(awsSdk2Transport);
        log.info("OpenSearch batch client initialized with index: {}", indexName);
    }

    public void bulkIndex(List<EmployeeSyncItem> items) throws IOException {
        try {
            BulkRequest.Builder bulkRequest = new BulkRequest.Builder();
            
            for (EmployeeSyncItem item : items) {
                if ("DELETE".equals(item.getOperation())) {
                    bulkRequest.operations(op -> op
                            .delete(d -> d.index(indexName).id(String.valueOf(item.getId()))
                    );
                } else {
                    Map<String, Object> document = new HashMap<>();
                    document.put("id", item.getId());
                    document.put("firstName", item.getFirstName());
                    document.put("lastName", item.getLastName());
                    document.put("email", item.getEmail());
                    document.put("department", item.getDepartment());
                    document.put("position", item.getPosition());
                    document.put("salary", item.getSalary());
                    document.put("hireDate", item.getHireDate().toString());
                    document.put("fullText", item.getFirstName() + " " + item.getLastName() + " " + item.getEmail() + " " + item.getPosition());
                    
                    bulkRequest.operations(op -> op
                            .index(i -> i.index(indexName).id(String.valueOf(item.getId())).document(document))
                    );
                }
            }

            BulkResponse response = openSearchClient.bulk(bulkRequest.build());
            
            if (response.errors()) {
                log.error("Bulk indexing had errors");
                response.items().forEach(item -> {
                    if (item.error() != null) {
                        log.error("Error indexing item: {}", item.error().reason());
                    }
                });
            }
            
            log.info("Bulk indexed {} items, took {} ms", items.size(), response.took());
        } catch (Exception e) {
            log.error("Bulk index operation failed", e);
            if (isAuthError(e)) {
                log.warn("Authentication error during bulk index, invalidating credentials");
                credentialProvider.invalidateCache();
            }
            throw new IOException("Bulk index failed: " + e.getMessage(), e);
        }
    }

    private boolean isAuthError(Exception e) {
        String message = e.getMessage();
        if (message == null) return false;
        return message.contains("403") || 
               message.contains("Authentication") || 
               message.contains("Unauthorized") ||
               message.contains("SignatureDoesNotMatch");
    }
}
