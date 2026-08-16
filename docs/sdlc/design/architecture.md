# Design Document - High-Level Architecture

**Project:** OpenSearch Employee Search Platform  
**Version:** 1.0  
**Date:** 2026-08-16  
**Architect:** Technical Architecture Team

---

## 1. Architecture Overview

### 1.1 Architectural Pattern
The system follows a **3-tier microservices architecture** with the following layers:

```
┌─────────────────────────────────────────────────────────────────┐
│                         Presentation Layer                      │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    Angular UI (:4200)                      │  │
│  │  • Search Interface                                       │  │
│  │  • Employee Management                                    │  │
│  │  • Dashboard                                              │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │ HTTP/REST
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                       Application Layer                         │
│  ┌─────────────────────────┐    ┌──────────────────────────┐   │
│  │  Spring Boot REST API   │    │  Spring Boot Batch       │   │
│  │       (:8080)           │    │       (:8081)            │   │
│  │  • Employee Controller  │    │  • Batch Job             │   │
│  │  • Search Service       │◄──►│  • Index Writer          │   │
│  │  • OpenSearch Client    │    │  • Scheduler             │   │
│  └───────────┬─────────────┘    └──────────────────────────┘   │
└──────────────┼─────────────────────────────────────────────────┘
               │ JDBC / OpenSearch Client
               ▼
┌─────────────────────────────────────────────────────────────────┐
│                        Data Layer                               │
│  ┌─────────────────────────┐    ┌──────────────────────────┐   │
│  │    PostgreSQL (:5432)   │    │   OpenSearch (:9200)     │   │
│  │  • Employee Table       │    │  • employees index       │   │
│  │  • Primary Data Store   │    │  • Full-text Search      │   │
│  └─────────────────────────┘    └──────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Infrastructure Layer                       │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    LocalStack (:4566)                      │  │
│  │  • IAM (Identity & Access Management)                      │  │
│  │  • Secrets Manager (Credential Storage)                     │  │
│  │  • STS (Security Token Service)                            │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 Design Principles

| Principle | Application |
|-----------|-------------|
| **Separation of Concerns** | Distinct layers for UI, API, Data |
| **Single Responsibility** | Each service has one primary purpose |
| **Dependency Inversion** | Depend on abstractions, not concretions |
| **Fail Fast** | Early validation and error detection |
| **Resilience** | Retry, timeout, and circuit breaker patterns |
| **Observability** | Logging, metrics, health checks |
| **Security by Design** | SigV4, IAM, Secrets Manager |

---

## 2. Component Architecture

### 2.1 Spring Boot REST API

```
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Boot REST API                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────────────┐  │
│  │ Controllers  │    │  Services   │    │   Repositories      │  │
│  │              │    │             │    │                     │  │
│  │ • Employee   │───▶│ • Employee  │───▶│ • EmployeeJpaRepo   │  │
│  │   Controller │    │   Service   │    │                     │  │
│  │ • Search     │    │ • OpenSearch│    │                     │  │
│  │   Controller │    │   Service   │    │                     │  │
│  └─────────────┘    └─────────────┘    └─────────────────────┘  │
│         │                   │                     │            │
│         │                   │                     │            │
│  ┌──────┴───────────────────┴─────────────────────┴─────────┐  │
│  │                   Configurations                           │  │
│  │  • AwsConfig           • OpenSearchConfig                  │  │
│  │  • SecretsManagerCredentialProvider                        │  │
│  │  • SecurityConfig                                          │  │
│  └────────────────────────────────────────────────────────────┘  │
│                                                                 │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                    External Integrations                    │ │
│  │  • PostgreSQL (HikariCP)                                   │ │
│  │  • OpenSearch (AwsSdk2Transport)                           │ │
│  │  • AWS Secrets Manager                                     │ │
│  │  • LocalStack                                              │ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 Spring Boot Batch Application

```
┌─────────────────────────────────────────────────────────────────┐
│                   Spring Boot Batch Application                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    Batch Job Configuration                │  │
│  │  • EmployeeSyncJob                                        │  │
│  │  • EmployeeSyncStep                                       │  │
│  └───────────────────────────────────────────────────────────┘  │
│                              │                                  │
│  ┌─────────────┐    ┌────────┴────────┐    ┌────────────────┐  │
│  │   Reader    │───▶│   Processor    │───▶│    Writer      │  │
│  │             │    │                │    │                │  │
│  │ • Jdbc      │    │ • Employee     │    │ • OpenSearch   │  │
│  │   Paging    │    │   Item         │    │   BulkWriter   │  │
│  │   Reader    │    │   Processor    │    │                │  │
│  └─────────────┘    └────────────────┘    └────────────────┘  │
│                              │                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                   External Integrations                    │ │
│  │  • PostgreSQL (DataSource)                                 │ │
│  │  • OpenSearch (AwsSdk2Transport)                           │ │
│  │  • AWS Secrets Manager                                     │ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### 2.3 Angular Frontend

```
┌─────────────────────────────────────────────────────────────────┐
│                       Angular Frontend                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                      Core Module                            │ │
│  │  • EmployeeService (HTTP Client)                            │ │
│  │  • AuthInterceptor (Request/Response Handling)              │ │
│  └────────────────────────────────────────────────────────────┘ │
│                              │                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                    Shared Components                        │ │
│  │  • ConfirmDialog                                            │ │
│  │  • LoadingSpinner                                           │ │
│  │  • Employee Model                                           │ │
│  └────────────────────────────────────────────────────────────┘ │
│                              │                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                    Feature Modules                          │ │
│  │  • Dashboard      - Statistics and metrics                  │ │
│  │  • Search         - Full-text search                        │ │
│  │  • EmployeeList   - Paginated employee list                 │ │
│  │  • EmployeeForm   - Create/Edit forms                       │ │
│  │  • EmployeeDetails - View employee details                  │ │
│  └────────────────────────────────────────────────────────────┘ │
│                              │                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                     Infrastructure                          │ │
│  │  • Router (Navigation)                                      │ │
│  │  • HttpClient (API Communication)                           │ │
│  │  • Forms (Form Validation)                                  │ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. Data Flow Architecture

### 3.1 Search Flow

```
User Input (Angular)
        │
        ▼
EmployeeService.search()
        │
        ▼ HTTP GET /api/employees/search?q=john
EmployeeController
        │
        ▼
EmployeeService.search()
        │
        ▼
EmployeeJpaRepository.search()
        │
        ▼ PostgreSQL Query
        │
        ▼
OpenSearchService.search()
        │
        ▼ SigV4 Signed Request
AwsSdk2Transport
        │
        ▼
OpenSearch Client
        │
        ▼ HTTP Request
OpenSearch Server
        │
        ▼ Search Results
        │
        ▼ JSON Response
Angular UI
```

### 3.2 CRUD Flow

```
User Action (Angular)
        │
        ▼
EmployeeService.create/update/delete()
        │
        ▼ HTTP POST/PUT/DELETE
EmployeeController
        │
        ▼
EmployeeService (with @Transactional)
        │
        ▼
EmployeeJpaRepository.save()
        │
        ▼ PostgreSQL Transaction
        │
        ▼ Success Response
        │
        ▼ Event Trigger
BatchTriggerService
        │
        ▼ HTTP POST /batch/run
Spring Boot Batch
        │
        ▼ Batch Job Execution
        │
        ▼ OpenSearch Update
```

### 3.3 Batch Sync Flow

```
Scheduler (Cron Trigger)
        │
        ▼
JobLauncher.run()
        │
        ▼
EmployeeSyncJob
        │
        ▼
EmployeeSyncStep (Chunk Processing)
        │
        ├─▶ Reader: JdbcPagingItemReader
        │       │
        │       ▼ PostgreSQL Query
        │       │
        │       ▼ EmployeeSyncItem
        │
        ├─▶ Processor: EmployeeItemProcessor
        │       │
        │       ▼ Transformation
        │       │
        │       ▼ EmployeeSyncItem
        │
        └─▶ Writer: OpenSearchBulkWriter
                │
                ▼ Bulk Index Request
                │
                ▼ SigV4 Signed
                │
                ▼ OpenSearch Bulk API
                │
                ▼ Success/Failure
```

---

## 4. Security Architecture

### 4.1 Authentication Flow

```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│   Angular   │      │ Spring Boot  │      │  OpenSearch │
│     UI      │      │    REST     │      │             │
└──────┬──────┘      └──────┬──────┘      └──────┬──────┘
       │                     │                     │
       │  1. User Request     │                     │
       │────────────────────▶│                     │
       │                     │  2. Retrieve         │
       │                     │  Credentials         │
       │                     │────────────────────▶│
       │                     │                     │
       │                     │  3. Return           │
       │                     │  Credentials         │
       │                     │◀────────────────────│
       │                     │                     │
       │                     │  4. SigV4 Sign       │
       │                     │  Request             │
       │                     │────────────────────▶│
       │                     │                     │
       │                     │  5. Signed Request   │
       │                     │────────────────────▶│
       │                     │                     │
       │                     │  6. Response         │
       │                     │◀────────────────────│
       │  7. JSON Response   │                     │
       │◀────────────────────│                     │
```

### 4.2 Credential Rotation Flow

```
Secrets Manager
       │
       ▼ Rotation Event
       │
       ▼ New Credentials
       │
       ▼
SecretsManagerCredentialProvider
       │
       ▼ Cache Invalidation
       │
       ▼ Next Request
       │
       ▼ Fetch New Credentials
       │
       ▼ SigV4 Sign with New Creds
       │
       ▼ OpenSearch Request
```

### 4.3 IAM Role Structure

| Role | Purpose | Permissions |
|------|---------|-------------|
| `admin` | Full OpenSearch access | All indices, all actions |
| `employee-reader` | Read-only access | Search, Get on employees |
| `employee-writer` | Write access | Index, Bulk, Delete on employees |
| `opensearch-role` | AWS IAM role | ES HTTP operations |

---

## 5. Deployment Architecture

### 5.1 Docker Compose Services

| Service | Image | Ports | Dependencies |
|---------|-------|-------|--------------|
| postgres | postgres:16-alpine | 5432 | - |
| opensearch-node | opensearchproject/opensearch:2.19.0 | 9200, 9600 | - |
| localstack | localstack/localstack:3.5 | 4566 | - |
| data-generator | alpine:3.19 | - | postgres, opensearch-node |
| aws-init | amazon/aws-cli:2.17 | - | localstack, opensearch-node |
| spring-boot-rest | Custom build | 8080 | All above |
| spring-boot-batch | Custom build | 8081 | All above |
| angular-ui | Custom build | 4200 | spring-boot-rest |

### 5.2 Network Topology

```
┌─────────────────────────────────────────────────────────────────┐
│                        app-network (bridge)                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
│  │  postgres   │  │ opensearch  │  │       localstack        │  │
│  │   :5432     │  │   :9200     │  │         :4566          │  │
│  └─────────────┘  └─────────────┘  └─────────────────────────┘  │
│         │                  │                    │               │
│         │                  │                    │               │
│  ┌──────┴──────────────────┴────────────────────┴───────────┐  │
│  │                  Application Services                     │  │
│  │  ┌─────────────────┐      ┌─────────────────────────┐    │  │
│  │  │ spring-boot-rest│      │   spring-boot-batch     │    │  │
│  │  │     :8080       │      │         :8081           │    │  │
│  │  └─────────────────┘      └─────────────────────────┘    │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              │                                  │
│  ┌───────────────────────────▼──────────────────────────────┐  │
│  │                    angular-ui (:4200)                     │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 6. Technology Decisions

### 6.1 Backend Framework: Spring Boot 3.3.4
**Rationale:**
- Mature, production-ready framework
- Excellent ecosystem and community support
- Built-in support for batch processing, security, and data access
- Native AWS SDK integration

### 6.2 Frontend Framework: Angular 17+
**Rationale:**
- Enterprise-grade framework
- TypeScript for type safety
- Component-based architecture
- Excellent form handling and validation
- Strong tooling with Angular CLI

### 6.3 Database: PostgreSQL 16
**Rationale:**
- ACID compliant
- Excellent JSON support
- Full-text search capabilities
- Widely adopted and supported
- Strong performance

### 6.4 Search Engine: OpenSearch 2.19.0
**Rationale:**
- Native AWS SigV4 support
- Open-source with active development
- Compatible with Elasticsearch APIs
- Strong security plugin
- Scalable architecture

### 6.5 AWS Emulation: LocalStack 3.5
**Rationale:**
- Local development without AWS costs
- Supports IAM, Secrets Manager, STS
- Compatible with AWS SDK
- Easy integration with Docker

---

## 7. Cross-Cutting Concerns

### 7.1 Logging Strategy
- **Framework:** SLF4J + Logback
- **Format:** Structured JSON logging
- **Levels:** DEBUG (dev), INFO (prod)
- **Correlation:** Request ID tracking

### 7.2 Monitoring Strategy
- **Health Checks:** Spring Boot Actuator
- **Metrics:** Micrometer + Prometheus
- **Tracing:** Request correlation IDs
- **Alerting:** Health endpoint monitoring

### 7.3 Error Handling Strategy
- **Global Exception Handler:** @RestControllerAdvice
- **Standard Error Response:** Consistent JSON format
- **Logging:** All errors logged with context
- **User Feedback:** Meaningful error messages

### 7.4 Configuration Management
- **External Configuration:** application.yml
- **Environment Variables:** Docker Compose
- **Secrets:** AWS Secrets Manager
- **Profiles:** dev, prod

---

## 8. Architecture Decision Records (ADRs)

### ADR-001: Use AWS SigV4 for OpenSearch Authentication
**Decision:** Use native AWS SigV4 signing via AwsSdk2Transport  
**Rationale:** Native client support, no workarounds needed, production-ready  
**Consequences:** Requires AWS credentials, depends on IAM configuration

### ADR-002: Use LocalStack for AWS Services
**Decision:** Use LocalStack for local AWS emulation  
**Rationale:** Cost-effective development, no AWS account required  
**Consequences:** May have limitations compared to real AWS

### ADR-003: Use Spring Batch for Index Synchronization
**Decision:** Implement batch processing with Spring Batch  
**Rationale:** Mature framework, chunk-based processing, retry support  
**Consequences:** Additional complexity, but proven reliability

### ADR-004: Use Angular for Frontend
**Decision:** Build frontend with Angular 17+  
**Rationale:** Enterprise-grade, TypeScript, component-based  
**Consequences:** Larger bundle size, but better maintainability

---

*End of High-Level Design Document*
