# Implementation Plan

**Project:** OpenSearch Employee Search Platform with Spring Boot, Spring Batch, LocalStack, and AWS SigV4  
**Version:** 1.0  
**Date:** 2026-08-16

---

## 1. Project Structure

```
OpenSearchClientsWithAWSSigV4-/
├── docs/
│   ├── aws-sigv4/
│   │   ├── README.md
│   │   ├── index.md
│   │   └── raw-content.md
│   ├── plan.md
│   └── requirements.md
├── spring-boot-rest/
│   ├── src/main/java/com/example/opensearch/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── model/
│   │   ├── repository/
│   │   ├── service/
│   │   └── security/
│   ├── src/main/resources/
│   │   ├── templates/
│   │   └── application.yml
│   ├── Dockerfile
│   └── pom.xml
├── spring-boot-batch/
│   ├── src/main/java/com/example/batch/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── model/
│   │   ├── processor/
│   │   ├── reader/
│   │   └── writer/
│   ├── src/main/resources/
│   │   ├── jobs/
│   │   └── application.yml
│   ├── Dockerfile
│   └── pom.xml
├── scripts/
│   ├── generate_employees.py
│   ├── populate_opensearch.sh
│   ├── aws_setup.py
│   └── schema.sql
├── opensearch/
│   ├── security/config/
│   └── ssl/
├── localstack/init/
├── docker-compose.yml
└── README.md
```

---

## 2. Implementation Phases

### Phase 1: Infrastructure Setup
**Objective:** Establish Docker Compose environment with all required services.

#### Tasks
1. Create Docker Compose configuration
   - PostgreSQL 16
   - OpenSearch 2.19 with security plugin
   - LocalStack (IAM, Secrets Manager, STS)
   - Spring Boot REST app
   - Spring Boot Batch app
   - Data generator service
   - AWS init service

2. Configure OpenSearch security
   - Enable security plugin
   - Configure IAM auth realm
   - Set up role mappings
   - Configure TLS/SSL settings

3. Set up LocalStack AWS services
   - IAM roles and policies for OpenSearch access
   - Secrets Manager for credential storage
   - Configure credential rotation

4. Create database schema
   - Employee table with indexes
   - Triggers for updated_at
   - Constraints and validations

#### Deliverables
- `docker-compose.yml`
- `scripts/schema.sql`
- `opensearch/security/config/`
- `localstack/init/`

---

### Phase 2: Spring Boot REST Application
**Objective:** Build the main REST API with employee CRUD operations and OpenSearch SigV4 integration.

#### Tasks
1. Project setup
   - Create Maven project structure
   - Configure dependencies (Spring Boot 3.3, OpenSearch Java client, AWS SDK v2)
   - Set up application.yml with AWS and OpenSearch configs

2. Data layer
   - Employee JPA entity
   - Employee JPA repository with custom queries
   - Database configuration

3. Security and AWS integration
   - SecretsManagerCredentialProvider with credential rotation support
   - AWS configuration beans
   - OpenSearch SigV4 transport configuration
   - Security filter chain

4. Service layer
   - EmployeeService with CRUD operations
   - OpenSearchService with search and indexing
   - BatchTriggerService for event-driven sync
   - Retry logic for resilience

5. Controller and UI
   - EmployeeController with REST endpoints
   - Thymeleaf templates (search, form, details)
   - Multi-page UI with Bootstrap
   - Pagination support

6. Docker configuration
   - Multi-stage Dockerfile
   - Environment variable configuration
   - Health checks

#### Deliverables
- Complete `spring-boot-rest/` application
- Docker image configuration
- UI templates

---

### Phase 3: Spring Boot Batch Application
**Objective:** Build batch processing service for PostgreSQL to OpenSearch synchronization.

#### Tasks
1. Project setup
   - Create Maven project structure
   - Configure Spring Batch dependencies
   - Set up application.yml

2. Batch configuration
   - Job and step configuration
   - Chunk-based processing
   - Retry and fault tolerance

3. Reader/Processor/Writer
   - JdbcPagingItemReader for PostgreSQL
   - EmployeeItemProcessor for transformations
   - OpenSearchBulkWriter for bulk indexing

4. OpenSearch integration
   - OpenSearchBatchService for bulk operations
   - SigV4 authentication
   - Error handling and retry logic

5. REST controller
   - BatchController for manual job triggering
   - Status endpoints

6. Docker configuration
   - Multi-stage Dockerfile
   - Scheduled execution configuration

#### Deliverables
- Complete `spring-boot-batch/` application
- Batch job configuration
- Docker image configuration

---

### Phase 4: Data Initialization and AWS Setup
**Objective:** Create scripts for populating OpenSearch with 10k records and configuring AWS services.

#### Tasks
1. Data generator script
   - Python script using Faker library
   - Generate 10,000 realistic employee records
   - Insert directly into PostgreSQL
   - Output CSV for OpenSearch population

2. OpenSearch population script
   - Bash script to read from PostgreSQL
   - Bulk index into OpenSearch
   - Configure index mappings and settings
   - Verify document count

3. AWS setup script
   - Create IAM role for OpenSearch access
   - Attach policies for employee index operations
   - Create Secrets Manager secret with credentials
   - Configure OpenSearch role mappings
   - Test credential rotation

#### Deliverables
- `scripts/generate_employees.py`
- `scripts/populate_opensearch.sh`
- `scripts/aws_setup.py`

---

### Phase 5: Testing and Validation
**Objective:** Ensure all components work together correctly.

#### Tasks
1. Unit tests
   - Spring Boot REST controllers and services
   - Batch processor logic
   - OpenSearch query builders
   - AWS credential provider

2. Integration tests
   - PostgreSQL connectivity
   - OpenSearch index operations
   - SigV4 request signing
   - Batch job execution

3. Docker Compose validation
   - Startup sequence verification
   - Data generation validation
   - Batch sync completion
   - SigV4 end-to-end testing

4. Credential rotation testing
   - Rotate secrets in Secrets Manager
   - Verify application continues without failure
   - Test cache invalidation
   - Verify graceful recovery

#### Deliverables
- Test suites
- Validation scripts
- Test documentation

---

### Phase 6: Documentation and Deployment
**Objective:** Create comprehensive documentation and finalize deployment.

#### Tasks
1. Documentation
   - README with setup instructions
   - Architecture diagrams
   - API documentation
   - Troubleshooting guide

2. Final configuration
   - Environment variable documentation
   - Docker Compose optimization
   - Health check configuration

#### Deliverables
- `README.md`
- `docs/requirements.md`
- `docs/plan.md`

---

## 3. Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Backend Framework** | Spring Boot | 3.3.4 |
| **Language** | Java | 21 |
| **ORM** | Spring Data JPA / Hibernate | - |
| **Database** | PostgreSQL | 16 |
| **Search Engine** | OpenSearch | 2.19.0 |
| **Batch Processing** | Spring Batch | - |
| **AWS SDK** | AWS SDK for Java v2 | 2.25.0 |
| **Container Orchestration** | Docker Compose | - |
| **Local AWS** | LocalStack | 3.5 |
| **Frontend** | Thymeleaf + Bootstrap | 5.3.2 |
| **Build Tool** | Maven | 3.9+ |
| **Authentication** | AWS SigV4 via AwsSdk2Transport | - |
| **Data Generation** | Python Faker | - |

---

## 4. Key Design Decisions

### 4.1 Credential Rotation Handling
- **SecretsManagerCredentialProvider** with caching and automatic refresh
- Cache invalidation on authentication errors
- Fallback to environment credentials if Secrets Manager fails
- Configurable refresh interval (default: 5 minutes)

### 4.2 Resilience Patterns
- **Spring Retry** for all external service calls
- Exponential backoff for retries
- Graceful degradation when batch sync fails
- Circuit breaker consideration for future enhancement

### 4.3 OpenSearch Integration
- Native Java client with `AwsSdk2Transport`
- SigV4 signing via AWS SDK v2
- Bulk operations for batch indexing
- Connection pooling via Apache HTTP client

### 4.4 Data Synchronization
- Event-driven sync via REST call from REST app to Batch app
- Periodic scheduled sync (every 5 minutes)
- Soft delete pattern for employee records
- Chunk-based batch processing (100 items per chunk)

---

## 5. Environment Variables

### Spring Boot REST App
| Variable | Description | Default |
|----------|-------------|---------|
| `AWS_REGION` | AWS region | us-east-1 |
| `AWS_ACCESS_KEY_ID` | AWS access key | test |
| `AWS_SECRET_ACCESS_KEY` | AWS secret key | test |
| `AWS_SESSION_TOKEN` | AWS session token | - |
| `OPENSEARCH_ENDPOINT` | OpenSearch endpoint | https://localhost:9200 |
| `LOCALSTACK_ENDPOINT` | LocalStack endpoint | http://localstack:4566 |
| `AWS_SECRETS_MANAGER_ENABLED` | Enable Secrets Manager | true |
| `AWS_SECRETS_MANAGER_SECRET_NAME` | Secret name | opensearch/credentials |
| `APP_BATCH_TRIGGER_ENABLED` | Enable batch trigger | true |
| `APP_BATCH_TRIGGER_URL` | Batch service URL | http://spring-boot-batch:8081/batch/run |

### Spring Boot Batch App
| Variable | Description | Default |
|----------|-------------|---------|
| `AWS_REGION` | AWS region | us-east-1 |
| `AWS_ACCESS_KEY_ID` | AWS access key | test |
| `AWS_SECRET_ACCESS_KEY` | AWS secret key | test |
| `OPENSEARCH_ENDPOINT` | OpenSearch endpoint | https://localhost:9200 |
| `BATCH_SCHEDULE_ENABLED` | Enable scheduled batch | true |
| `BATCH_SCHEDULE_CRON` | Cron expression | 0 */5 * * * * |

---

## 6. Docker Compose Services

| Service | Image | Ports | Purpose |
|---------|-------|-------|---------|
| `postgres` | postgres:16-alpine | 5432 | Primary database |
| `opensearch-node` | opensearchproject/opensearch:2.19.0 | 9200, 9600 | Search engine with SigV4 |
| `localstack` | localstack/localstack:3.5 | 4566 | AWS service emulation |
| `data-generator` | alpine:3.19 | - | Generate and load 10k records |
| `aws-init` | amazon/aws-cli:2.17 | - | Configure IAM and Secrets |
| `spring-boot-rest` | Custom build | 8080 | REST API and UI |
| `spring-boot-batch` | Custom build | 8081 | Batch sync service |

---

## 7. Verification Checklist

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

---

## 8. Future Enhancements

1. **Kubernetes Deployment:** Helm charts for K8s deployment
2. **Monitoring:** Prometheus + Grafana metrics
3. **Change Data Capture:** Debezium integration for real-time sync
4. **OpenSearch Serverless:** Support for Amazon OpenSearch Serverless
5. **Multi-Index Sync:** Support for multiple entity types
6. **GraphQL API:** GraphQL wrapper on top of REST endpoints
7. **Connection Pooling:** HikariCP for database, custom for OpenSearch
8. **Distributed Tracing:** OpenTelemetry integration

---

*End of Implementation Plan*
