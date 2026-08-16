# Learning Documentation

**Project:** OpenSearch Employee Search Platform  
**Purpose:** Onboarding guide for new team members  
**Version:** 1.0  
**Date:** 2026-08-16

---

## Purpose

This documentation helps new developers quickly get up to speed with the technologies used in this project. Each section provides focused, practical knowledge tailored to this application's needs.

---

## Technology Learning Path

### Prerequisites
- Basic Java knowledge
- Basic web development understanding
- Familiarity with REST APIs
- Basic database concepts

### Learning Path

```
┌─────────────────────────────────────────────────────────────────┐
│                    Learning Path                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Week 1: Foundation                                             │
│  ├── Docker Compose (Day 1-2)                                   │
│  ├── Spring Boot Basics (Day 3-5)                              │
│  └── PostgreSQL Basics (Day 6-7)                               │
│                                                                 │
│  Week 2: Backend Services                                       │
│  ├── OpenSearch (Day 8-10)                                     │
│  ├── AWS SigV4 (Day 11-12)                                     │
│  └── Spring Boot Batch (Day 13-14)                             │
│                                                                 │
│  Week 3: Frontend & Testing                                     │
│  ├── Angular Basics (Day 15-17)                                │
│  ├── Unit Testing (Day 18-19)                                  │
│  └── Integration Testing (Day 20-21)                           │
│                                                                 │
│  Week 4: Advanced Topics                                        │
│  ├── LocalStack (Day 22-23)                                    │
│  ├── Docker Deep Dive (Day 24-25)                              │
│  └── Project Integration (Day 26-28)                           │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Documentation Structure

```
docs/learning/
├── README.md                    # This file
├── spring-boot/
│   ├── README.md                # Spring Boot learning guide
│   ├── getting-started.md       # Setup and first project
│   ├── rest-api.md              # REST API development
│   ├── data-jpa.md              # Database access with JPA
│   ├── security.md              # Security basics
│   └── testing.md               # Testing Spring Boot apps
├── opensearch/
│   ├── README.md                # OpenSearch learning guide
│   ├── basics.md                # OpenSearch fundamentals
│   ├── indexing.md              # Data indexing
│   ├── searching.md             # Search queries
│   ├── mappings.md              # Index mappings
│   └── java-client.md           # Java client usage
├── angular/
│   ├── README.md                # Angular learning guide
│   ├── basics.md                # Angular fundamentals
│   ├── components.md            # Component development
│   ├── services.md              # Services and HTTP
│   ├── routing.md               # Navigation
│   └── forms.md                 # Forms and validation
├── docker-compose/
│   ├── README.md                # Docker Compose guide
│   ├── basics.md                # Docker fundamentals
│   ├── services.md              # Service configuration
│   ├── networking.md            # Container networking
│   └── volumes.md               # Data persistence
├── localstack/
│   ├── README.md                # LocalStack guide
│   ├── basics.md                # LocalStack fundamentals
│   ├── iam.md                   # IAM emulation
│   ├── secrets-manager.md       # Secrets Manager emulation
│   └── testing.md               # Testing with LocalStack
├── aws-sigv4/
│   ├── README.md                # AWS SigV4 guide
│   ├── basics.md                # SigV4 fundamentals
│   ├── java-client.md           # Java client integration
│   └── rotation.md              # Credential rotation
└── testing/
    ├── README.md                # Testing overview
    ├── unit-testing/
    │   ├── README.md            # Unit testing guide
    │   ├── junit.md             # JUnit 5 basics
    │   ├── mockito.md           # Mocking with Mockito
    │   └── coverage.md          # Code coverage
    ├── cucumber/
    │   ├── README.md            # Cucumber guide
    │   ├── basics.md            # BDD fundamentals
    │   ├── feature-files.md     # Writing feature files
    │   └── step-defs.md         # Step definitions
    └── playwright/
        ├── README.md            # Playwright guide
        ├── basics.md            # Playwright fundamentals
        ├── selectors.md         # Element selection
        └── assertions.md        # Test assertions
```

---

## Quick Start Guides

### For Backend Developers

1. Start with [Spring Boot Basics](spring-boot/getting-started.md)
2. Learn [OpenSearch Basics](opensearch/basics.md)
3. Understand [AWS SigV4](aws-sigv4/basics.md)
4. Study [Spring Boot Batch](spring-boot/batch.md)
5. Practice [Unit Testing](testing/unit-testing/junit.md)

### For Frontend Developers

1. Start with [Angular Basics](angular/basics.md)
2. Learn [Angular Components](angular/components.md)
3. Study [Angular Services](angular/services.md)
4. Practice [Playwright Testing](testing/playwright/basics.md)

### For DevOps Engineers

1. Start with [Docker Compose](docker-compose/basics.md)
2. Learn [LocalStack](localstack/basics.md)
3. Study [Docker Volumes](docker-compose/volumes.md)
4. Practice [Integration Testing](testing/cucumber/basics.md)

---

## Learning Resources

### Official Documentation
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [OpenSearch Documentation](https://opensearch.org/docs/)
- [Angular Documentation](https://angular.io/docs)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [LocalStack Documentation](https://docs.localstack.cloud/)
- [AWS SigV4 Documentation](https://docs.aws.amazon.com/general/latest/gr/sigv4_signing.html)

### Video Tutorials
- Spring Boot: [Spring.io Guides](https://spring.io/guides)
- OpenSearch: [OpenSearch YouTube](https://www.youtube.com/c/OpenSearchProject)
- Angular: [Angular YouTube Channel](https://www.youtube.com/c/Angular)
- Docker: [Docker YouTube Channel](https://www.youtube.com/c/DockerInc)

### Practice Exercises

1. **Spring Boot:** Build a simple REST API with CRUD operations
2. **OpenSearch:** Create an index and perform search queries
3. **Angular:** Build a component that fetches data from an API
4. **Docker:** Containerize a simple application
5. **Testing:** Write unit tests for a service class

---

## Mentorship

### Getting Help
1. Check this documentation first
2. Search existing issues and discussions
3. Ask in the team Slack channel
4. Schedule 1:1 with senior team members

### Code Reviews
- All code must be reviewed
- Reviewers should provide constructive feedback
- Focus on code quality, not just correctness
- Share knowledge through reviews

---

*End of Learning Documentation*
