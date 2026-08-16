package com.example.opensearch.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class SecretsManagerCredentialProvider implements AwsCredentialsProvider {

    private static final Logger log = LoggerFactory.getLogger(SecretsManagerCredentialProvider.class);
    private static final long DEFAULT_REFRESH_INTERVAL_MS = 300000; // 5 minutes
    private static final String DEFAULT_SECRET_STRING = "{\"username\":\"admin\",\"password\":\"admin\"}";

    private final SecretsManagerClient secretsManagerClient;
    private final String secretName;
    private final long refreshIntervalMs;
    private final AtomicReference<CachedCredentials> cachedCredentials = new AtomicReference<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SecretsManagerCredentialProvider(@Value("${aws.secrets-manager.secret-name:}") String secretName,
                                            @Value("${aws.secrets-manager.refresh-interval:300000}") long refreshIntervalMs,
                                            SecretsManagerClient secretsManagerClient) {
        this.secretName = secretName;
        this.refreshIntervalMs = refreshIntervalMs > 0 ? refreshIntervalMs : DEFAULT_REFRESH_INTERVAL_MS;
        this.secretsManagerClient = secretsManagerClient;
        this.cachedCredentials.set(new CachedCredentials(createFallbackCredentials(), Instant.now()));
    }

    @Override
    public AwsCredentials resolveCredentials() {
        CachedCredentials current = cachedCredentials.get();
        
        // Check if credentials need refresh
        if (current == null || isExpired(current)) {
            log.info("Refreshing credentials from Secrets Manager for secret: {}", secretName);
            current = fetchCredentialsFromSecretsManager();
            cachedCredentials.set(current);
        }
        
        return current.getCredentials();
    }

    private boolean isExpired(CachedCredentials cached) {
        return Instant.now().isAfter(cached.timestamp.plusMillis(refreshIntervalMs));
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    private CachedCredentials fetchCredentialsFromSecretsManager() {
        try {
            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();

            GetSecretValueResponse response = secretsManagerClient.getSecretValue(request);
            String secretString = response.secretString();
            
            JsonNode root = objectMapper.readTree(secretString);
            String accessKey = root.has("accessKey") ? root.get("accessKey").asText() : "test";
            String secretKey = root.has("secretKey") ? root.get("secretKey").asText() : "test";
            String sessionToken = root.has("sessionToken") ? root.get("sessionToken").asText() : null;

            log.info("Successfully fetched credentials from Secrets Manager");
            return new CachedCredentials(
                    new RotatableAwsCredentials(accessKey, secretKey, sessionToken),
                    Instant.now()
            );
        } catch (Exception e) {
            log.warn("Failed to fetch credentials from Secrets Manager, using cached/fallback credentials: {}", e.getMessage());
            // Return cached credentials if refresh fails
            CachedCredentials current = cachedCredentials.get();
            if (current != null && !isExpired(current)) {
                return current;
            }
            return new CachedCredentials(createFallbackCredentials(), Instant.now());
        }
    }

    private AwsCredentials createFallbackCredentials() {
        return new RotatableAwsCredentials(accessKey, secretKey, sessionToken);
    }

    public void invalidateCache() {
        log.info("Invalidating credential cache");
        cachedCredentials.set(null);
    }

    private static class CachedCredentials {
        private final AwsCredentials credentials;
        private final Instant timestamp;

        CachedCredentials(AwsCredentials credentials, Instant timestamp) {
            this.credentials = credentials;
            this.timestamp = timestamp;
        }

        AwsCredentials getCredentials() {
            return credentials;
        }
    }

    private static class RotatableAwsCredentials implements AwsCredentials {
        private volatile String accessKeyId;
        private volatile String secretAccessKey;
        private volatile String sessionToken;

        RotatableAwsCredentials(String accessKeyId, String secretAccessKey, String sessionToken) {
            this.accessKeyId = accessKeyId;
            this.secretAccessKey = secretAccessKey;
            this.sessionToken = sessionToken;
        }

        @Override
        public String accessKeyId() {
            return accessKeyId;
        }

        @Override
        public String secretAccessKey() {
            return secretAccessKey;
        }

        public String sessionToken() {
            return sessionToken;
        }

        public void rotate(String accessKeyId, String secretAccessKey, String sessionToken) {
            this.accessKeyId = accessKeyId;
            this.secretAccessKey = secretAccessKey;
            this.sessionToken = sessionToken;
        }
    }
}
