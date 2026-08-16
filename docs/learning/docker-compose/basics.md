# Docker Compose Basics

**Project:** OpenSearch Employee Search Platform  
**Purpose:** Learn Docker Compose fundamentals  
**Version:** 1.0  
**Date:** 2026-08-16

---

## What is Docker Compose?

Docker Compose is a tool for defining and running multi-container Docker applications. It uses a YAML file to configure all your application's services.

### Key Concepts

1. **Service:** A container definition
2. **Image:** Docker image to use
3. **Ports:** Map container ports to host
4. **Environment:** Environment variables
5. **Volumes:** Persistent data storage
6. **Networks:** Communication between containers

---

## Basic docker-compose.yml

```yaml
version: '3.9'

services:
  web:
    image: nginx:alpine
    ports:
      - "8080:80"
  
  database:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: mydb
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: admin123
    volumes:
      - db_data:/var/lib/postgresql/data

volumes:
  db_data:
```

---

## Common Commands

```bash
# Start all services
docker-compose up

# Start in background
docker-compose up -d

# Stop services
docker-compose down

# View logs
docker-compose logs

# Execute command in service
docker-compose exec database psql -U admin -d mydb

# List running services
docker-compose ps
```

---

## Project-Specific Configuration

### Services
- **postgres:** PostgreSQL database
- **opensearch-node:** OpenSearch search engine
- **localstack:** AWS service emulation
- **spring-boot-rest:** REST API service
- **spring-boot-batch:** Batch processing service
- **angular-ui:** Frontend application

### Networks
All services communicate via `app-network` bridge network.

### Volumes
- **postgres_data:** PostgreSQL data persistence
- **opensearch_data:** OpenSearch data persistence
- **localstack_data:** LocalStack data persistence

---

## Next Steps

1. Learn [Service Configuration](services.md)
2. Study [Networking](networking.md)
3. Understand [Volumes](volumes.md)
4. Practice with [Health Checks](healthchecks.md)

---

*Part of the OpenSearchClientsWithAWSSigV4 Learning Documentation*
