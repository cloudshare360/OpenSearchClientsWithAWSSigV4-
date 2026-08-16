# Domain Requirements

**Project:** OpenSearch Employee Search Platform  
**Version:** 1.0  
**Date:** 2026-08-16  
**Stakeholder:** Engineering Team

---

## 1. Business Context

### 1.1 Problem Statement
Organizations using Amazon OpenSearch Service with fine-grained access control currently lack native client support for AWS SigV4 authentication. This forces developers to use workarounds like cURL requests or proxy servers, which:
- Increase development complexity
- Reduce application performance
- Introduce security vulnerabilities
- Complicate maintenance and monitoring

### 1.2 Business Objectives
- Demonstrate native AWS SigV4 support in OpenSearch clients
- Provide a reference implementation for enterprise adoption
- Enable fine-grained access control without workarounds
- Support credential rotation without application downtime
- Enable real-time search capabilities for employee data

### 1.3 Scope
**In Scope:**
- Spring Boot REST API with OpenSearch integration
- Spring Boot Batch for index synchronization
- Angular UI for employee search and management
- AWS SigV4 authentication with credential rotation
- Full-text search capabilities
- CRUD operations on employee data

**Out of Scope:**
- Production-grade authentication/authorization
- Multi-tenancy support
- Advanced analytics and reporting
- Mobile applications
- Third-party integrations

---

## 2. Stakeholder Analysis

| Stakeholder | Role | Interest | Influence |
|-------------|------|----------|-----------|
| Engineering Team | Development | High | High |
| DevOps Team | Infrastructure | Medium | Medium |
| Security Team | Compliance | High | Medium |
| Product Management | Roadmap | Medium | High |
| End Users | Consumption | High | Low |

---

## 3. User Stories

### 3.1 Employee Search User
```
As an HR manager
I want to search employees by name, email, department, or position
So that I can quickly find employee information

Acceptance Criteria:
- Full-text search across all employee fields
- Search results display within 500ms
- Pagination support for large result sets
- Highlighted search terms in results
```

### 3.2 Employee Management User
```
As an HR administrator
I want to create, update, and delete employee records
So that I can maintain accurate employee information

Acceptance Criteria:
- Form validation for all required fields
- Success/error feedback messages
- Confirmation dialog for delete operations
- Audit trail of changes
```

### 3.3 System Administrator
```
As a system administrator
I want automatic credential rotation without downtime
So that security compliance is maintained without manual intervention

Acceptance Criteria:
- Credentials refresh automatically from Secrets Manager
- No application restart required
- Graceful fallback to environment credentials
- Audit logs of credential rotations
```

### 3.4 Batch Operator
```
As a system operator
I want batch synchronization between PostgreSQL and OpenSearch
So that search indices stay consistent with source data

Acceptance Criteria:
- Batch job runs every 5 minutes by default
- Manual trigger capability
- Job execution history and metrics
- Error handling and retry logic
```

---

## 4. Use Cases

### 4.1 Use Case Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        Employee Search System                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────────────┐  │
│  │   HR User   │    │  Admin User │    │   System Admin      │  │
│  └──────┬──────┘    └──────┬──────┘    └──────────┬──────────┘  │
│         │                   │                       │            │
│         │                   │                       │            │
│  ┌──────▼───────────────────▼───────────────────────▼─────────┐ │
│  │                         Use Cases                          │ │
│  │                                                             │ │
│  │  • Search Employees    • Create Employee     • Configure   │ │
│  │  • View Employee       • Update Employee        System     │ │
│  │  • Edit Employee       • Delete Employee                  │ │
│  │  • Advanced Search     • Batch Sync                       │ │
│  │  • View Dashboard      • Monitor Jobs                     │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 Detailed Use Cases

| ID | Use Case | Actor | Precondition | Postcondition | Priority |
|----|----------|-------|--------------|---------------|----------|
| UC-01 | Search Employees | HR User | System is running | Search results displayed | High |
| UC-02 | Create Employee | Admin User | Form is accessible | Employee created in DB | High |
| UC-03 | Update Employee | Admin User | Employee exists | Employee updated in DB | High |
| UC-04 | Delete Employee | Admin User | Employee exists | Employee soft-deleted | High |
| UC-05 | View Dashboard | HR User | System is running | Statistics displayed | Medium |
| UC-06 | Trigger Batch Sync | System Admin | Batch service running | Sync job executed | Medium |
| UC-07 | Rotate Credentials | System Admin | Secret exists | No downtime | High |
| UC-08 | Monitor Jobs | System Admin | Batch service running | Job status visible | Low |

---

## 5. Business Rules

| Rule ID | Rule | Description |
|---------|------|-------------|
| BR-01 | Unique Email | Employee email must be unique across the system |
| BR-02 | Required Fields | First name, last name, email, department, position, hire date are mandatory |
| BR-03 | Positive Salary | Salary must be greater than 0 if provided |
| BR-04 | Soft Delete | Employees are soft-deleted (isActive flag) |
| BR-05 | Audit Trail | Created and updated timestamps are automatically maintained |
| BR-06 | SigV4 Required | All OpenSearch requests must be signed with SigV4 |
| BR-07 | Credential Rotation | Credentials must support rotation without restart |
| BR-08 | Batch Sync | PostgreSQL changes must sync to OpenSearch within 5 minutes |

---

## 6. Constraints

### 6.1 Business Constraints
- Must use AWS SigV4 for OpenSearch authentication
- Must support credential rotation without downtime
- Must demonstrate real-time search capabilities

### 6.2 Regulatory Constraints
- No hardcoded credentials in source code
- Audit logging for all data modifications
- Secure transmission of sensitive data

### 6.3 Technical Constraints
- Docker Compose for local development
- PostgreSQL as primary data store
- OpenSearch 2.x for search capabilities
- Spring Boot 3.x for backend
- Angular 17+ for frontend

---

## 7. Assumptions

- AWS credentials will be available via environment or Secrets Manager
- OpenSearch domain will have IAM-based access control enabled
- LocalStack will emulate AWS services for local development
- Network connectivity between services is reliable
- System will be used for demonstration/development purposes

---

## 8. Dependencies

| Dependency | Type | Risk | Mitigation |
|------------|------|------|------------|
| OpenSearch SigV4 Support | External | Medium | Use latest client versions |
| LocalStack SigV4 Support | External | Medium | Test locally, use real AWS for production |
| Angular Material | Framework | Low | Stable releases available |
| Spring Boot 3.x | Framework | Low | Mature framework |
| PostgreSQL 16 | Database | Low | Stable releases available |

---

## 9. Success Criteria

| Criteria | Metric | Target |
|----------|--------|--------|
| Search Performance | Response time | < 500ms for 10k records |
| Availability | Uptime | 99.5% during business hours |
| Credential Rotation | Downtime | 0 seconds |
| Batch Sync | Frequency | Every 5 minutes |
| Data Accuracy | Index consistency | 100% |
| User Satisfaction | Survey | > 4.0/5.0 |

---

## 10. Glossary

| Term | Definition |
|------|------------|
| SigV4 | AWS Signature Version 4 - authentication protocol |
| OpenSearch | Open-source search and analytics engine |
| LocalStack | Local AWS cloud stack for development |
| Spring Batch | Framework for batch processing |
| CRUD | Create, Read, Update, Delete operations |
| IAM | AWS Identity and Access Management |
| Soft Delete | Marking records as inactive instead of deleting |

---

*End of Domain Requirements Document*
