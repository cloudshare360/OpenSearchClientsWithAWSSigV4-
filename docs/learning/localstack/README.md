# LocalStack Learning Guide

**Project:** OpenSearch Employee Search Platform  
**Purpose:** Learn LocalStack for this project  
**Version:** 1.0  
**Date:** 2026-08-16

---

## What is LocalStack?

LocalStack is a fully functional local AWS cloud stack that allows you to develop and test AWS applications without connecting to the real AWS cloud. It provides a mock environment for AWS services.

### Key Concepts for This Project

1. **Services:** Emulated AWS services (IAM, Secrets Manager, STS)
2. **Endpoints:** Local URLs instead of AWS endpoints
3. **Credentials:** Test credentials for local development
4. **Init Scripts:** Scripts that run on startup
5. **Data Persistence:** LocalStack data stored in volumes

---

## Why LocalStack for This Project?

1. **Cost:** No AWS charges for development
2. **Speed:** Fast local development without network latency
3. **Offline:** Work without internet connection
4. **Testing:** Test AWS integrations locally
5. **CI/CD:** Run tests in CI pipelines

---

## Services Used in This Project

### 1. IAM (Identity and Access Management)
- Create IAM roles
- Attach policies to roles
- Manage access control

### 2. Secrets Manager
- Store secrets (OpenSearch credentials)
- Retrieve secrets at runtime
- Test credential rotation

### 3. STS (Security Token Service)
- Get caller identity
- Assume roles
- Get temporary credentials

---

## Basic Usage

### Starting LocalStack

```bash
# Using Docker Compose
docker-compose up localstack

# Using Docker directly
docker run -p 4566:4566 -p 4571:4571 localstack/localstack

# Using LocalStack CLI
localstack start
```

### Accessing LocalStack

All services are available at:
```
http://localhost:4566
```

Individual service endpoints:
- IAM: http://localhost:4566
- Secrets Manager: http://localhost:4566
- STS: http://localhost:4566

---

## AWS CLI with LocalStack

### Configuration
```bash
# Configure AWS CLI for LocalStack
aws configure
# AWS Access Key ID: test
# AWS Secret Access Key: test
# Region: us-east-1

# Or use environment variables
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=us-east-1
export AWS_ENDPOINT_URL=http://localhost:4566
```

### Common Commands

```bash
# List secrets
awslocal secretsmanager list-secrets

# Get secret value
awslocal secretsmanager get-secret-value --secret-id opensearch/credentials

# Create IAM role
awslocal iam create-role --role-name opensearch-role --assume-role-policy-document file://trust-policy.json

# List IAM roles
awslocal iam list-roles
```

---

## Project-Specific Configuration

### Docker Compose Service
```yaml
localstack:
  image: localstack/localstack:3.5
  container_name: localstack
  environment:
    - SERVICES=iam,secretsmanager,sts
    - DEBUG=1
    - DATA_DIR=/tmp/localstack/data
    - DOCKER_HOST=unix:///var/run/docker.sock
  ports:
    - "4566:4566"
  volumes:
    - localstack_data:/tmp/localstack
    - /var/run/docker.sock:/var/run/docker.sock
    - ./localstack/init:/docker-entrypoint-initaws.d
```

### Python AWS Setup Script
```python
import boto3
import os

# Configure boto3 for LocalStack
session = boto3.Session(
    aws_access_key_id='test',
    aws_secret_access_key='test',
    region_name='us-east-1'
)

# Create clients
iam_client = session.client('iam', endpoint_url='http://localhost:4566')
secrets_client = session.client('secretsmanager', endpoint_url='http://localhost:4566')

# Create IAM role
iam_client.create_role(
    RoleName='opensearch-role',
    AssumeRolePolicyDocument=json.dumps(trust_policy)
)

# Create secret
secrets_client.create_secret(
    Name='opensearch/credentials',
    SecretString=json.dumps({
        'accessKey': 'test',
        'secretKey': 'test'
    })
)
```

---

## Common Operations

### IAM Operations
```bash
# Create role
awslocal iam create-role \
  --role-name opensearch-role \
  --assume-role-policy-document '{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":{"Service":"opensearch.amazonaws.com"},"Action":"sts:AssumeRole"}]}'

# Attach policy
awslocal iam attach-role-policy \
  --role-name opensearch-role \
  --policy-arn arn:aws:iam::000000000000:policy/opensearch-access

# List roles
awslocal iam list-roles
```

### Secrets Manager Operations
```bash
# Create secret
awslocal secretsmanager create-secret \
  --name opensearch/credentials \
  --secret-string '{"accessKey":"test","secretKey":"test"}'

# Get secret
awslocal secretsmanager get-secret-value --secret-id opensearch/credentials

# Update secret
awslocal secretsmanager update-secret \
  --secret-id opensearch/credentials \
  --secret-string '{"accessKey":"new-key","secretKey":"new-secret"}'
```

### STS Operations
```bash
# Get caller identity
awslocal sts get-caller-identity
```

---

## Best Practices

1. **Use Consistent Credentials:** `test` / `test` for local development
2. **Use Init Scripts:** Automate setup with scripts in `/docker-entrypoint-initaws.d`
3. **Persist Data:** Use volumes to keep LocalStack data between restarts
4. **Use awslocal CLI:** Simpler than configuring AWS CLI
5. **Clean Up:** Remove unused resources with `awslocal` commands
6. **Use Environment Variables:** Configure endpoints and credentials

---

## Troubleshooting

### LocalStack Won't Start
```bash
# Check logs
docker logs localstack

# Check if ports are available
lsof -i :4566
```

### Services Not Available
```bash
# Check service status
awslocal --help

# Verify SERVICES environment variable
docker-compose exec localstack env | grep SERVICES
```

### Data Not Persisting
```bash
# Check volumes
docker volume ls | grep localstack

# Verify volume mounts
docker-compose exec localstack ls -la /tmp/localstack
```

---

## Differences from Real AWS

| Feature | LocalStack | Real AWS |
|---------|-------------|----------|
| Cost | Free | Pay per use |
| Latency | Minimal | Network latency |
| Availability | Always | Depends on region |
| Limits | Generous | Service quotas |
| SigV4 | Supported | Supported |
| IAM | Emulated | Real |

---

## Next Steps

1. Complete [LocalStack Basics](basics.md)
2. Learn [IAM Emulation](iam.md)
3. Study [Secrets Manager](secrets-manager.md)
4. Practice [Testing](testing.md)

---

*Part of the OpenSearchClientsWithAWSSigV4 Learning Documentation*
