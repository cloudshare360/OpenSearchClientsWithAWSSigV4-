package com.example.opensearch.config;

import org.opensearch.client.transport.aws.AwsSdk2Transport;
import org.opensearch.client.transport.aws.AwsSdk2TransportOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.opensearchserverless.OpenSearchServerlessClient;

import java.io.IOException;

@Configuration
public class OpenSearchConfig {

    private static final Logger log = LoggerFactory.getLogger(OpenSearchConfig.class);

    @Value("${aws.opensearch.endpoint:https://localhost:9200}")
    private String opensearchEndpoint;

    @Value("${aws.opensearch.index:employees}")
    private String opensearchIndex;

    @Value("${aws.opensearch.connection-timeout:30000}")
    private int connectionTimeout;

    @Value("${aws.opensearch.socket-timeout:60000}")
    private int socketTimeout;

    @Value("${aws.opensearch.max-retries:3}")
    private int maxRetries;

    @Value("${aws.opensearch.retry-backoff:1000}")
    private long retryBackoff;

    @Qualifier("awsCredentialsProvider")
    private final AwsCredentialsProvider awsCredentialsProvider;

    public OpenSearchConfig(@Qualifier("awsCredentialsProvider") AwsCredentialsProvider awsCredentialsProvider) {
        this.awsCredentialsProvider = awsCredentialsProvider;
    }

    @Bean(destroyMethod = "close")
    public SdkHttpClient sdkHttpClient() {
        return ApacheHttpClient.builder()
                .connectionTimeout(java.time.Duration.ofMillis(connectionTimeout))
                .socketTimeout(java.time.Duration.ofMillis(socketTimeout))
                .build();
    }

    @Bean(destroyMethod = "close")
    public AwsSdk2Transport awsSdk2Transport(SdkHttpClient sdkHttpClient, Region awsRegion) throws IOException {
        try {
            return new AwsSdk2Transport(
                    sdkHttpClient,
                    extractHostname(opensearchEndpoint),
                    awsRegion,
                    AwsSdk2TransportOptions.builder()
                            .setMaxRetries(maxRetries)
                            .setRetryStrategy(new RotatableRetryStrategy(retryBackoff))
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to create AwsSdk2Transport", e);
            throw new IOException("Failed to initialize OpenSearch transport", e);
        }
    }

    @Bean(destroyMethod = "close")
    public OpenSearchServerlessClient openSearchServerlessClient(Region awsRegion) {
        return OpenSearchServerlessClient.builder()
                .region(awsRegion)
                .credentialsProvider(awsCredentialsProvider)
                .build();
    }

    private String extractHostname(String endpoint) {
        if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
            return endpoint.substring(endpoint.indexOf("://") + 3);
        }
        return endpoint;
    }

    private static class RotatableRetryStrategy implements software.amazon.awssdk.core.retry.RetryPolicyContextRetryStrategy {
        private final long backoffMs;

        RotatableRetryStrategy(long backoffMs) {
            this.backoffMs = backoffMs;
        }

        @Override
        public long retryDelayInMillis(software.amazon.awssdk.core.retry.RetryPolicyContext context) {
            // Exponential backoff for retries, but handle auth errors specially
            if (context.exception() != null && 
                context.exception().getMessage() != null &&
                context.exception().getMessage().contains("403") || 
                context.exception().getMessage().contains("SignatureDoesNotMatch")) {
                log.warn("Authentication error detected, may need credential refresh: {}", context.exception().getMessage());
                return backoffMs * 2;
            }
            return backoffMs;
        }
    }
}
