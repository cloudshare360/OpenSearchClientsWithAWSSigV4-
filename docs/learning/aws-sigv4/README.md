# AWS SigV4 Learning Guide

**Project:** OpenSearch Employee Search Platform  
**Purpose:** Learn AWS SigV4 for this project  
**Version:** 1.0  
**Date:** 2026-08-16

---

## What is AWS SigV4?

AWS Signature Version 4 (SigV4) is the AWS authentication protocol for signing API requests. It ensures that requests are authenticated and authorized to access AWS resources.

### Key Concepts

1. **Signing:** Adding authentication headers to requests
2. **Credentials:** Access key, secret key, and session token
3. **Region:** AWS region for the service
4. **Service:** AWS service identifier (e.g., `es` for OpenSearch)
5. **Headers:** Authorization, X-Amz-Date, X-Amz-Security-Token

---

## How SigV4 Works

### The Signing Process

```
1. Create Canonical Request
   - HTTP method
   - URI
   - Query string
   - Headers
   - Payload hash

2. Create String to Sign
   - Algorithm
   - Request date
   - Credential scope
   - Canonical request hash

3. Calculate Signature
   - Derive signing key
   - Calculate signature

4. Add Authentication Headers
   - Authorization
   - X-Amz-Date
   - X-Amz-Security-Token (if applicable)
```

### SigV4 Headers

```http
Authorization: AWS4-HMAC-SHA256 Credential=ACCESS_KEY/20260816/us-east-1/es/aws4_request, SignedHeaders=content-type;host;x-amz-date, Signature=signature
X-Amz-Date: 20260816T120000Z
X-Amz-Security-Token: session_token (if using temporary credentials)
```

---

## SigV4 in This Project

### 1. Credentials Source
Credentials come from AWS Secrets Manager (or environment variables):

```yaml
aws:
  secrets-manager:
    enabled: true
    secret-name: opensearch/credentials
```

### 2. Credential Provider
```java
@Component
public class SecretsManagerCredentialProvider implements AwsCredentialsProvider {
    private final AtomicReference<CachedCredentials> cachedCredentials;
    
    @Override
    public AwsCredentials resolveCredentials() {
        if (isExpired()) {
            refreshCredentials();
        }
        return cachedCredentials.get().getCredentials();
    }
}
```

### 3. OpenSearch Client Configuration
```java
@Configuration
public class OpenSearchConfig {
    @Bean
    public AwsSdk2Transport awsSdk2Transport(
            SdkHttpClient httpClient,
            Region awsRegion,
            AwsCredentialsProvider credentialsProvider) throws IOException {
        return new AwsSdk2Transport(
            httpClient,
            extractHostname(opensearchEndpoint),
            awsRegion,
            AwsSdk2TransportOptions.builder()
                .setMaxRetries(maxRetries)
                .build()
        );
    }
}
```

---

## Credential Rotation

### Why Rotation Matters
- Security best practice
- Limits exposure if credentials are compromised
- Required for compliance

### How It Works in This Project

1. **Credentials Stored in Secrets Manager**
   ```json
   {
     "accessKey": "test",
     "secretKey": "test",
     "sessionToken": null
   }
   ```

2. **Credential Provider Caches Credentials**
   ```java
   private final AtomicReference<CachedCredentials> cachedCredentials;
   private final long refreshIntervalMs = 300000; // 5 minutes
   ```

3. **Cache Invalidation on Auth Errors**
   ```java
   private boolean isAuthError(Exception e) {
       String message = e.getMessage();
       return message.contains("403") || 
              message.contains("Authentication") ||
              message.contains("SignatureDoesNotMatch");
   }
   
   public void invalidateCache() {
       cachedCredentials.set(null);
   }
   ```

4. **Automatic Refresh**
   ```java
   public AwsCredentials resolveCredentials() {
       if (isExpired()) {
           refreshCredentials();
       }
       return cachedCredentials.get().getCredentials();
   }
   ```

---

## IAM Roles and Policies

### IAM Role for OpenSearch
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "opensearch.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
```

### IAM Policy for OpenSearch Access
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::000000000000:role/opensearch-role"
      },
      "Action": [
        "es:ESHttpGet",
        "es:ESHttpPost",
        "es:ESHttpPut",
        "es:ESHttpDelete"
      ],
      "Resource": "arn:aws:es:us-east-1:000000000000:domain/opensearch-node/employees/*"
    }
  ]
}
```

---

## Common Issues and Solutions

### 1. Signature Mismatch
**Error:** `403 Forbidden` or `SignatureDoesNotMatch`

**Solutions:**
- Ensure system clock is synchronized
- Verify credentials are correct
- Check region matches OpenSearch domain
- Ensure service name is `es` (not `opensearch`)

### 2. SSL Certificate Issues
**Error:** `SSL: CERTIFICATE_VERIFY_FAILED`

**Solutions:**
- For local development, use HTTP instead of HTTPS
- Or disable certificate verification (not for production)
- Configure proper SSL certificates

### 3. Access Denied
**Error:** `AccessDeniedException`

**Solutions:**
- Verify IAM policy is attached
- Check OpenSearch domain access policy
- Ensure IAM role is mapped in OpenSearch
- Verify permissions are correct

---

## Testing SigV4 Locally

### Using LocalStack
```bash
# Start LocalStack
docker-compose up localstack

# Configure AWS CLI
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=us-east-1

# Test IAM
awslocal iam list-roles

# Test Secrets Manager
awslocal secretsmanager list-secrets
```

### Testing Credential Rotation
```bash
# 1. Create initial secret
awslocal secretsmanager create-secret \
  --name opensearch/credentials \
  --secret-string '{"accessKey":"test","secretKey":"test"}'

# 2. Update secret (simulate rotation)
awslocal secretsmanager update-secret \
  --name opensearch/credentials \
  --secret-string '{"accessKey":"new-key","secretKey":"new-secret"}'

# 3. Application should pick up new credentials on next refresh
```

---

## Best Practices

1. **Never Hardcode Credentials:** Use Secrets Manager or environment variables
2. **Use IAM Roles:** When running on AWS (EC2, ECS, Lambda)
3. **Rotate Regularly:** Implement automatic rotation
4. **Use Least Privilege:** Grant only necessary permissions
5. **Enable Logging:** Audit all authentication attempts
6. **Use HTTPS:** Always use secure connections

---

## Debugging SigV4

### Enable Debug Logging
```yaml
logging:
  level:
    org.opensearch.client: DEBUG
    software.amazon.awssdk: DEBUG
```

### Check Request Headers
```java
// Log outgoing requests
log.debug("OpenSearch request: {} {}", method, endpoint);
log.debug("Headers: {}", headers);
```

### Verify Credentials
```java
// Log credential source
log.debug("Using credentials from: {}", credentialSource);
log.debug("Access Key: {}", credentials.accessKeyId().substring(0, 4) + "...");
```

---

## Next Steps

1. Complete [SigV4 Basics](basics.md)
2. Learn [Java Client Integration](java-client.md)
3. Study [Credential Rotation](rotation.md)
4. Practice with LocalStack

---

*Part of the OpenSearchClientsWithAWSSigV4 Learning Documentation*
