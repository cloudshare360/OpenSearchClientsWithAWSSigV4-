# Detailed Design Document

**Project:** OpenSearch Employee Search Platform  
**Version:** 1.0  
**Date:** 2026-08-16  
**Designer:** Technical Architecture Team

---

## 1. Detailed Architecture

### 1.1 System Component Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Docker Compose Network                      │
└─────────────────────────────────────────────────────────────────────┘
                                   │
        ┌──────────────────────────┼──────────────────────────┐
        │                          │                          │
        ▼                          ▼                          ▼
┌──────────────┐         ┌──────────────────┐      ┌──────────────────┐
│   Angular    │         │  Spring Boot     │      │  Spring Boot     │
│     UI       │         │     REST         │      │     Batch        │
│  (:4200)     │         │    (:8080)       │      │    (:8081)       │
└──────┬───────┘         └────────┬─────────┘      └────────┬─────────┘
       │                          │                         │
       │                          │                         │
       ▼                          ▼                         ▼
┌──────────────┐         ┌──────────────────┐      ┌──────────────────┐
│   Nginx      │         │  Controllers     │      │  Job Launcher    │
│  (Reverse    │         │  • EmployeeCtrl  │      │  • BatchJob      │
│   Proxy)     │         │  • SearchCtrl    │      │  • StepBuilder   │
└──────────────┘         └────────┬─────────┘      └────────┬─────────┘
                                   │                         │
                                   ▼                         ▼
                         ┌──────────────────────────────────────────┐
                         │              Service Layer               │
                         │  • EmployeeService                       │
                         │  • OpenSearchService                     │
                         │  • BatchTriggerService                   │
                         │  • OpenSearchBatchService                │
                         └──────────────────────────────────────────┘
                                   │
                                   ▼
                         ┌──────────────────────────────────────────┐
                         │              Data Layer                   │
                         │  • EmployeeJpaRepository                 │
                         │  • OpenSearch Client (SigV4)             │
                         │  • DataSource (PostgreSQL)               │
                         └──────────────────────────────────────────┘
                                   │
                                   ▼
                         ┌──────────────────────────────────────────┐
                         │         Infrastructure Layer              │
                         │  • PostgreSQL (:5432)                     │
                         │  • OpenSearch (:9200)                     │
                         │  • LocalStack (:4566)                     │
                         └──────────────────────────────────────────┘
```

---

## 2. Class Diagrams

### 2.1 Spring Boot REST Application

```
┌─────────────────────────────────────────────────────────────────┐
│                    REST Application Classes                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────┐      ┌─────────────────────────────────┐  │
│  │ Employee        │      │ EmployeeController              │  │
│  │ ─────────────── │      │ ──────────────────────────────  │  │
│  │ + id: Long      │      │ + search(): ResponseEntity      │  │
│  │ + firstName     │      │ + create(): ResponseEntity      │  │
│  │ + lastName      │      │ + update(): ResponseEntity      │  │
│  │ + email         │      │ + delete(): ResponseEntity      │  │
│  │ + department    │      │ + getStats(): ResponseEntity    │  │
│  │ + position      │      └─────────────────────────────────┘  │
│  │ + salary        │                    │                       │
│  │ + hireDate      │                    ▼                       │
│  │ + isActive      │      ┌─────────────────────────────────┐  │
│  │ + createdAt     │      │ EmployeeService                 │  │
│  │ + updatedAt     │      │ ──────────────────────────────  │  │
│  └─────────────────┘      │ + getAll(): Page<Employee>      │  │
│                            │ + create(): Employee            │  │
│  ┌─────────────────┐      │ + update(): Employee            │  │
│  │ Employee        │      │ + delete(): void                │  │
│  │ JpaRepository   │      │ + search(): Page<Employee>      │  │
│  │ ─────────────── │      └─────────────────────────────────┘  │
│  │ + findById()    │                    │                       │
│  │ + findAll()     │                    ▼                       │
│  │ + save()        │      ┌─────────────────────────────────┐  │
│  │ + delete()      │      │ OpenSearchService               │  │
│  │ + search()      │      │ ──────────────────────────────  │  │
│  └─────────────────┘      │ + search(): Map<String,Object>  │  │
│                            │ + index(): Map<String,Object>   │  │
│  ┌─────────────────┐      │ + delete(): boolean             │  │
│  │ SecretsManager  │      │ + getInfo(): Map<String,Object> │  │
│  │ CredProvider    │      └─────────────────────────────────┘  │
│  │ ─────────────── │                    │                       │
│  │ + resolveCreds()│                    ▼                       │
│  │ + invalidate()  │      ┌─────────────────────────────────┐  │
│  └─────────────────┘      │ BatchTriggerService             │  │
│                            │ ──────────────────────────────  │  │
│  ┌─────────────────┐      │ + trigger(): void               │  │
│  │ AwsSdk2Transport│      └─────────────────────────────────┘  │
│  │ ─────────────── │                                          │
│  │ + OpenSearch    │                                          │
│  │   Client        │                                          │
│  └─────────────────┘                                          │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 Spring Boot Batch Application

```
┌─────────────────────────────────────────────────────────────────┐
│                    Batch Application Classes                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ BatchConfig                                                 ││
│  │ ─────────────────────────────────────────────────────────── ││
│  │ + employeeSyncJob(): Job                                    ││
│  │ + employeeSyncStep(): Step                                  ││
│  └─────────────────────────────────────────────────────────────┘│
│                            │                                    │
│                            ▼                                    │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ EmployeeSyncStep (Chunk-oriented)                           ││
│  │ ─────────────────────────────────────────────────────────── ││
│  │ Reader          Processor           Writer                  ││
│  │ ─────────────   ──────────────     ──────────────           ││
│  │ JdbcPagingItem  EmployeeItem       OpenSearchBulkWriter     ││
│  │ Reader          Processor                                     ││
│  └─────────────────────────────────────────────────────────────┘│
│                            │                                    │
│                            ▼                                    │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ OpenSearchBatchService                                       ││
│  │ ─────────────────────────────────────────────────────────── ││
│  │ + bulkIndex(List<EmployeeSyncItem>): void                   ││
│  │ - buildBulkRequest(): BulkRequest.Builder                   ││
│  │ - executeBulkRequest(): BulkResponse                        ││
│  └─────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. Sequence Diagrams

### 3.1 Employee Search Sequence

```
┌──────┐ ┌────────┐ ┌─────────┐ ┌──────────┐ ┌───────────┐
│User │ │Angular │ │ REST    │ │ Service  │ │OpenSearch │
│     │ │  UI    │ │Controller│ │          │ │           │
└──┬───┘ └───┬────┘ └────┬────┘ └────┬─────┘ └─────┬─────┘
   │         │           │            │             │
   │ 1. Enter│           │            │             │
   │ search  │           │            │             │
   │────────▶│           │            │             │
   │         │ 2. GET /  │            │             │
   │         │ api/      │            │             │
   │         │ employees │            │             │
   │         │ search?q= │            │             │
   │         │ john      │            │             │
   │         │───────────▶│            │             │
   │         │           │ 3. Call    │             │
   │         │           │ service    │             │
   │         │           │────────────▶│             │
   │         │           │            │ 4. Search   │
   │         │           │            │ with SigV4  │
   │         │           │            │─────────────▶│
   │         │           │            │             │ 5. Execute
   │         │           │            │             │ search
   │         │           │            │             │───▶
   │         │           │            │             │
   │         │           │            │ 6. Results  │
   │         │           │            │◀─────────────│
   │         │           │ 7. Return  │             │
   │         │           │◀────────────│             │
   │         │ 8. JSON   │            │             │
   │         │◀──────────│            │             │
   │ 9. Display│          │            │             │
   │◀─────────│          │            │             │
   │         │           │            │             │
```

### 3.2 Employee Creation Sequence

```
┌──────┐ ┌────────┐ ┌─────────┐ ┌──────────┐ ┌───────────┐ ┌──────┐
│User │ │Angular │ │ REST    │ │ Service  │ │Repository │ │  DB  │
│     │ │  UI    │ │Controller│ │          │ │           │ │      │
└──┬───┘ └───┬────┘ └────┬────┘ └────┬─────┘ └────┬─────┘ └──┬───┘
   │         │           │            │             │          │
   │ 1. Fill │           │            │             │          │
   │ form    │           │            │             │          │
   │────────▶│           │            │             │          │
   │         │ 2. POST / │            │             │          │
   │         │ api/      │            │             │          │
   │         │ employees │            │             │          │
   │         │───────────▶│            │             │          │
   │         │           │ 3. Call    │             │          │
   │         │           │ service    │             │          │
   │         │           │────────────▶│             │          │
   │         │           │            │ 4. Save()   │          │
   │         │           │            │─────────────▶│          │
   │         │           │            │             │ 5. INSERT│
   │         │           │            │             │─────────▶│
   │         │           │            │             │          │
   │         │           │            │             │ 6. ID    │
   │         │           │            │             │◀─────────│
   │         │           │            │ 7. Return   │          │
   │         │           │            │◀─────────────│          │
   │         │           │ 8. Return  │             │          │
   │         │           │◀────────────│             │          │
   │         │ 9. 201    │            │             │          │
   │         │◀──────────│            │             │          │
   │ 10.Show │           │            │             │          │
   │ success │           │            │             │          │
   │◀────────│           │            │             │          │
   │         │           │            │             │          │
```

### 3.3 Batch Synchronization Sequence

```
┌──────────┐ ┌──────────┐ ┌────────────┐ ┌──────────┐ ┌───────────┐
│Scheduler│ │Job       │ │ Step       │ │ Reader   │ │PostgreSQL │
│          │ │Launcher  │ │            │ │          │ │           │
└────┬─────┘ └────┬─────┘ └─────┬──────┘ └────┬─────┘ └─────┬─────┘
     │            │             │              │             │
     │ 1. Cron    │             │              │             │
     │ trigger    │             │              │             │
     │───────────▶│             │              │             │
     │            │ 2. Run job  │              │             │
     │            │────────────▶│              │             │
     │            │             │ 3. Execute   │             │
     │            │             │ step         │             │
     │            │             │─────────────▶│             │
     │            │             │              │ 4. Read 100│
     │            │             │              │ employees  │
     │            │             │              │────────────▶│
     │            │             │              │             │
     │            │             │              │ 5. Result   │
     │            │             │              │◀────────────│
     │            │             │ 6. Process   │             │
     │            │             │ each item    │             │
     │            │             │─────────────▶│             │
     │            │             │              │             │
     │            │             │ 7. Write     │             │
     │            │             │ bulk to      │             │
     │            │             │ OpenSearch   │             │
     │            │             │─────────────────────────────▶│
     │            │             │              │             │
     │            │             │ 8. Success   │             │
     │            │             │◀─────────────────────────────│
     │            │             │              │             │
     │            │             │ 9. Next      │             │
     │            │             │ chunk or end │             │
     │            │             │─────────────▶│             │
```

---

## 4. Database Design

### 4.1 Entity-Relationship Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        Database Schema                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                      employees                            │  │
│  ├───────────────────────────────────────────────────────────┤  │
│  │  id               BIGSERIAL PRIMARY KEY                   │  │
│  │  first_name       VARCHAR(50) NOT NULL                    │  │
│  │  last_name        VARCHAR(50) NOT NULL                    │  │
│  │  email            VARCHAR(100) UNIQUE NOT NULL            │  │
│  │  department       VARCHAR(50) NOT NULL                    │  │
│  │  position         VARCHAR(100) NOT NULL                   │  │
│  │  salary           NUMERIC(12,2)                           │  │
│  │  hire_date        DATE NOT NULL                           │  │
│  │  is_active        BOOLEAN NOT NULL DEFAULT TRUE           │  │
│  │  created_at       TIMESTAMP NOT NULL DEFAULT NOW()        │  │
│  │  updated_at       TIMESTAMP NOT NULL DEFAULT NOW()        │  │
│  └───────────────────────────────────────────────────────────┘  │
│                                                                 │
│  Indexes:                                                       │
│  • idx_employees_department ON employees(department)            │
│  • idx_employees_email ON employees(email)                      │
│  • idx_employees_hire_date ON employees(hire_date)              │
│  • idx_employees_is_active ON employees(is_active)              │
│                                                                 │
│  Triggers:                                                       │
│  • update_employees_updated_at (BEFORE UPDATE)                  │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 OpenSearch Index Mapping

```json
{
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0,
    "analysis": {
      "analyzer": {
        "default": {
          "type": "standard"
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "id": { "type": "long" },
      "firstName": { 
        "type": "text", 
        "analyzer": "standard",
        "fields": {
          "keyword": { "type": "keyword" }
        }
      },
      "lastName": { 
        "type": "text", 
        "analyzer": "standard",
        "fields": {
          "keyword": { "type": "keyword" }
        }
      },
      "email": { "type": "keyword" },
      "department": { "type": "keyword" },
      "position": { "type": "text", "analyzer": "standard" },
      "salary": { "type": "double" },
      "hireDate": { "type": "date", "format": "yyyy-MM-dd" },
      "fullText": {
        "type": "text",
        "analyzer": "standard"
      }
    }
  }
}
```

---

## 5. API Design

### 5.1 REST API Endpoints

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/api/employees` | List employees | Query params | `PagedResponse<Employee>` |
| GET | `/api/employees/{id}` | Get employee | - | `Employee` |
| POST | `/api/employees` | Create employee | `EmployeeCreateRequest` | `Employee` |
| PUT | `/api/employees/{id}` | Update employee | `EmployeeUpdateRequest` | `Employee` |
| DELETE | `/api/employees/{id}/delete` | Delete employee | - | `204 No Content` |
| GET | `/api/employees/search` | Search employees | Query params | `PagedResponse<Employee>` |
| GET | `/api/employees/stats` | Get statistics | - | `Map<String, Object>` |

### 5.2 Request/Response Models

```java
// EmployeeCreateRequest.java
public class EmployeeCreateRequest {
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @Email @NotBlank private String email;
    @NotBlank private String department;
    @NotBlank private String position;
    @Positive private BigDecimal salary;
    @NotNull private LocalDate hireDate;
    private Boolean isActive = true;
}

// EmployeeUpdateRequest.java
public class EmployeeUpdateRequest extends EmployeeCreateRequest {
    @NotNull private Long id;
}

// PagedResponse.java
public class PagedResponse<T> {
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
}
```

### 5.3 Error Response Format

```json
{
  "timestamp": "2026-08-16T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/employees",
  "errors": [
    {
      "field": "email",
      "message": "Email is required"
    }
  ]
}
```

---

## 6. Security Design

### 6.1 Authentication Flow

```java
// SecretsManagerCredentialProvider.java
@Component
public class SecretsManagerCredentialProvider implements AwsCredentialsProvider {
    private final AtomicReference<CachedCredentials> cachedCredentials;
    private final long refreshIntervalMs;
    
    @Override
    public AwsCredentials resolveCredentials() {
        if (isExpired()) {
            refreshCredentials();
        }
        return cachedCredentials.get().getCredentials();
    }
    
    public void invalidateCache() {
        cachedCredentials.set(null);
    }
}
```

### 6.2 OpenSearch SigV4 Configuration

```java
// OpenSearchConfig.java
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
                .setRetryStrategy(new RotatableRetryStrategy(retryBackoff))
                .build()
        );
    }
}
```

### 6.3 IAM Role Mappings

```yaml
# roles_mapping.yml
admin:
  backend_roles: ["admin"]
  users: ["*"]
  admin_roles: ["admin"]

employee-reader:
  backend_roles: ["employee-reader"]
  users: ["*"]
  index_permissions:
    - index_patterns: ["employees"]
      allowed_actions:
        - "indices:data/read/search"
        - "indices:data/read/get"

employee-writer:
  backend_roles: ["employee-writer"]
  users: ["*"]
  index_permissions:
    - index_patterns: ["employees"]
      allowed_actions:
        - "indices:data/write/index"
        - "indices:data/write/bulk"
        - "indices:data/write/delete"
```

---

## 7. Configuration Design

### 7.1 Application Configuration

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/employeedb
    username: admin
    password: admin123
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

aws:
  region: us-east-1
  secrets-manager:
    enabled: true
    secret-name: opensearch/credentials
    refresh-interval: 300000
  opensearch:
    endpoint: http://opensearch-node:9200
    index: employees
    connection-timeout: 30s
    max-retries: 3

batch:
  chunk:
    size: 100
  schedule:
    enabled: true
    cron: "0 */5 * * * *"
```

### 7.2 Docker Compose Configuration

```yaml
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: employeedb
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: admin123
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./scripts/schema.sql:/docker-entrypoint-initdb.d/01-schema.sql

  opensearch-node:
    image: opensearchproject/opensearch:2.19.0
    environment:
      - discovery.type=single-node
      - plugins.security.enable_sigv4_support=true
    volumes:
      - opensearch_data:/usr/share/opensearch/data
      - ./opensearch/security/config:/usr/share/opensearch/config/security/config

  localstack:
    image: localstack/localstack:3.5
    environment:
      - SERVICES=iam,secretsmanager,sts
    volumes:
      - ./localstack/init:/docker-entrypoint-initaws.d
```

---

## 8. Resilience Design

### 8.1 Retry Strategy

```java
@Retryable(
    value = {IOException.class, OpenSearchException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
)
public Map<String, Object> search(String query, int page, int size) {
    // Search implementation
}
```

### 8.2 Circuit Breaker Pattern

```java
@Component
public class OpenSearchCircuitBreaker {
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicLong lastFailureTime = new AtomicLong(0);
    private static final int FAILURE_THRESHOLD = 5;
    private static final long RECOVERY_TIME_MS = 60000;
    
    public boolean canProceed() {
        if (failureCount.get() >= FAILURE_THRESHOLD) {
            long now = System.currentTimeMillis();
            if (now - lastFailureTime.get() > RECOVERY_TIME_MS) {
                failureCount.set(0);
                return true;
            }
            return false;
        }
        return true;
    }
}
```

### 8.3 Fallback Mechanism

```java
public class OpenSearchService {
    public Map<String, Object> searchWithFallback(String query, int page, int size) {
        try {
            return openSearchClient.search(query, page, size);
        } catch (OpenSearchException e) {
            if (isAuthError(e)) {
                credentialProvider.invalidateCache();
            }
            log.warn("OpenSearch search failed, returning empty results", e);
            return Map.of("hits", List.of(), "total", 0);
        }
    }
}
```

---

## 9. Monitoring and Observability

### 9.1 Health Checks

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

### 9.2 Metrics

| Metric | Type | Description |
|--------|------|-------------|
| `opensearch.search.duration` | Timer | Search query execution time |
| `opensearch.index.duration` | Timer | Index operation time |
| `batch.job.duration` | Timer | Batch job execution time |
| `batch.records.processed` | Counter | Number of records processed |
| `http.requests` | Counter | HTTP request count |
| `credentials.refresh` | Counter | Credential refresh count |

### 9.3 Logging Strategy

```java
// Correlation ID for request tracing
@GetMapping("/api/employees")
public ResponseEntity<PagedResponse<Employee>> getEmployees(
        @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
    String requestId = correlationId != null ? correlationId : UUID.randomUUID().toString();
    log.info("[{}] Fetching employees page={}", requestId, page);
    // ...
}
```

---

## 10. Deployment Design

### 10.1 Docker Multi-Stage Build

```dockerfile
# Spring Boot REST Dockerfile
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 10.2 Environment Configuration

| Environment | Profile | Database | OpenSearch | AWS |
|-------------|---------|----------|------------|-----|
| Development | dev | PostgreSQL (Docker) | OpenSearch (Docker) | LocalStack |
| Testing | test | H2 | Embedded | Mocked |
| Production | prod | RDS | Amazon OpenSearch | Real AWS |

---

*End of Detailed Design Document*
