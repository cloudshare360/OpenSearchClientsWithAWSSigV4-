# OpenSearch Employee Search Platform - Requirements Document

**Project:** Full-Stack Employee Search Application with OpenSearch, Spring Boot, Spring Batch, LocalStack, and AWS SigV4  
**Version:** 1.0  
**Date:** 2026-08-16  
**Author:** System Requirements Gathering  

---

## 1. Executive Summary

Build a full-stack application that demonstrates AWS SigV4-signed OpenSearch client connectivity, fine-grained access control, and real-time index synchronization using Spring Boot, Spring Batch, OpenSearch, LocalStack, and Docker Compose.

---

## 2. Architecture Overview

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
│                  │                      │  └──────┬──────┘ │  │
│                  │    Batch Job          │         │        │  │
│                  │ (Periodic Sync) ──────┤─────────┤        │  │
│                  │                      │         │        │  │
│  ┌───────────────┴──────────────────────┴─────────▼───────┐  │
│  │                    PostgreSQL (H2 / R2DBC)              │  │
│  │                    Primary Data Store                    │  │
│  └─────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. Functional Requirements

### 3.1 OpenSearch Database
- **Container:** OpenSearch 2.x running via Docker Compose
- **Security Plugin:** Enabled with IAM authentication realm
- **Role Mappings:** Backend role mapping for IAM-based access
- **Index:** Pre-configured `employees` index with full-text search mapping
- **Data:** 10,000 employee records indexed on startup
- **Transport:** SSL/TLS with SigV4 request signing

### 3.2 LocalStack AWS Services
- **IAM Service:**
  - Create an IAM Role for OpenSearch access
  - Attach policy granting `es:ESHttpGet`, `es:ESHttpPost`, `es:ESHttpPut`, `es:ESHttpDelete` on the `employees` index
- **Secrets Manager:**
  - Store OpenSearch endpoint, IAM role ARN, and credentials
  - Allow Spring Boot apps to retrieve secrets at runtime
- **OpenSearch Service (Mocked):**
  - Emulate AWS OpenSearch service for SigV4 signing context

### 3.3 Spring Boot REST Application
- **Framework:** Spring Boot 3.x with Spring Web
- **UI:** Multi-page Thymeleaf frontend
  - **Page 1: Search/List** - Full-text search, filters, pagination
  - **Page 2: Create/Edit Employee** - Form for CRUD operations
  - **Page 3: View Details** - Single employee view
- **Data Source:** PostgreSQL for primary employee records
- **Search Client:** OpenSearch Java client with `AwsSdk2Transport` and SigV4 signing
- **Operations:**
  - Full-text search across employee fields (name, email, department, position)
  - Create, Read, Update, Delete employee records
  - Real-time index updates via application events

### 3.4 Spring Boot Batch Application
- **Framework:** Spring Boot 3.x with Spring Batch
- **Purpose:** Synchronize PostgreSQL relational data to OpenSearch index
- **Job Configuration:**
  - Periodic batch job (configurable cron/interval)
  - Reader: JdbcCursorItemReader reading from PostgreSQL
  - Processor: Optional transformation/filtering
  - Writer: OpenSearch bulk indexer using SigV4
- **Trigger Mechanisms:**
  - Scheduled execution
  - Event-driven reindex after CRUD operations
- **Monitoring:** Job execution history and metrics

### 3.5 Docker Compose Infrastructure
- **Services:**
  - `opensearch-node` - OpenSearch single-node cluster with security plugin
  - `localstack` - AWS service emulation (IAM, Secrets Manager, STS)
  - `spring-boot-rest` - REST API and UI application
  - `spring-boot-batch` - Batch synchronization service
  - `postgres` - Primary relational database
  - `data-generator` - One-time init container for 10k records
  - `aws-init` - One-time init container for IAM/Secrets setup
- **Networking:** Shared Docker network for inter-service communication
- **Volumes:** Persistent storage for OpenSearch data and PostgreSQL

---

## 4. Non-Functional Requirements

### 4.1 Performance
- Search response time: < 500ms for 10k records
- Batch sync of 10k records: < 5 minutes
- Batch incremental sync: < 30 seconds

### 4.2 Security
- All OpenSearch requests signed with AWS SigV4
- IAM role-based access control
- Secrets stored in AWS Secrets Manager (local simulation)
- SSL/TLS for all service communications
- No hardcoded credentials in application code

### 4.3 Scalability
- Horizontal scaling of Spring Boot REST instances
- OpenSearch cluster expansion capability
- Batch job partitioning for large datasets

### 4.4 Observability
- Application logs via Docker logging
- Batch job execution history
- OpenSearch index metrics

---

## 5. Data Model

### 5.1 Employee Entity
```yaml
fields:
  id: Long (Primary Key, auto-generated)
  firstName: String (required, 2-50 chars)
  lastName: String (required, 2-50 chars)
  email: String (required, valid email format)
  department: String (required)
  position: String (required)
  salary: BigDecimal (optional, > 0)
  hireDate: LocalDate (required)
  isActive: Boolean (default: true)
  createdAt: LocalDateTime (auto)
  updatedAt: LocalDateTime (auto)
```

### 5.2 OpenSearch Index Mapping
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
      "fullName": {
        "type": "text",
        "analyzer": "standard",
        "copy_to": "fullText"
      },
      "fullText": {
        "type": "text",
        "analyzer": "standard"
      }
    }
  }
}
```

---

## 6. API Specifications

### 6.1 REST Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/employees` | List employees with pagination |
| GET | `/api/employees/{id}` | Get employee by ID |
| POST | `/api/employees` | Create new employee |
| PUT | `/api/employees/{id}` | Update employee |
| DELETE | `/api/employees/{id}` | Delete employee |
| GET | `/api/employees/search?q={query}` | Full-text search |
| GET | `/api/employees/search?department={dept}` | Filter by department |

### 6.2 Batch Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/batch/run` | Trigger batch job manually |
| GET | `/batch/status` | Get last job execution status |

---

## 7. Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Backend Framework** | Spring Boot | 3.3.x |
| **Language** | Java | 21 |
| **ORM** | Spring Data JPA / Hibernate | - |
| **Database** | PostgreSQL | 16 |
| **Search Engine** | OpenSearch | 2.19 |
| **Batch Processing** | Spring Batch | - |
| **AWS SDK** | AWS SDK for Java v2 | 2.24+ |
| **Container Orchestration** | Docker Compose | - |
| **Local AWS** | LocalStack | 3.x |
| **Frontend** | Thymeleaf + Bootstrap | - |
| **Build Tool** | Maven | 3.9+ |
| **Authentication** | AWS SigV4 via AwsSdk2Transport | - |

---

## 8. Docker Compose Configuration

```yaml
version: '3.9'

services:
  postgres:
    image: postgres:16-alpine
    container_name: employee-postgres
    environment:
      POSTGRES_DB: employeedb
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: admin123
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  opensearch-node:
    image: opensearchproject/opensearch:2.19.0
    container_name: opensearch-node
    environment:
      - discovery.type=single-node
      - plugins.security.disabled=false
      - plugins.security.ssl.http.enabled=true
      - plugins.security.ssl.http.pemcertfilepath=/usr/share/opensearch/config/node-cert.pem
      - plugins.security.ssl.http.pemkeyfilepath=/usr/share/opensearch/config/node-key.pem
      - plugins.security.ssl.http.pemtrustedcasfilepath=/usr/share/opensearch/config/root-ca.pem
      - plugins.security.authcz.admin_dn=CN=admin,OU=admin,O=admin,L=admin,ST=admin,C=admin
      - plugins.security.audit.type=internal_opensearch
      - plugins.security.enable_sigv4_support=true
      - OPENSEARCH_JAVA_OPTS=-Xms2g -Xmx2g
    ports:
      - "9200:9200"
      - "9600:9600"
    volumes:
      - opensearch_data:/usr/share/opensearch/data
      - ./opensearch/security/config:/usr/share/opensearch/config/security/config
      - ./opensearch/ssl:/usr/share/opensearch/config/ssl

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

  data-generator:
    image: alpine:3.19
    container_name: data-generator
    depends_on:
      - postgres
      - opensearch-node
    volumes:
      - ./scripts:/scripts
    command: sh -c "apk add --no-cache curl postgresql-client jq python3 && python3 /scripts/generate_employees.py && chmod +x /scripts/populate_opensearch.sh && /scripts/populate_opensearch.sh"
    environment:
      - POSTGRES_HOST=postgres
      - POSTGRES_USER=admin
      - POSTGRES_PASSWORD=admin123
      - OPENSEARCH_HOST=opensearch-node
      - OPENSEARCH_PORT=9200

  aws-init:
    image: amazon/aws-cli:2.17
    container_name: aws-init
    depends_on:
      - localstack
      - opensearch-node
    environment:
      - AWS_ACCESS_KEY_ID=test
      - AWS_SECRET_ACCESS_KEY=test
      - AWS_DEFAULT_REGION=us-east-1
      - LOCALSTACK_HOST=localstack
    volumes:
      - ./scripts:/scripts
    command: >
      sh -c "
        sleep 10 &&
        python3 /scripts/aws_setup.py
      "

  spring-boot-rest:
    build:
      context: ./spring-boot-rest
      dockerfile: Dockerfile
    container_name: spring-boot-rest
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/employeedb
      - SPRING_DATASOURCE_USERNAME=admin
      - SPRING_DATASOURCE_PASSWORD=admin123
      - AWS_REGION=us-east-1
      - OPENSEARCH_ENDPOINT=https://opensearch-node:9200
      - LOCALSTACK_ENDPOINT=http://localstack:4566
      - AWS_ACCESS_KEY_ID=test
      - AWS_SECRET_ACCESS_KEY=test
    depends_on:
      postgres:
        condition: service_healthy
      opensearch-node:
        condition: service_healthy
      localstack:
        condition: service_healthy
      aws-init:
        condition: service_completed_successfully
      data-generator:
        condition: service_completed_successfully
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 5

  spring-boot-batch:
    build:
      context: ./spring-boot-batch
      dockerfile: Dockerfile
    container_name: spring-boot-batch
    ports:
      - "8081:8081"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/employeedb
      - SPRING_DATASOURCE_USERNAME=admin
      - SPRING_DATASOURCE_PASSWORD=admin123
      - AWS_REGION=us-east-1
      - OPENSEARCH_ENDPOINT=https://opensearch-node:9200
      - LOCALSTACK_ENDPOINT=http://localstack:4566
      - AWS_ACCESS_KEY_ID=test
      - AWS_SECRET_ACCESS_KEY=test
    depends_on:
      postgres:
        condition: service_healthy
      opensearch-node:
        condition: service_healthy
      localstack:
        condition: service_healthy
      aws-init:
        condition: service_completed_successfully
      spring-boot-rest:
        condition: service_healthy

volumes:
  postgres_data:
  opensearch_data:
  localstack_data:

```

---

## 9. Setup and Initialization Scripts

### 9.1 Data Generator (scripts/generate_employees.py)
- Generates 10,000 employee records using Faker library
- Outputs to CSV format
- Inserts directly into PostgreSQL
- Prepares bulk JSON for OpenSearch indexing

### 9.2 OpenSearch Population (scripts/populate_opensearch.sh)
- Reads generated employee data
- Uses OpenSearch `_bulk` API to index 10k records
- Configures index settings and mappings
- Verifies document count

### 9.3 AWS Setup (scripts/aws_setup.py)
- Creates IAM Role for OpenSearch access
- Attaches custom policy for employee index operations
- Creates Secrets Manager secret with OpenSearch credentials
- Configures OpenSearch role mappings via security API

---

## 10. Spring Boot REST Application Details

### 10.1 Modules
```
spring-boot-rest/
├── src/main/java/com/example/opensearch/
│   ├── config/
│   │   ├── AwsConfig.java
│   │   ├── OpenSearchConfig.java
│   │   └── WebConfig.java
│   ├── controller/
│   │   └── EmployeeController.java
│   ├── model/
│   │   └── Employee.java
│   ├── repository/
│   │   ├── EmployeeJpaRepository.java
│   │   └── OpenSearchEmployeeRepository.java
│   ├── service/
│   │   ├── EmployeeService.java
│   │   ├── OpenSearchService.java
│   │   └── BatchTriggerService.java
│   └── OpenSearchRestApplication.java
├── src/main/resources/
│   ├── templates/
│   │   ├── search.html
│   │   ├── form.html
│   │   └── details.html
│   └── application.yml
└── Dockerfile
```

### 10.2 Key Configuration (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/employeedb
    username: admin
    password: admin123
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  thymeleaf:
    cache: false

aws:
  region: us-east-1
  credentials:
    access-key: test
    secret-key: test
  opensearch:
    endpoint: https://localhost:9200
    index: employees

logging:
  level:
    com.example.opensearch: DEBUG
    org.opensearch.client: DEBUG
```

---

## 11. Spring Boot Batch Application Details

### 11.1 Modules
```
spring-boot-batch/
├── src/main/java/com/example/batch/
│   ├── config/
│   │   ├── BatchConfig.java
│   │   ├── OpenSearchConfig.java
│   │   └── AwsConfig.java
│   ├── model/
│   │   └── Employee.java
│   ├── processor/
│   │   └── EmployeeItemProcessor.java
│   ├── reader/
│   │   └── EmployeeJdbcReader.java
│   ├── writer/
│   │   ├── EmployeeJpaWriter.java
│   │   └── OpenSearchBulkWriter.java
│   └── BatchApplication.java
├── src/main/resources/
│   ├── jobs/
│   │   └── employee-sync-job.xml
│   └── application.yml
└── Dockerfile
```

### 11.2 Batch Job Configuration
```java
@Bean
public Job employeeSyncJob(JobBuilderFactory jobs, Step step) {
    return jobs.get("employeeSyncJob")
        .incrementer(new RunIdIncrementer())
        .flow(step)
        .end()
        .build();
}

@Bean
public Step step(StepBuilderFactory steps, ItemReader<Employee> reader,
                 ItemProcessor<Employee, Employee> processor,
                 ItemWriter<Employee> writer) {
    return steps.get("employeeSyncStep")
        .<Employee, Employee>chunk(100)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build();
}
```

---

## 12. Security Configuration

### 12.1 IAM Role Policy (JSON)
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::000000000000:role/opensearch-role"
      },
      "Action": "es:*",
      "Resource": "arn:aws:es:us-east-1:000000000000:domain/opensearch-node/employees/*"
    }
  ]
}
```

### 12.2 OpenSearch Role Mapping
```json
{
  "opendistro_roles_mapping": {
    "opensearch-admin": {
      "backend_roles": ["admin"],
      "users": ["*"]
    },
    "employee-reader": {
      "backend_roles": ["employee-reader"],
      "users": ["*"],
      "index_permissions": [
        {
          "index_patterns": ["employees"],
          "allowed_actions": ["indices:data/read/search"]
        }
      ]
    },
    "employee-writer": {
      "backend_roles": ["employee-writer"],
      "users": ["*"],
      "index_permissions": [
        {
          "index_patterns": ["employees"],
          "allowed_actions": ["indices:data/write/index", "indices:data/write/bulk"]
        }
      ]
    }
  }
}
```

---

## 13. Testing Strategy

### 13.1 Unit Tests
- Spring Boot REST controllers and services
- Batch item processor logic
- OpenSearch query builders
- AWS SigV4 signing verification

### 13.2 Integration Tests
- PostgreSQL connectivity and CRUD operations
- OpenSearch index creation and document CRUD
- SigV4-signed request verification
- Batch job end-to-end execution

### 13.3 Docker Compose Tests
- Startup verification script
- Data generation validation (10k records)
- Batch sync completion verification
- SigV4 request signing end-to-end

---

## 14. Verification Checklist

- [ ] OpenSearch container starts with security plugin enabled
- [ ] 10,000 employee records are indexed in OpenSearch
- [ ] IAM role and policies created in LocalStack
- [ ] Secrets Manager contains OpenSearch connection credentials
- [ ] Spring Boot REST app connects to PostgreSQL
- [ ] Spring Boot REST app can search OpenSearch with SigV4
- [ ] CRUD operations via REST app update PostgreSQL
- [ ] Spring Boot Batch app syncs PostgreSQL changes to OpenSearch
- [ ] Multi-page UI displays search results with pagination
- [ ] All services communicate via Docker network
- [ ] Application logs show SigV4-signed requests to OpenSearch

---

## 15. Future Enhancements

1. **Asynchronous Connections:** Async OpenSearch client configuration
2. **Compressed Requests:** Enable gzip compression for bulk operations
3. **Connection Pooling:** Implement connection pooling for batch writes
4. **AI Agents Integration:** Enable AI agent queries via SigV4
5. **Monitoring Dashboard:** Grafana + Prometheus metrics
6. **Kubernetes Deployment:** Helm charts for K8s deployment
7. **OpenSearch Serverless:** Support for Amazon OpenSearch Serverless
8. **Multi-Index Sync:** Support syncing multiple entity types
9. **Change Data Capture:** Debezium integration for real-time sync
10. **GraphQL API:** GraphQL wrapper on top of REST endpoints

---

## 16. References

- [AWS SigV4 Support for OpenSearch Clients](https://opensearch.org/blog/aws-sigv4-support-for-clients/)
- [OpenSearch Documentation](https://opensearch.org/docs/)
- [Spring Batch Documentation](https://docs.spring.io/spring-batch/)
- [LocalStack Documentation](https://docs.localstack.cloud/)
- [AWS SDK for Java v2](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/home.html)
- [OpenSearch Security Plugin](https://opensearch.org/docs/latest/security/index/)

---

*End of Requirements Document*
