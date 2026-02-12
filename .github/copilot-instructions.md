# GitHub Copilot Instructions for Tracker Backend

## Project Overview

This is a Trip Tracker application for tracking a pilgrimage to Santiago de Compostela. It's built using Spring Boot 3.5.6 with Java 21, implementing CQRS (Command Query Responsibility Segregation) architecture in a multi-module Maven project.

### Purpose
The system receives location updates from mobile devices (via OwnTracks or custom Android app) and provides real-time tracking, messaging, achievements, and weather information for friends, family, and guests following the journey.

## Code Standards

### Required Before Each Commit
- Run `mvn spotless:apply` before committing any changes to ensure proper code formatting
- This will run Google Java Format (AOSP style) on all Java files to maintain consistent style
- Run `mvn clean verify` to ensure all tests pass and code coverage meets requirements (80%+)

### Development Flow
- **Build**: `mvn clean install` (builds all modules)
- **Build specific module**: `mvn clean install -pl <module-name>` (e.g., `mvn clean install -pl tracker-command`)
- **Test**: `mvn test` (runs unit tests)
- **Integration Test**: `mvn verify` (runs unit and integration tests)
- **Format Code**: `mvn spotless:apply` (formats all Java code)
- **Check Formatting**: `mvn spotless:check` (validates code formatting without changing files)
- **Skip Tests**: `mvn clean install -DskipTests` (only when necessary for faster builds)
- **Coverage Report**: `mvn clean verify` (generates JaCoCo coverage reports at `{module}/target/site/jacoco/index.html`)

## Repository Structure

### Multi-Module Maven Project
```
tracker-backend/
├── commons/                  # Shared domain entities, DTOs, and CQRS infrastructure
├── tracker-auth/            # Authentication service (JWT tokens) - Port 8083
├── tracker-command/         # Write operations (location updates, messages, trip management) - Port 8081
├── tracker-query/           # Read operations (location history, achievements, weather) - Port 8082
├── docs/                    # Documentation (DOCKER.md, CI-CD.md)
├── .github/                 # GitHub Actions workflows and this instructions file
│   ├── workflows/          # CI/CD pipeline definitions
│   └── copilot-instructions.md
├── pom.xml                  # Parent POM with dependency management
└── docker-compose.yml       # Docker Compose configuration
```

### Module-Specific Package Structure
Each module follows this standardized structure:
```
com.tomassirio.wanderer.{module}/
├── controller/      # REST API controllers
├── service/         # Business logic services  
├── repository/      # JPA repositories
├── entity/          # JPA entities
├── dto/             # Data Transfer Objects
├── mapper/          # MapStruct mappers
├── config/          # Configuration classes
├── exception/       # Custom exceptions
└── constants/       # Constants and enums
```

## Architecture

### CQRS Multi-Module Structure
- **commons**: Shared domain entities, DTOs, and CQRS infrastructure
- **tracker-auth**: Authentication service (JWT tokens) - Port 8083
- **tracker-command**: Write operations (location updates, messages, trip management) - Port 8081
- **tracker-query**: Read operations (location history, achievements, weather) - Port 8082

### Technology Stack
- **Backend Framework**: Spring Boot 3.5.6
- **Language**: Java 21
- **Build Tool**: Maven 3.6+
- **Database**: PostgreSQL 16 with Liquibase migrations
- **Security**: JWT Bearer tokens
- **API Documentation**: SpringDoc OpenAPI (Swagger UI)
- **DTO Mapping**: MapStruct
- **Code Quality**: Spotless with Google Java Format (AOSP style)
- **Testing**: JUnit 5, Cucumber for BDD, JaCoCo for coverage
- **Containerization**: Docker with Jib Maven Plugin
- **Orchestration**: Kubernetes + Helm charts

## Key Guidelines

1. **Java 21 Required**: This project uses Java 21. Ensure your local environment has JDK 21 installed.
2. **Before making changes**: Run `mvn clean install` to ensure the project builds successfully
3. **After making changes**: Run `mvn clean verify` to ensure tests pass and coverage requirements are met
4. **Code Formatting**: Always run `mvn spotless:apply` before committing to maintain code style consistency
5. **Write Tests**: Maintain minimum 80% code coverage (enforced by JaCoCo). Write unit tests for service layer methods and integration tests for controllers and repositories.
6. **Document APIs**: Use OpenAPI annotations (`@Operation`, `@ApiResponse`, etc.) for all REST endpoints
7. **Follow CQRS**: Keep write operations in tracker-command module, read operations in tracker-query module
8. **Use DTOs**: Never expose JPA entities directly in API responses. Use MapStruct for entity-to-DTO conversions.
9. **Security First**: Always consider authentication and authorization requirements. Use `@PreAuthorize` annotations for method-level security.
10. **Review Logs**: Use appropriate logging levels (DEBUG, INFO, WARN, ERROR). Never log sensitive data (passwords, tokens).

## Coding Guidelines

### Code Style
- Follow **Google Java Format (AOSP style)** for all Java code
- Code formatting is enforced via Spotless Maven plugin
- Run `mvn spotless:apply` to format code before committing
- All code is automatically formatted during Maven builds

### Java Conventions
- Use Java 21 features appropriately
- Use Lombok annotations to reduce boilerplate code
- Always use constructor-based dependency injection with `@RequiredArgsConstructor`
- Use MapStruct for entity-to-DTO conversions
- Follow Spring Boot best practices

### Package Structure
```
com.tomassirio.wanderer.{module}
├── controller/      # REST API controllers
├── service/         # Business logic services  
├── repository/      # JPA repositories
├── entity/          # JPA entities
├── dto/             # Data Transfer Objects
├── mapper/          # MapStruct mappers
├── config/          # Configuration classes
├── exception/       # Custom exceptions
└── constants/       # Constants and enums
```

### Naming Conventions
- **Classes**: PascalCase (e.g., `TripService`, `UserController`)
- **Methods**: camelCase (e.g., `createTrip()`, `findUserById()`)
- **Variables**: camelCase (e.g., `userId`, `tripDetails`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_MESSAGE_LENGTH`)
- **Packages**: lowercase, no underscores (e.g., `controller`, `service`)

### REST API Conventions
- Use RESTful URL patterns: `/api/1/{resource}`
- HTTP Methods: GET (read), POST (create), PUT (update), PATCH (partial update), DELETE (delete)
- Return appropriate HTTP status codes (200, 201, 204, 400, 401, 403, 404, 500)
- Use DTOs for all API requests and responses (never expose entities directly)
- Document all endpoints with OpenAPI annotations (`@Operation`, `@ApiResponse`, etc.)

### Database Conventions
- Use UUID for all entity IDs
- Use Liquibase for database migrations (YAML format)
- Store complex data structures as JSONB (e.g., `GeoLocation`, `Reactions`)
- Use `@CreationTimestamp` and `@UpdateTimestamp` for audit fields
- Always use JPA relationships appropriately (`@OneToMany`, `@ManyToOne`, etc.)

### Security
- JWT Bearer tokens for authentication
- Use Spring Security for authorization
- Role-based access control (ADMIN, USER)
- HTTPS mandatory in production
- Never log sensitive data (passwords, tokens)

### Testing
- Write unit tests for all service layer methods
- Write integration tests for repository and controller layers
- Use Cucumber for BDD acceptance tests
- Maintain minimum 80% code coverage (enforced by JaCoCo)
- Test files follow naming convention: `*Test.java` (unit), `*IT.java` (integration)
- Exclude DTOs, entities, config classes, and generated code from coverage

### Error Handling
- Use `@RestControllerAdvice` for global exception handling
- Return meaningful error messages with appropriate HTTP status codes
- Use custom exception classes in the `exception` package
- Log errors with appropriate severity levels

## Running Applications Locally

### Prerequisites
- Java 21 (required)
- Maven 3.6+ (required)
- Docker (optional, for containerized deployment)
- PostgreSQL 16 (required for local database, or use Docker Compose)

### Start Individual Services

```bash
# Auth Service (Port 8083)
mvn spring-boot:run -pl tracker-auth

# Command Service (Port 8081)
mvn spring-boot:run -pl tracker-command

# Query Service (Port 8082)
mvn spring-boot:run -pl tracker-query
```

### Docker Deployment

```bash
# Build Docker images with Jib
mvn clean compile jib:dockerBuild -pl tracker-command
mvn clean compile jib:dockerBuild -pl tracker-query
mvn clean compile jib:dockerBuild -pl tracker-auth

# Run with Docker Compose
docker-compose up
```

See [docs/DOCKER.md](../docs/DOCKER.md) for detailed Docker deployment guide.

## API Documentation

### Swagger UI URLs
- Auth Service: http://localhost:8083/swagger-ui.html
- Command Service: http://localhost:8081/swagger-ui.html
- Query Service: http://localhost:8082/swagger-ui.html

### Comprehensive API Documentation
Full API documentation is available in the [GitHub Wiki](https://github.com/tomassirio/tracker-backend/wiki), including:
- Getting Started Guide
- Authentication API
- User API
- Trip API
- Trip Plan API
- Trip Update API
- Comment API
- Security and Authorization

## Data Model

### Key Entities
- **User**: Authenticated users with username and ID
- **Trip**: Main trip entity with settings, details, and relationships to updates/comments
- **TripPlan**: Route planning with start/end locations and dates
- **TripUpdate**: Location updates with battery, message, and reactions
- **Comment**: Comments on trips with support for replies (one level of nesting)
- **Reactions**: JSONB structure for reaction counts (heart, smiley, sad, laugh, anger)

### Enums
- **TripVisibility**: PUBLIC, PRIVATE, PROTECTED
- **TripStatus**: CREATED, IN_PROGRESS, PAUSED, FINISHED
- **TripPlanType**: SIMPLE, MULTI_DAY
- **ReactionType**: HEART, SMILEY, SAD, LAUGH, ANGER

## Module Responsibilities

### Commons Module
- Shared domain entities and value objects
- DTOs and MapStruct mappers
- CQRS infrastructure (commands, queries, handlers)
- Custom exceptions
- Common utilities and constants

### Tracker-Auth Module
- User registration and authentication
- JWT token generation and validation
- Login/register endpoints
- Security configuration

### Tracker-Command Module
- Write operations: Create, Update, Delete
- Trip management (CRUD, status, visibility)
- Trip Plan management
- Trip Update creation (location tracking)
- Comment creation and reactions
- Database writes via JPA repositories

### Tracker-Query Module
- Read operations: Query and List
- User queries (by ID, username, current user)
- Trip queries (by ID, all, user's trips)
- Optimized read models
- Read-only database access

## CI/CD

### GitHub Actions Workflows
- **ci.yml**: Feature branch builds and tests
- **merge.yml**: Main branch releases (version bump, tagging, GitHub releases)
- **docker-build.yml**: Docker image builds
- **docker-publish.yml**: Docker image publishing to GHCR

### Versioning
- Semantic versioning with Maven versions plugin
- Automatic version bumping on merge to main
- Git tags created for each release (e.g., `v0.3.7`)

## Dependencies Management

### Adding Dependencies
- Add to `<dependencyManagement>` in parent `pom.xml` for version control
- Add to module-specific `pom.xml` without version (inherited from parent)
- Keep dependency versions centralized in parent POM properties

### Key Dependencies
- Spring Boot Starter Web, Data JPA, Security, Validation
- PostgreSQL Driver
- Liquibase Core
- Lombok
- MapStruct
- SpringDoc OpenAPI
- JJWT (JWT tokens)
- JaCoCo (code coverage)
- Cucumber (BDD testing)

### Best Practices for Contributors

1. **Before making changes**: Run `mvn clean install` to ensure the project builds
2. **After making changes**: Run `mvn clean verify` to ensure tests pass
3. **Format code**: Always run `mvn spotless:apply` before committing
4. **Write tests**: Maintain high code coverage (target 80%+)
5. **Document APIs**: Use OpenAPI annotations for all REST endpoints
6. **Follow CQRS**: Keep write operations in command module, read operations in query module
7. **Use DTOs**: Never expose JPA entities directly in API responses
8. **Security**: Always consider authentication and authorization requirements
9. **Review logs**: Use appropriate logging levels (DEBUG, INFO, WARN, ERROR)
10. **Check wiki**: Refer to GitHub Wiki for detailed API documentation

## Important Constraints and Boundaries

### What You Should Never Do
- **Never modify or expose sensitive data**: Do not log passwords, JWT tokens, or other sensitive information
- **Never expose JPA entities directly**: Always use DTOs for API requests and responses
- **Never skip code formatting**: Always run `mvn spotless:apply` before committing
- **Never commit without tests**: Ensure adequate test coverage for new functionality
- **Never modify database schema directly**: Use Liquibase migrations (XML format)
- **Never bypass security**: Always use proper authentication and authorization
- **Never commit to main directly**: All changes must go through pull requests
- **Never remove or modify existing tests**: Unless fixing a broken test related to your changes
- **Never store secrets in code**: Use environment variables or configuration management

### What You Should Always Do
- **Always use constructor-based dependency injection** with `@RequiredArgsConstructor`
- **Always use MapStruct** for entity-to-DTO conversions
- **Always use UUID** for all entity IDs
- **Always use Liquibase** for database migrations (YAML format)
- **Always document endpoints** with OpenAPI annotations
- **Always write unit tests** for service layer methods
- **Always write integration tests** for repository and controller layers
- **Always follow Google Java Format (AOSP style)** enforced by Spotless
- **Always use appropriate HTTP status codes** (200, 201, 204, 400, 401, 403, 404, 500)

## Additional Resources

- [README.md](../README.md) - Project overview and getting started
- [GitHub Wiki](https://github.com/tomassirio/tracker-backend/wiki) - Complete API documentation
- [Docker Guide](../docs/DOCKER.md) - Docker deployment guide
- [CI/CD Guide](../docs/CI-CD.md) - GitHub Actions workflows
- [Release Notes](https://github.com/tomassirio/tracker-backend/releases) - Version history

## Questions or Issues?

For questions, suggestions, or issues, please open a GitHub issue in the repository.

---

**¡Buen Camino!** 🥾⛪
