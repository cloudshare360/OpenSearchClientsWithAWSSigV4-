package com.example.opensearch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

@Configuration
public class AwsConfig {

    @Value("${aws.region:us-east-1}")
    private String region;

    @Value("${aws.credentials.access-key:test}")
    private String accessKey;

    @Value("${aws.credentials.secret-key:test}")
    private String secretKey;

    @Value("${aws.credentials.session-token:}")
    private String sessionToken;

    @Value("${aws.secrets-manager.enabled:false}")
    private boolean secretsManagerEnabled;

    @Value("${aws.secrets-manager.secret-name:}")
    private String secretName;

    @Bean
    public Region awsRegion() {
        return Region.of(region);
    }

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        if (secretsManagerEnabled && secretName != null && !secretName.isEmpty()) {
            return new SecretsManagerCredentialProvider(secretName);
        }
        return DefaultCredentialsProvider.create();
    }

    @Bean
    public SecretsManagerClient secretsManagerClient(AwsCredentialsProvider credentialsProvider, Region awsRegion) {
        return SecretsManagerClient.builder()
                .region(awsRegion)
                .credentialsProvider(credentialsProvider)
                .build();
    }
}
