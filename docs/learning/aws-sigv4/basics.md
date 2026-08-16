# AWS SigV4 Basics

**Project:** OpenSearch Employee Search Platform  
**Purpose:** Learn AWS SigV4 fundamentals  
**Version:** 1.0  
**Date:** 2026-08-16

---

## What is AWS SigV4?

AWS Signature Version 4 (SigV4) is the AWS authentication protocol. It signs API requests to verify identity and ensure data integrity.

### Key Concepts

1. **Signing:** Adding authentication to requests
2. **Credentials:** Access key + secret key + session token
3. **Region:** AWS region (e.g., us-east-1)
4. **Service:** AWS service name (e.g., es for OpenSearch)
5. **Headers:** Authorization, X-Amz-Date

---

## SigV4 Flow

```
1. Create Canonical Request
   - HTTP method, URI, query string, headers, payload hash

2. Create String to Sign
   - Algorithm, timestamp, credential scope, canonical request hash

3. Calculate Signature
   - Derive signing key, compute signature

4. Add Headers
   - Authorization header with signature
   - X-Amz-Date timestamp
   - X-Amz-Security-Token (if temporary credentials)
```

---

## SigV4 in This Project

### Credential Flow
```
AWS Secrets Manager
       │
       ▼ Retrieve Credentials
SecretsManagerCredentialProvider
       │
       ▼ Cache Credentials
AwsSdk2Transport
       │
       ▼ Sign Request
OpenSearch Client
       │
       ▼ Send Request
OpenSearch Server
```

### Credential Rotation
```
Secrets Manager Rotation
       │
       ▼ New Credentials Stored
       │
       ▼ Next Request
       │
       ▼ Detect Auth Error
       │
       ▼ Invalidate Cache
       │
       ▼ Fetch New Credentials
       │
       ▼ Sign with New Credentials
```

---

## Common Issues

### Signature Mismatch
- Check system clock synchronization
- Verify credentials are correct
- Ensure region matches

### Access Denied
- Verify IAM policy attached
- Check OpenSearch access policy
- Ensure IAM role is mapped

---

## Next Steps

1. Learn [Java Client Integration](java-client.md)
2. Study [Credential Rotation](rotation.md)
3. Practice with LocalStack

---

*Part of the OpenSearchClientsWithAWSSigV4 Learning Documentation*
