# Docker Usage Guide

## Building Docker Images with Jib

### Build all services
```bash
./mvnw clean package jib:dockerBuild
```

### Build specific service
```bash
# Build only tracker-command
./mvnw clean package jib:dockerBuild -pl tracker-command

# Build only tracker-query
./mvnw clean package jib:dockerBuild -pl tracker-query
```

### Skip tests during build
```bash
./mvnw clean package jib:dockerBuild -DskipTests
```

## Verify Images Were Created

```bash
docker images | grep tracker
```

You should see:
```
ghcr.io/tomassirio/tracker-command   latest              ...
ghcr.io/tomassirio/tracker-command   0.1.1-SNAPSHOT      ...
ghcr.io/tomassirio/tracker-query     latest              ...
ghcr.io/tomassirio/tracker-query     0.1.1-SNAPSHOT      ...
```

## Running with Docker Compose

### Start all services
```bash
docker-compose up -d
```

### View logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f tracker-command
docker-compose logs -f tracker-query
```

### Stop all services
```bash
docker-compose down
```

### Stop and remove volumes (clean database)
```bash
docker-compose down -v
```

## Running Individual Containers

### tracker-command
```bash
docker run -d \
  --name tracker-command \
  -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/wanderer_db \
  -e SPRING_DATASOURCE_USERNAME=wanderer \
  -e SPRING_DATASOURCE_PASSWORD=password \
  ghcr.io/tomassirio/tracker-command:latest
```

### tracker-query
```bash
docker run -d \
  --name tracker-query \
  -p 8082:8082 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/wanderer_db \
  -e SPRING_DATASOURCE_USERNAME=wanderer \
  -e SPRING_DATASOURCE_PASSWORD=password \
  ghcr.io/tomassirio/tracker-query:latest
```

## Service Endpoints

Once running, your services are available at:

- **tracker-command**: http://localhost:8081
  - Swagger UI: http://localhost:8081/swagger-ui.html
  - Health: http://localhost:8081/actuator/health
  
- **tracker-query**: http://localhost:8082
  - Swagger UI: http://localhost:8082/swagger-ui.html
  - Health: http://localhost:8082/actuator/health

## Pushing to Docker Registry

### Configure registry in pom.xml
Update the `docker.image.prefix` property:
```xml
<docker.image.prefix>your-registry.io/your-namespace</docker.image.prefix>
```

### Build and push to registry
```bash
# Requires Docker registry authentication
./mvnw clean package jib:build
```

### Push to Docker Hub
```bash
# Login to Docker Hub first
docker login

# Update property to use your Docker Hub username
# Then run:
./mvnw clean package jib:build
```

## Troubleshooting

### Image not found
Make sure you built the images first:
```bash
./mvnw clean package jib:dockerBuild
```

### Database connection issues
Ensure PostgreSQL is running and accessible:
```bash
docker-compose up -d postgres
docker-compose ps
```

### View application logs
```bash
docker logs tracker-command
docker logs tracker-query
```

### Rebuild after code changes
```bash
# Rebuild images
./mvnw clean package jib:dockerBuild

# Restart services
docker-compose restart tracker-command tracker-query
```

## Environment Variables

You can customize these environment variables in docker-compose.yml or when running containers:

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL | jdbc:postgresql://postgres:5432/wanderer_db |
| `SPRING_DATASOURCE_USERNAME` | Database username | wanderer |
| `SPRING_DATASOURCE_PASSWORD` | Database password | password |
| `SPRING_PROFILES_ACTIVE` | Spring active profile | - |
| `JAVA_OPTS` | Additional JVM options | - |

Example with custom JVM options:
```yaml
environment:
  - JAVA_OPTS=-Xmx1g -Xms512m
```

## Development Workflow

1. **Make code changes**
2. **Rebuild images**:
   ```bash
   ./mvnw clean package jib:dockerBuild -DskipTests
   ```
3. **Restart services**:
   ```bash
   docker-compose restart tracker-command tracker-query
   ```
4. **Check logs**:
   ```bash
   docker-compose logs -f
   ```

## Quick Reference

```bash
# Full rebuild and restart
./mvnw clean package jib:dockerBuild && docker-compose up -d

# View all running containers
docker-compose ps

# Stop everything
docker-compose down

# Clean restart (with fresh database)
docker-compose down -v && ./mvnw clean package jib:dockerBuild && docker-compose up -d
```
