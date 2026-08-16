# Docker Compose Learning Guide

**Project:** OpenSearch Employee Search Platform  
**Purpose:** Learn Docker Compose for this project  
**Version:** 1.0  
**Date:** 2026-08-16

---

## What is Docker Compose?

Docker Compose is a tool for defining and running multi-container Docker applications. With a single YAML file, you can configure all your application's services, networks, and volumes.

### Key Concepts for This Project

1. **Services:** Individual containers (PostgreSQL, OpenSearch, etc.)
2. **Networks:** Communication between containers
3. **Volumes:** Persistent data storage
4. **Depends_on:** Service startup order
5. **Healthchecks:** Service readiness verification
6. **Environment Variables:** Configuration

---

## Docker Compose File Structure

```yaml
version: '3.9'

services:
  service-name:
    image: image-name:tag
    container_name: container-name
    ports:
      - "host-port:container-port"
    environment:
      - VARIABLE=value
    volumes:
      - volume-name:/path/in/container
    networks:
      - network-name
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9200"]
      interval: 30s
      timeout: 10s
      retries: 5

volumes:
  volume-name:

networks:
  network-name:
    driver: bridge
```

---

## Services in This Project

### 1. PostgreSQL
```yaml
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
    - ./scripts/schema.sql:/docker-entrypoint-initdb.d/01-schema.sql
  healthcheck:
    test: ["CMD-SHELL", "pg_isready -U admin -d employeedb"]
    interval: 10s
    timeout: 5s
    retries: 5
```

**Key Points:**
- `environment` sets database credentials
- `volumes` persists data and runs init scripts
- `healthcheck` ensures database is ready

### 2. OpenSearch
```yaml
opensearch-node:
  image: opensearchproject/opensearch:2.19.0
  container_name: opensearch-node
  environment:
    - discovery.type=single-node
    - plugins.security.enable_sigv4_support=true
    - OPENSEARCH_JAVA_OPTS=-Xms2g -Xmx2g
  ports:
    - "9200:9200"
    - "9600:9600"
  volumes:
    - opensearch_data:/usr/share/opensearch/data
    - ./opensearch/security/config:/usr/share/opensearch/config/security/config
  healthcheck:
    test: ["CMD-SHELL", "curl -f http://localhost:9200/_cluster/health"]
    interval: 30s
    timeout: 10s
    retries: 5
```

**Key Points:**
- Single-node cluster for development
- Security plugin enabled with SigV4 support
- JVM heap size configured

### 3. LocalStack
```yaml
localstack:
  image: localstack/localstack:3.5
  container_name: localstack
  environment:
    - SERVICES=iam,secretsmanager,sts
    - DEBUG=1
    - DATA_DIR=/tmp/localstack/data
  ports:
    - "4566:4566"
  volumes:
    - localstack_data:/tmp/localstack
    - ./localstack/init:/docker-entrypoint-initaws.d
```

**Key Points:**
- Emulates AWS services locally
- `SERVICES` specifies which AWS services to emulate
- Init scripts run on container startup

### 4. Spring Boot Applications
```yaml
spring-boot-rest:
  build:
    context: ./spring-boot-rest
    dockerfile: Dockerfile
  container_name: spring-boot-rest
  ports:
    - "8080:8080"
  environment:
    - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/employeedb
    - AWS_REGION=us-east-1
    - OPENSEARCH_ENDPOINT=http://opensearch-node:9200
  depends_on:
    postgres:
      condition: service_healthy
    opensearch-node:
      condition: service_healthy
```

**Key Points:**
- `build` builds from Dockerfile
- `depends_on` with `condition: service_healthy` ensures dependencies are ready
- Environment variables configure the application

---

## Networks

All services are connected to a shared network:

```yaml
networks:
  app-network:
    driver: bridge

services:
  spring-boot-rest:
    networks:
      - app-network
  spring-boot-batch:
    networks:
      - app-network
```

**Communication:**
- Services communicate using service names as hostnames
- Example: `jdbc:postgresql://postgres:5432/employeedb`
- Example: `http://opensearch-node:9200`

---

## Volumes

### Named Volumes
```yaml
volumes:
  postgres_data:
  opensearch_data:
  localstack_data:
```

**Purpose:**
- Persist data between container restarts
- Survive `docker-compose down`
- Share data between containers

### Bind Mounts
```yaml
volumes:
  - ./scripts/schema.sql:/docker-entrypoint-initdb.d/01-schema.sql
  - ./opensearch/security/config:/usr/share/opensearch/config/security/config
```

**Purpose:**
- Mount local files/folders into containers
- Used for configuration files
- Changes reflect immediately

---

## Health Checks

Health checks ensure services are ready before dependent services start:

```yaml
healthcheck:
  test: ["CMD-SHELL", "pg_isready -U admin -d employeedb"]
  interval: 10s
  timeout: 5s
  retries: 5
```

**Parameters:**
- `test`: Command to check health
- `interval`: Time between checks
- `timeout`: Time to wait for response
- `retries`: Number of retries before marking unhealthy

---

## Common Commands

### Start Services
```bash
# Start all services
docker-compose up

# Start in background
docker-compose up -d

# Start specific service
docker-compose up postgres opensearch-node
```

### Stop Services
```bash
# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

### View Logs
```bash
# All logs
docker-compose logs

# Specific service logs
docker-compose logs postgres

# Follow logs
docker-compose logs -f opensearch-node
```

### Execute Commands
```bash
# Execute command in running container
docker-compose exec postgres psql -U admin -d employeedb

# Execute bash shell
docker-compose exec spring-boot-rest sh
```

---

## Best Practices

1. **Use Specific Image Tags:** Don't use `latest` in production
2. **Set Resource Limits:** CPU and memory constraints
3. **Use Health Checks:** Ensure services are ready
4. **Don't Store Secrets:** Use environment variables or secrets
5. **Use .dockerignore:** Exclude unnecessary files
6. **Multi-stage Builds:** Reduce image size
7. **Named Volumes:** For persistent data

---

## Troubleshooting

### Service Won't Start
```bash
# Check logs
docker-compose logs service-name

# Check service status
docker-compose ps
```

### Port Already in Use
```bash
# Check what's using the port
lsof -i :5432

# Change port in docker-compose.yml
ports:
  - "5433:5432"
```

### Volume Issues
```bash
# List volumes
docker volume ls

# Remove unused volumes
docker volume prune
```

---

## Next Steps

1. Learn [Docker Basics](basics.md)
2. Study [Service Configuration](services.md)
3. Understand [Networking](networking.md)
4. Practice with [Volumes](volumes.md)

---

*Part of the OpenSearchClientsWithAWSSigV4 Learning Documentation*
