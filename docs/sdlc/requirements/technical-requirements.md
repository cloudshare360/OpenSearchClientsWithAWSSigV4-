# Technical Requirements

**Project:** OpenSearch Employee Search Platform  
**Version:** 1.0  
**Date:** 2026-08-16  
**Author:** Technical Architecture Team

---

## 1. System Overview

### 1.1 System Architecture
- **3-Tier Architecture:** Presentation (Angular UI) → Business Logic (Spring Boot) → Data (PostgreSQL + OpenSearch)
- **Microservices:** Two Spring Boot services (REST API + Batch)
- **Containerization:** Docker Compose for orchestration
- **Cloud Services:** LocalStack for AWS service emulation

### 1.2 System Components

| Component | Technology | Purpose |
|-----------|-----------|---------|
| **Frontend** | Angular 17+ | User interface |
| **REST API** | Spring Boot 3.3.4 | Employee CRUD and search |
| **Batch Service** | Spring Boot 3.3.4 + Spring Batch | Index synchronization |
| **Database** | PostgreSQL 16 | Primary data store |
| **Search Engine** | OpenSearch 2.19.0 | Full-text search |
| **AWS Emulation** | LocalStack 3.5 | IAM, Secrets Manager, STS |
| **Container** | Docker Compose | Service orchestration |

---

## 2. Functional Technical Requirements

### 2.1 Spring Boot REST API

#### FR-001: Employee CRUD Operations
- **Description:** Provide RESTful endpoints for employee management
- **Endpoints:**
  - `GET /api/employees` - List with pagination
  - `GET /api/employees/{id}` - Get by ID
  - `POST /api/employees` - Create employee
  - `PUT /api/employees/{id}` - Update employee
  - `DELETE /api/employees/{id}/delete` - Soft delete
- **Response Format:** JSON with consistent structure
- **Error Handling:** Standard HTTP status codes with error messages

#### FR-002: Full-Text Search
- **Description:** Search employees across multiple fields
- **Fields:** firstName, lastName, email, position, department, fullText
- **Implementation:** OpenSearch multi-match query
- **Response Time:** < 500ms for 10k records
- **Pagination:** Page-based results with configurable size

#### FR-003: Statistics Endpoint
- **Description:** Provide employee statistics
- **Metrics:** Total employees, active count, department count
- **Endpoint:** `GET /api/employees/stats`

### 2.2 Spring Boot Batch Application

#### FR-004: Batch Synchronization
- **Description:** Sync PostgreSQL data to OpenSearch index
- **Frequency:** Every 5 minutes (configurable)
- **Chunk Size:** 100 records per chunk
- **Error Handling:** Retry with exponential backoff
- **Manual Trigger:** `POST /batch/run`

#### FR-005: Batch Monitoring
- **Description:** Monitor batch job execution
- **Endpoint:** `GET /batch/status`
- **Metrics:** Job execution time, records processed, errors

### 2.3 Angular Frontend

#### FR-006: Search Interface
- **Description:** Full-text search with autocomplete
- **Features:** Search suggestions, result highlighting
- **Performance:** Debounced search input

#### FR-007: Employee Management
- **Description:** Forms for CRUD operations
- **Validation:** Client-side validation with error messages
- **Feedback:** Success/error notifications

#### FR-008: Dashboard
- **Description:** Statistics and metrics display
- **Charts:** Employee distribution by department
- **Real-time:** Auto-refresh capabilities

---

## 3. Non-Functional Technical Requirements

### 3.1 Performance Requirements

| Requirement | Metric | Target |
|-------------|--------|--------|
| Search Response Time | 95th percentile | < 500ms |
| API Response Time | 95th percentile | < 200ms |
| Page Load Time | Initial load | < 2s |
| Batch Job Duration | Full sync (10k records) | < 5 minutes |
| Database Query Time | Single record | < 50ms |
| OpenSearch Query Time | Search query | < 300ms |

### 3.2 Security Requirements

#### NFR-001: Authentication
- AWS SigV4 signing for all OpenSearch requests
- IAM role-based access control
- Secrets Manager for credential storage
- Automatic credential rotation

#### NFR-002: Authorization
- Role-based access control (RBAC)
- Index-level permissions
- Audit logging for all operations

#### NFR-003: Data Protection
- No hardcoded credentials
- TLS/SSL for service communication
- Sensitive data encrypted at rest

### 3.3 Reliability Requirements

| Requirement | Target |
|-------------|--------|
| Application Uptime | 99.5% |
| Data Backup Frequency | Daily |
| Recovery Time Objective (RTO) | < 1 hour |
| Recovery Point Objective (RPO) | < 24 hours |

### 3.4 Scalability Requirements
- Horizontal scaling of Spring Boot REST instances
- OpenSearch cluster expansion capability
- Batch job partitioning for large datasets
- Database connection pooling

### 3.5 Maintainability Requirements
- Code coverage > 80% (unit tests)
- Comprehensive logging
- Health check endpoints
- Monitoring and metrics

---

## 4. Technical Constraints

### 4.1 Technology Constraints
- Java 21 for backend development
- Angular 17+ for frontend
- PostgreSQL 16 for database
- OpenSearch 2.19.0 for search
- Spring Boot 3.3.4 for backend framework
- Maven for dependency management
- Docker Compose for orchestration

### 4.2 Integration Constraints
- OpenSearch Java client 2.6.0
- AWS SDK for Java v2.25.0
- LocalStack 3.5 for AWS emulation
- Spring Batch for batch processing

### 4.3 Deployment Constraints
- Docker containers for all services
- Shared Docker network for communication
- Volume persistence for data stores
- Health checks for service dependencies

---

## 5. System Interfaces

### 5.1 External Interfaces

#### 5.1.1 OpenSearch Interface
```
Protocol: HTTP/HTTPS
Port: 9200
Authentication: AWS SigV4
Data Format: JSON
```

#### 5.1.2 PostgreSQL Interface
```
Protocol: TCP
Port: 5432
Authentication: Username/Password
Data Format: SQL/ResultSet
```

#### 5.1.3 LocalStack Interface
```
Protocol: HTTP
Port: 4566
Services: IAM, Secrets Manager, STS
Authentication: AWS Signature V4
```

### 5.2 Internal Interfaces

#### 5.2.1 REST API to Database
```
Protocol: JDBC
Connection Pool: HikariCP
ORM: Hibernate
```

#### 5.2.2 REST API to OpenSearch
```
Protocol: HTTP
Client: OpenSearch Java Client
Authentication: AwsSdk2Transport with SigV4
```

#### 5.2.3 REST API to Batch Service
```
Protocol: HTTP
Endpoint: http://spring-boot-batch:8081/batch/run
Method: POST
```

---

## 6. Data Requirements

### 6.1 Data Models

#### 6.1.1 Employee Entity
```yaml
fields:
  id: Long (Primary Key, auto-generated)
  firstName: String (required, 2-50 chars)
  lastName: String (required, 2-50 chars)
  email: String (required, unique, valid email)
  department: String (required)
  position: String (required)
  salary: BigDecimal (optional, > 0)
  hireDate: LocalDate (required)
  isActive: Boolean (default: true)
  createdAt: LocalDateTime (auto)
  updatedAt: LocalDateTime (auto)
```

#### 6.1.2 OpenSearch Employee Document
```json
{
  "id": "long",
  "firstName": "text",
  "lastName": "text",
  "email": "keyword",
  "department": "keyword",
  "position": "text",
  "salary": "double",
  "hireDate": "date",
  "fullText": "text"
}
```

### 6.2 Data Migration
- Initial load: 10,000 employee records
- Data source: Python Faker generator
- Migration method: Bulk insert to PostgreSQL, bulk index to OpenSearch

---

## 7. Quality Attributes

### 7.1 Performance
- Search response time: < 500ms
- API response time: < 200ms
- Batch sync: < 5 minutes for 10k records

### 7.2 Security
- SigV4 authentication for OpenSearch
- IAM role-based access
- Secrets Manager integration
- No hardcoded credentials

### 7.3 Reliability
- Automatic retry on transient failures
- Graceful degradation on service unavailability
- Comprehensive error handling

### 7.4 Maintainability
- Modular architecture
- Comprehensive logging
- Health check endpoints
- Monitoring and metrics

---

## 8. Development Environment

### 8.1 Required Software
- Docker and Docker Compose
- Java 21 JDK
- Node.js 20+
- Python 3.9+
- Maven 3.9+
- Git

### 8.2 IDE Recommendations
- IntelliJ IDEA / Eclipse for Java
- VS Code for Angular development
- Docker Desktop for container management

### 8.3 Environment Variables
```bash
# Common
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=test
AWS_SECRET_ACCESS_KEY=test

# Spring Boot REST
OPENSEARCH_ENDPOINT=http://opensearch-node:9200
AWS_SECRETS_MANAGER_ENABLED=true
AWS_SECRETS_MANAGER_SECRET_NAME=opensearch/credentials

# Spring Boot Batch
OPENSEARCH_ENDPOINT=http://opensearch-node:9200
BATCH_SCHEDULE_ENABLED=true
BATCH_SCHEDULE_CRON=0 */5 * * * *
```

---

## 9. Monitoring and Logging

### 9.1 Application Logs
- Structured logging with SLF4J
- Log levels: DEBUG for development, INFO for production
- Correlation IDs for request tracing

### 9.2 Metrics
- Spring Boot Actuator endpoints
- OpenSearch query metrics
- Batch job execution metrics
- Docker container metrics

### 9.3 Health Checks
- `/actuator/health` - Application health
- `/actuator/info` - Application info
- `/actuator/metrics` - Application metrics

---

## 10. Backup and Recovery

### 10.1 Data Backup
- PostgreSQL: Daily automated backups
- OpenSearch: Snapshot and restore
- Docker volumes: Persistent storage

### 10.2 Recovery Procedures
- Database restore from backup
- OpenSearch index restoration
- Service restart procedures

---

*End of Technical Requirements Document*
