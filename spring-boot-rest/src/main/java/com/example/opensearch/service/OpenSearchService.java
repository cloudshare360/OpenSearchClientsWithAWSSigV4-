package com.example.opensearch.service;

import com.example.opensearch.config.SecretsManagerCredentialProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.InfoResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.transport.aws.AwsSdk2Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenSearchService {

    private static final Logger log = LoggerFactory.getLogger(OpenSearchService.class);
    private final OpenSearchClient openSearchClient;
    private final AwsSdk2Transport awsSdk2Transport;
    private final SecretsManagerCredentialProvider credentialProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String indexName;

    public OpenSearchService(@Qualifier("awsSdk2Transport") AwsSdk2Transport awsSdk2Transport,
                             SecretsManagerCredentialProvider credentialProvider,
                             @Value("${aws.opensearch.index:employees}") String indexName) throws IOException {
        this.awsSdk2Transport = awsSdk2Transport;
        this.credentialProvider = credentialProvider;
        this.indexName = indexName;
        this.openSearchClient = new OpenSearchClient(awsSdk2Transport);
        log.info("OpenSearch client initialized with index: {}", indexName);
    }

    @Retryable(value = {IOException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public Map<String, Object> search(String query, int page, int size) {
        try {
            int from = page * size;
            SearchRequest request = SearchRequest.of(s -> s
                    .index(indexName)
                    .from(from)
                    .size(size)
                    .query(q -> q
                            .multiMatch(m -> m
                                    .query(query)
                                    .fields("firstName", "lastName", "email", "position", "department", "fullText")
                            )
                    )
            );

            SearchResponse<Map> response = openSearchClient.search(request, Map.class);
            List<Map<String, Object>> hits = response.hits().hits().stream()
                    .map(Hit::source)
                    .toList();

            Map<String, Object> result = new HashMap<>();
            result.put("hits", hits);
            result.put("total", response.hits().total().value());
            result.put("took", response.took());
            return result;
        } catch (Exception e) {
            log.error("OpenSearch search failed", e);
            if (isAuthError(e)) {
                log.warn("Authentication error, invalidating credential cache");
                credentialProvider.invalidateCache();
            }
            throw new RuntimeException("Search failed: " + e.getMessage(), e);
        }
    }

    @Retryable(value = {IOException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public Map<String, Object> indexDocument(String id, Map<String, Object> document) {
        try {
            var request = new org.opensearch.client.opensearch.core.IndexRequest.Builder<Map<String, Object>>()
                    .index(indexName)
                    .id(id)
                    .document(document)
                    .build();

            var response = openSearchClient.index(request);
            Map<String, Object> result = new HashMap<>();
            result.put("id", response.id());
            result.put("result", response.result());
            result.put("version", response.version());
            return result;
        } catch (Exception e) {
            log.error("OpenSearch index operation failed", e);
            if (isAuthError(e)) {
                credentialProvider.invalidateCache();
            }
            throw new RuntimeException("Index operation failed: " + e.getMessage(), e);
        }
    }

    @Retryable(value = {IOException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public boolean deleteDocument(String id) {
        try {
            var request = new org.opensearch.client.opensearch.core.DeleteRequest.Builder()
                    .index(indexName)
                    .id(id)
                    .build();

            var response = openSearchClient.delete(request);
            return "deleted".equals(response.result().toString());
        } catch (Exception e) {
            log.error("OpenSearch delete operation failed", e);
            if (isAuthError(e)) {
                credentialProvider.invalidateCache();
            }
            throw new RuntimeException("Delete operation failed: " + e.getMessage(), e);
        }
    }

    @Retryable(value = {IOException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public Map<String, Object> getInfo() throws IOException {
        InfoResponse info = openSearchClient.info();
        Map<String, Object> result = new HashMap<>();
        result.put("distribution", info.version().distribution());
        result.put("number", info.version().number());
        result.put("buildType", info.version().buildType());
        result.put("clusterName", info.clusterName());
        return result;
    }

    public void refreshCredentials() {
        log.info("Refreshing OpenSearch credentials due to rotation");
        credentialProvider.invalidateCache();
        // Credentials will be refreshed on next request
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
