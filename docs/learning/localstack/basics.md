# LocalStack Basics

**Project:** OpenSearch Employee Search Platform  
**Purpose:** Learn LocalStack fundamentals  
**Version:** 1.0  
**Date:** 2026-08-16

---

## What is LocalStack?

LocalStack is a fully functional local AWS cloud stack. It lets you run AWS services locally without connecting to the real AWS cloud.

### Key Concepts

1. **Services:** Emulated AWS services
2. **Endpoints:** Local URLs instead of AWS URLs
3. **Credentials:** Test credentials for local use
4. **Init Scripts:** Scripts that run on startup

---

## Why Use LocalStack?

- **No AWS Costs:** Free for development
- **Fast Development:** No network latency
- **Offline Work:** Work without internet
- **Easy Testing:** Test AWS integrations locally

---

## Starting LocalStack

### Using Docker Compose
```bash
docker-compose up localstack
```

### Using Docker
```bash
docker run -p 4566:4566 localstack/localstack
```

### Using CLI
```bash
localstack start
```

---

## Accessing Services

All services are available at `http://localhost:4566`

### Using AWS CLI
```bash
# Configure AWS CLI
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=us-east-1

# Or use awslocal (simpler)
awslocal iam list-roles
awslocal secretsmanager list-secrets
awslocal sts get-caller-identity
```

---

## Services Used in This Project

### IAM
- Create roles and policies
- Manage permissions

### Secrets Manager
- Store OpenSearch credentials
- Test credential rotation

### STS
- Get caller identity
- Assume roles

---

## Next Steps

1. Learn [IAM Emulation](iam.md)
2. Study [Secrets Manager](secrets-manager.md)
3. Practice [Testing](testing.md)

---

*Part of the OpenSearchClientsWithAWSSigV4 Learning Documentation*
