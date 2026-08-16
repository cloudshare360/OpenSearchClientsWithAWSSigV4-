# OpenSearch Employee Search Platform

A full-stack application demonstrating AWS SigV4-signed OpenSearch client connectivity, fine-grained access control, and real-time index synchronization using Spring Boot, Spring Batch, Angular, OpenSearch, and LocalStack.

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Docker Compose                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────┐    ┌──────────────┐    ┌───────────────────┐  │
│  │   Spring    │    │  Spring Boot │    │                   │  │
│  │   Boot REST │    │    Batch     │    │   LocalStack      │  │
│  │    App      │    │   Service    │    │  (AWS Services)   │  │
│  │  :8080      │    │              │    │                   │  │
│  └──────┬──────┘    └──────┬───────┘    │  ┌─────────────┐ │  │
│         │                  │            │  │    IAM      │ │  │
│         │                  │            │  │  (Roles +   │ │  │
│         │                  │            │  │  Policies)  │ │  │
│         │                  │            │  ├─────────────┤ │  │
│         │                  │            │  │   Secrets   │ │  │
│         │                  │            │  │  Manager    │ │  │
│         └────────┬─────────┘            │  ├─────────────┤ │  │
│                  │                      │  │  OpenSearch │ │  │
│                  │                      │  │  (SigV4)    │ │  │
│                  │    Batch Job          │  └──────┬──────┘ │  │
│                  │ (Periodic Sync) ──────┤─────────┤        │  │
│                  │                      │         │        │  │
│  ┌───────────────┴──────────────────────┴─────────▼───────┐  │
│  │                    PostgreSQL (Primary Data Store)      │  │
│  └─────────────────────────────────────────────────────────┘  │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │              Angular UI (:4200)                          │  │
│  │  Search | Advanced Search | CRUD | Full Text Search     │  │
│  └─────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

## Features

- **Full-Text Search**: Search employees by name, email, position, department
- **Advanced Search**: Filter by department, salary range, hire date
- **CRUD Operations**: Create, read, update, delete employee records
- **Real-Time Sync**: Spring Batch continuously syncs PostgreSQL to OpenSearch
- **AWS SigV4**: Native OpenSearch client authentication with SigV4 signing
- **Secrets Manager**: Automatic credential rotation without downtime
- **Multi-Page UI**: Angular frontend with responsive Bootstrap design

## Prerequisites

- Docker and Docker Compose
- Java 21 (for local development)
- Node.js 20+ (for Angular development)
- Python 3.9+ (for data generation scripts)

## Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd OpenSearchClientsWithAWSSigV4-
```

### 2. Start All Services

```bash
docker-compose up --build
```

This will start:
- PostgreSQL on port 5432
- OpenSearch on ports 9200, 9600
- LocalStack on port 4566
- Spring Boot REST API on port 8080
- Spring Boot Batch on port 8081
- Angular UI on port 4200

### 3. Verify Services

```bash
# Check PostgreSQL
docker exec -it employee-postgres psql -U admin -d employeedb -c "SELECT COUNT(*) FROM employees;"

# Check OpenSearch
curl http://localhost:9200/employees/_search?q=*&pretty=true

# Check REST API
curl http://localhost:8080/api/employees

# Check Angular UI
open http://localhost:4200
```

## Project Structure

```
├── docker-compose.yml          # Infrastructure services
├── spring-boot-rest/           # REST API microservice
├── spring-boot-batch/          # Batch sync microservice
├── angular-ui/                 # Angular frontend
├── scripts/                    # Data generation and AWS setup
├── opensearch/                 # OpenSearch security configs
├── localstack/init/            # LocalStack initialization
├── docs/                       # Documentation
│   ├── README.md               # Documentation index
│   ├── aws-sigv4/             # AWS SigV4 documentation
│   ├── learning/              # Technology learning guides
│   └── sdlc/                  # Software development lifecycle
│       ├── requirements/     # Requirements documents
│       ├── analysis/         # Analysis documents
│       ├── design/           # Design documents
│       ├── plan.md           # Implementation plan
│       └── TASK_TRACKER.md   # Project status tracker
├── README.md                   # This file
```

## Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Backend Framework** | Spring Boot | 3.3.4 |
| **Language** | Java | 21 |
| **Frontend** | Angular | 17+ |
| **UI Framework** | Bootstrap | 5.3 |
| **ORM** | Spring Data JPA / Hibernate | - |
| **Database** | PostgreSQL | 16 |
| **Search Engine** | OpenSearch | 2.19.0 |
| **Batch Processing** | Spring Batch | - |
| **AWS SDK** | AWS SDK for Java v2 | 2.25.0 |
| **Container Orchestration** | Docker Compose | - |
| **Local AWS** | LocalStack | 3.5 |
| **Authentication** | AWS SigV4 via AwsSdk2Transport | - |

## API Endpoints

### Employee REST API (`http://localhost:8080/api/employees`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/employees` | List employees with pagination |
| GET | `/api/employees/{id}` | Get employee by ID |
| POST | `/api/employees` | Create new employee |
| PUT | `/api/employees/{id}` | Update employee |
| DELETE | `/api/employees/{id}/delete` | Delete employee |
| GET | `/api/employees/search?q={query}` | Full-text search |
| GET | `/api/employees/stats` | Get employee statistics |

### Batch API (`http://localhost:8081/batch`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/batch/run` | Trigger batch sync job |
| GET | `/batch/status` | Get batch service status |

## Environment Variables

### Spring Boot REST App

| Variable | Description | Default |
|----------|-------------|---------|
| `AWS_REGION` | AWS region | us-east-1 |
| `AWS_ACCESS_KEY_ID` | AWS access key | test |
| `AWS_SECRET_ACCESS_KEY` | AWS secret key | test |
| `OPENSEARCH_ENDPOINT` | OpenSearch endpoint | http://opensearch-node:9200 |
| `AWS_SECRETS_MANAGER_ENABLED` | Enable Secrets Manager | true |
| `AWS_SECRETS_MANAGER_SECRET_NAME` | Secret name | opensearch/credentials |
| `APP_BATCH_TRIGGER_ENABLED` | Enable batch trigger | true |

### Spring Boot Batch App

| Variable | Description | Default |
|----------|-------------|---------|
| `AWS_REGION` | AWS region | us-east-1 |
| `AWS_ACCESS_KEY_ID` | AWS access key | test |
| `AWS_SECRET_ACCESS_KEY` | AWS secret key | test |
| `OPENSEARCH_ENDPOINT` | OpenSearch endpoint | http://opensearch-node:9200 |
| `BATCH_SCHEDULE_ENABLED` | Enable scheduled batch | true |
| `BATCH_SCHEDULE_CRON` | Cron expression | 0 */5 * * * * |

## Data Model

### Employee Entity

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Primary key |
| firstName | String | Employee first name |
| lastName | String | Employee last name |
| email | String | Unique email address |
| department | String | Department name |
| position | String | Job position |
| salary | BigDecimal | Salary amount |
| hireDate | LocalDate | Date of hire |
| isActive | Boolean | Employment status |
| createdAt | LocalDateTime | Record creation timestamp |
| updatedAt | LocalDateTime | Record update timestamp |

### OpenSearch Index Mapping

```json
{
  "mappings": {
    "properties": {
      "id": { "type": "long" },
      "firstName": { "type": "text", "analyzer": "standard" },
      "lastName": { "type": "text", "analyzer": "standard" },
      "email": { "type": "keyword" },
      "department": { "type": "keyword" },
      "position": { "type": "text", "analyzer": "standard" },
      "salary": { "type": "double" },
      "hireDate": { "type": "date" },
      "fullText": { "type": "text", "analyzer": "standard" }
    }
  }
}
```

## AWS SigV4 Configuration

The application uses AWS Signature V4 for authenticating with OpenSearch:

1. **Credentials**: Retrieved from AWS Secrets Manager (or environment variables)
2. **Signing**: Requests are signed using AWS SDK v2 `AwsSdk2Transport`
3. **Rotation**: Credentials are automatically refreshed when rotated in Secrets Manager
4. **Fallback**: If Secrets Manager fails, falls back to environment credentials

### IAM Roles

| Role | Purpose |
|------|---------|
| `opensearch-role` | IAM role for OpenSearch access |
| `admin` | Full access to OpenSearch |
| `employee-reader` | Read-only access to employees index |
| `employee-writer` | Write access to employees index |

## Testing

### Run All Tests

```bash
# Start services
docker-compose up -d postgres opensearch-node localstack

# Run Spring Boot REST tests
cd spring-boot-rest && ./mvnw test

# Run Spring Boot Batch tests
cd spring-boot-batch && ./mvnw test

# Run Angular tests
cd angular-ui && npm test
```

### Manual Testing

1. **Search Test**: Open http://localhost:4200 and search for employees
2. **CRUD Test**: Create, edit, and delete employee records
3. **Batch Sync Test**: Verify OpenSearch index updates after CRUD operations
4. **Credential Rotation Test**: Update secret in LocalStack and verify no downtime

## Verification Checklist

- [ ] All Docker Compose services start successfully
- [ ] PostgreSQL contains 10,000 employee records
- [ ] OpenSearch index `employees` has 10,000 documents
- [ ] IAM role and policies created in LocalStack
- [ ] Secrets Manager contains OpenSearch credentials
- [ ] Spring Boot REST app connects to PostgreSQL
- [ ] Spring Boot REST app can search OpenSearch with SigV4
- [ ] CRUD operations via REST app update PostgreSQL
- [ ] Spring Boot Batch app syncs PostgreSQL changes to OpenSearch
- [ ] Multi-page UI displays search results with pagination
- [ ] All services communicate via Docker network
- [ ] Application logs show SigV4-signed requests to OpenSearch
- [ ] Credential rotation in Secrets Manager doesn't cause failures
- [ ] Batch sync runs successfully on schedule
- [ ] Event-driven batch sync triggers on CRUD operations

## Troubleshooting

### OpenSearch Connection Issues

```bash
# Check OpenSearch health
curl http://localhost:9200/_cluster/health

# Check OpenSearch logs
docker logs opensearch-node
```

### PostgreSQL Connection Issues

```bash
# Check PostgreSQL health
docker exec -it employee-postgres pg_isready -U admin -d employeedb

# Check PostgreSQL logs
docker logs employee-postgres
```

### LocalStack Issues

```bash
# Check LocalStack health
curl http://localhost:4566/health

# List secrets
awslocal secretsmanager list-secrets

# Get secret
awslocal secretsmanager get-secret-value --secret-id opensearch/credentials
```

### Angular UI Issues

```bash
# Check Angular container logs
docker logs angular-ui

# Rebuild Angular
cd angular-ui && npm run build
```

## License

MIT

## References

- [AWS SigV4 Support for OpenSearch Clients](https://opensearch.org/blog/aws-sigv4-support-for-clients/)
- [OpenSearch Documentation](https://opensearch.org/docs/)
- [Spring Batch Documentation](https://docs.spring.io/spring-batch/)
- [LocalStack Documentation](https://docs.localstack.cloud/)
- [Angular Documentation](https://angular.io/docs)
