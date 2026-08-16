# Software Development Life Cycle (SDLC)

**Project:** OpenSearch Employee Search Platform  
**SDLC Model:** Agile with Iterative Development  
**Version:** 1.0  
**Date:** 2026-08-16

---

## SDLC Phases

This project follows a structured SDLC approach with the following phases:

```
┌─────────────────────────────────────────────────────────────────┐
│                        SDLC Workflow                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐ │
│  │ Requirements │───▶│ Analysis │───▶│ Design   │───▶│ Coding   │ │
│  │              │    │          │    │          │    │          │ │
│  └──────────┘    └──────────┘    └──────────┘    └──────────┘ │
│        ▲                                                  │     │
│        │                                                  │     │
│        └──────────────────────────────────────────────────┘     │
│                        Testing (Unit + Integration)            │
└─────────────────────────────────────────────────────────────────┘
```

### Phase 1: Requirements
- **Domain Requirements:** Business needs, user stories, use cases
- **Technical Requirements:** System constraints, technology choices, performance criteria

### Phase 2: Analysis
- Feasibility analysis
- Risk assessment
- Stakeholder analysis
- Gap analysis

### Phase 3: Design
- High-level architecture
- System design
- Database design
- API design
- Security design

### Phase 4: Detailed Design
- Class diagrams
- Sequence diagrams
- Component diagrams
- Database schema details
- Interface contracts

### Phase 5: Coding
- Implementation guidelines
- Coding standards
- Code reviews
- Version control practices

### Phase 6: Testing
- **Unit Testing:** 100% code coverage with JUnit/Mockito
- **Integration Testing:** Cucumber for REST API, Playwright for UI
- **Batch Integration:** Spring Boot Batch testing strategies

---

## SDLC Folder Structure

```
docs/sdlc/
├── README.md                           # This file - SDLC overview
├── requirements/
│   ├── README.md                       # Requirements overview
│   ├── domain-requirements.md          # Business/domain requirements
│   └── technical-requirements.md       # Technical requirements
├── analysis/
│   └── analysis.md                     # Analysis document
├── design/
│   ├── README.md                       # Design overview
│   ├── architecture.md                 # High-level architecture
│   └── detailed-design.md              # Detailed design document
├── coding/
│   ├── README.md                       # Coding guidelines
│   ├── standards.md                    # Coding standards
│   └── guidelines.md                   # Development guidelines
├── testing/
│   ├── README.md                       # Testing overview
│   ├── unit-testing/
│   │   ├── README.md                   # Unit testing plan
│   │   ├── coverage-strategy.md        # 100% coverage strategy
│   │   ├── spring-boot-rest/
│   │   │   └── unit-test-plan.md
│   │   └── spring-boot-batch/
│   │       └── unit-test-plan.md
│   ├── integration-testing/
│   │   ├── README.md                   # Integration testing overview
│   │   ├── cucumber-rest-api/
│   │   │   ├── README.md
│   │   │   ├── feature-files/
│   │   │   └── step-definitions/
│   │   ├── playwright-ui/
│   │   │   ├── README.md
│   │   │   └── test-scenarios.md
│   │   └── spring-boot-batch/
│   │       ├── README.md
│   │       └── batch-integration-plan.md
│   └── test-data/
│       └── README.md
└── TASK_TRACKER.md                     # Project status tracker
```

---

## How to Use This SDLC

1. **Start with Requirements:** Review `requirements/domain-requirements.md` and `requirements/technical-requirements.md`
2. **Proceed to Analysis:** Review `analysis/analysis.md` for feasibility and risk assessment
3. **Review Design:** Study `design/architecture.md` and `design/detailed-design.md`
4. **Follow Coding Standards:** Adhere to guidelines in `coding/standards.md` and `coding/guidelines.md`
5. **Implement Testing Strategy:** Follow plans in `testing/` directory
6. **Track Progress:** Update `TASK_TRACKER.md` as you complete each phase

---

## SDLC Principles

- **Test-Driven Development:** Write tests before code
- **Continuous Integration:** Automated builds and tests
- **Code Reviews:** All code must be reviewed before merging
- **Documentation:** Maintain up-to-date documentation
- **Automation:** Automate repetitive tasks
- **Quality First:** No compromise on code quality

---

*Part of the OpenSearchClientsWithAWSSigV4 project*
