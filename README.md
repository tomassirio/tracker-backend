# Trip Tracker Application

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen?logo=spring&logoColor=white)
![Version](https://img.shields.io/badge/version-0.5.0-blue)
![Build Status](https://img.shields.io/github/actions/workflow/status/tomassirio/tracker-backend/merge.yml?branch=main&label=build)
![License](https://img.shields.io/badge/license-MIT-green)
![Coverage](https://img.shields.io/badge/coverage-48%25-orange)

![PostgresSQL](https://img.shields.io/badge/PostgreSQL-16-336791?logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/docker-enabled-2496ED?logo=docker&logoColor=white)
![Kubernetes](https://img.shields.io/badge/kubernetes-ready-326CE5?logo=kubernetes&logoColor=white)
![Architecture](https://img.shields.io/badge/architecture-CQRS-blueviolet)

A comprehensive tracking system for my pilgrimage to Santiago de Compostela, built with CQRS architecture using Spring Boot and Java 21.

## 📚 Documentation

- **[API Documentation Wiki](https://github.com/tomassirio/tracker-backend/wiki)** - Comprehensive API reference and guides
  - All wiki source files are in [`docs/wiki/`](docs/wiki/)
  - To publish to GitHub Wiki: `./setup-wiki.sh`
  - See [`docs/wiki/SETUP-INSTRUCTIONS.md`](docs/wiki/SETUP-INSTRUCTIONS.md) for details
- **[Docker Guide](docs/DOCKER.md)** - Complete guide for building and running with Docker
- **[CI/CD Workflows](docs/CI-CD.md)** - GitHub Actions workflows and automation
- **[Release Notes](https://github.com/tomassirio/tracker-backend/releases)** - Version history and changelog

## 📖 Description

As part of my trip to Santiago de Compostela, I'm creating a set of applications for friends, family, and guests to check on my status. The journey will be approximately 48 days long, walking 50 km per day from Utrecht to Santiago de Compostela.

The system receives location updates from my phone via OwnTracks (or a custom Android app) and provides real-time tracking, messaging, achievements, and weather information.

## 🏗️ Architecture

### CQRS Multi-Module Structure
- **Commons**: Shared domain entities, DTOs, and CQRS infrastructure
- **Tracker-Auth**: Authentication and authorization service - Port 8083
  - User registration and login
  - JWT token generation and validation
  - Refresh token management with rotation
  - Password reset and change functionality
  - Token blacklisting for logout
- **Tracker-Command**: Write operations (location updates, messages) - Port 8081
- **Tracker-Query**: Read operations (location history, achievements, weather) - Port 8082

### Technology Stack
- **Backend**: Java 21 with Spring Boot
- **Database**: PostgreSQL with Liquibase migrations
- **Architecture**: CQRS (Command Query Responsibility Segregation)
- **Containerization**: Docker with Jib Maven Plugin
- **Orchestration**: Kubernetes + Helm charts
- **Observability**: Prometheus + Grafana, Loki/ELK for logging
- **Security**: JWT tokens, HTTPS mandatory

## 🧩 Applications

### Tracker-Backend (This Repository)
- Receives REST calls with location and OwnTracks metadata
- Stores data in PostgresSQL
- Exposes REST API for frontend queries
- Supports status messages and weather integration
- Automatically unlocks achievements based on milestones

### Tracker-Frontend (Separate Repository)
- Interactive maps showing Camino path and current position
- Daily route planning view
- Messages feed and achievements display
- Weather information integration

### Infrastructure
- Docker containers deployed to home Kubernetes cluster
- Managed with Helm charts
- Exposed via router port-forwarding
- Proxied through Cloudflare for custom domain and security

## 🗄️ Data Model

### User
- `id` (UUID) 
- `username` (unique)
- Represents authenticated users in the system

### Friendships & Following
- **Friend Requests**: Users can send friend requests to other users
- **Friendships**: Bidirectional relationships created when friend requests are accepted
- **User Follows**: Users can follow other users independently of friendships
- Friends can see each other's PROTECTED trips
- Trips from followed users are prioritized in public trip lists

### Trip
- `id` (UUID)
- `name`
- `userId` (owner)
- `tripSettings` (visibility, status) 
- `tripDetails` (start/end dates, locations, distance)
- `tripPlanId` (optional reference to trip plan)
- `comments` (one-to-many relationship)
- `tripUpdates` (one-to-many relationship)
- `creationTimestamp`
- `enabled`

### TripPlan
- `id` (UUID) 
- `name`
- `planType` (SIMPLE, MULTI_DAY)
- `userId` (owner)
- `startLocation` (GeoLocation)
- `endLocation` (GeoLocation)
- `startDate`
- `endDate`
- `metadata` (JSONB for flexible plan data)
- `creationTimestamp`
- `updateTimestamp`

### TripUpdate
- `id` (UUID)
- `tripId`
- `location` (GeoLocation with lat/lon/altitude in JSONB)
- `battery`
- `message`
- `reactions` (Reactions JSONB)
- `timestamp`

### Comment
- `id` (UUID)
- `tripId` (belongs to a trip)
- `userId` (comment author)
- `parentCommentId` (nullable - for nested replies)
- `message` (TEXT, max 1000 characters)
- `reactions` (Reactions JSONB)
- `replies` (one-to-many self-referential relationship)
- `timestamp`
- Supports one level of nesting (comments can have replies, but replies cannot have replies)

### Reactions
A JSONB structure tracking reaction counts:
- `heart` (integer counter)
- `smiley` (integer counter)
- `sad` (integer counter)
- `laugh` (integer counter)
- `anger` (integer counter)

### Supporting Types
- **GeoLocation**: Latitude and longitude coordinates
- **TripSettings**: Visibility (PUBLIC, PRIVATE, PROTECTED) and Status
- **TripDetails**: Additional trip information (dates, locations, distance)
- **TripVisibility**: PUBLIC, PRIVATE, PROTECTED
- **TripStatus**: CREATED, IN_PROGRESS, PAUSED, FINISHED
- **TripPlanType**: SIMPLE, MULTI_DAY
- **ReactionType**: HEART, SMILEY, SAD, LAUGH, ANGER

## 🏆 Achievements System

### Distance Milestones
- 100 km, 500 miles, 1000 miles
- Camino Francés completion
- Full pilgrimage completion

### Event Triggers
- Enter new country
- Halfway mark reached
- Santiago arrival

## 📖 API Documentation

For comprehensive API documentation, visit the **[API Documentation Wiki](https://github.com/tomassirio/tracker-backend/wiki)**:

- **[Getting Started Guide](https://github.com/tomassirio/tracker-backend/wiki/Getting-Started-with-APIs)** - Quick start with examples
- **[Authentication API](https://github.com/tomassirio/tracker-backend/wiki/Authentication-API)** - User registration and login
- **[User API](https://github.com/tomassirio/tracker-backend/wiki/User-API)** - User management
- **[Trip API](https://github.com/tomassirio/tracker-backend/wiki/Trip-API)** - Trip CRUD operations
- **[Trip Plan API](https://github.com/tomassirio/tracker-backend/wiki/Trip-Plan-API)** - Route planning
- **[Trip Update API](https://github.com/tomassirio/tracker-backend/wiki/Trip-Update-API)** - Location tracking
- **[Comment API](https://github.com/tomassirio/tracker-backend/wiki/Comment-API)** - Comments and reactions
- **[Security Guide](https://github.com/tomassirio/tracker-backend/wiki/Security-and-Authorization)** - Authentication and authorization

The Wiki provides detailed documentation with request/response examples, error handling, and complete workflows.

### Interactive API Documentation (Swagger UI)

Each service also provides interactive API documentation:

- **Auth Service**: http://localhost:8083/swagger-ui.html
- **Command Service**: http://localhost:8081/swagger-ui.html
- **Query Service**: http://localhost:8082/swagger-ui.html

## 🌐 API Endpoints Overview

Below is a quick reference of available endpoints. For detailed documentation, see the [Wiki](https://github.com/tomassirio/tracker-backend/wiki).

### Authentication API (tracker-auth) - Port 8083
```
POST /api/1/auth/login           → Login with username/password, returns access & refresh tokens
POST /api/1/auth/register        → Register new user, returns access & refresh tokens
POST /api/1/auth/logout          → Logout user, invalidates access token and revokes refresh tokens (Auth: USER, ADMIN)
POST /api/1/auth/refresh         → Exchange refresh token for new access & refresh tokens
POST /api/1/auth/password/reset  → Initiate password reset, generates reset token
PUT  /api/1/auth/password/reset  → Complete password reset with token
PUT  /api/1/auth/password/change → Change password for authenticated user (Auth: USER, ADMIN)
```

### User APIs

#### Command (tracker-command) - Port 8081
```
POST /api/1/users                              → Create new user
POST /api/1/users/friends/requests             → Send a friend request
POST /api/1/users/friends/requests/{id}/accept → Accept a friend request
POST /api/1/users/friends/requests/{id}/decline → Decline a friend request
POST /api/1/users/follows                      → Follow a user
DELETE /api/1/users/follows/{id}               → Unfollow a user
```

#### Query (tracker-query) - Port 8082
```
GET /api/1/users/{id}                          → Get user by ID (Auth: ADMIN, USER)
GET /api/1/users/username/{username}           → Get user by username (Public)
GET /api/1/users/me                            → Get current authenticated user profile
GET /api/1/users/friends                       → Get user's friends list
GET /api/1/users/friends/requests/received     → Get pending received friend requests
GET /api/1/users/friends/requests/sent         → Get pending sent friend requests
GET /api/1/users/follows/following             → Get users that current user follows
GET /api/1/users/follows/followers             → Get users that follow current user
```

### Trip APIs

#### Command (tracker-command) - Port 8081
```
POST   /api/1/trips                    → Create new trip
PUT    /api/1/trips/{id}               → Update trip
PATCH  /api/1/trips/{id}/visibility    → Change trip visibility (PUBLIC/PRIVATE/PROTECTED)
PATCH  /api/1/trips/{id}/status        → Change trip status (PLANNING/ACTIVE/COMPLETED/CANCELLED)
DELETE /api/1/trips/{id}               → Delete trip
POST   /api/1/trips/{tripId}/updates   → Create trip update (location, battery, message)
```

#### Query (tracker-query) - Port 8082
```
GET /api/1/trips/{id}      → Get trip by ID
GET /api/1/trips           → Get all trips (Admin only)
GET /api/1/trips/me        → Get current user's trips
```

### Trip Plan APIs (tracker-command) - Port 8081
```
POST   /api/1/trips/plans   → Create trip plan
PUT    /api/1/trips/plans/{planId}   → Update trip plan
DELETE /api/1/trips/plans/{planId}   → Delete trip plan
```

### Comment APIs

#### Command (tracker-command) - Port 8081
```
POST   /api/1/trips/{tripId}/comments              → Create comment or reply on a trip
                                                     (use parentCommentId in body for replies)
POST   /api/1/comments/{commentId}/reactions       → Add a reaction to a comment
                                                     (HEART, SMILEY, SAD, LAUGH, ANGER)
DELETE /api/1/comments/{commentId}/reactions       → Remove a reaction from a comment
```

### Legacy/Planned Endpoints
```
POST /api/1/{tripId}/location          → Submit location update (planned)
POST /api/1/{tripId}/messages          → Submit status message (planned)
GET  /api/1/{tripId}/location/{locationId} → Fetch specific location (planned)
GET  /api/1/{tripId}/locations         → Location history with filters (planned)
GET  /api/1/{tripId}/location/latest   → Latest position (planned)
GET  /api/1/{tripId}/messages          → List status messages (planned)
GET  /api/1/{tripId}/achievements      → Unlocked achievements (planned)
GET  /api/1/{tripId}/weather/latest    → Live weather data (planned)
```

## 📌 Functional Requirements

- ✅ Track and store location at configurable intervals
- ✅ Provide "latest position" and "trip history" endpoints
- ✅ Accept and display custom messages
- ✅ Fetch and display live weather data
- ✅ Unlock and store achievements automatically
- ✅ Show maps at multiple zoom levels

## 📈 Non-Functional Requirements

- **Reliability**: Offline queuing and retry for location updates
- **Performance**: Write operations <200ms latency, scalable queries
- **Availability**: Global exposure with minimal downtime
- **Security**: HTTPS mandatory, JWT token authentication
- **Privacy**: Location data visible only to authorized users

## 🔒 Security

### Authentication & Authorization
- **JWT Bearer Tokens**: Access tokens with 1-hour expiration (configurable)
- **Refresh Tokens**: Long-lived tokens (7 days) for obtaining new access tokens
- **Token Rotation**: New refresh token issued on each refresh for enhanced security
- **Token Blacklisting**: JTI-based blacklist for immediate logout functionality
- **Role-Based Access Control**: USER and ADMIN roles with method-level security
- **Password Security**: Bcrypt hashing with configurable strength

### Password Management
- **Password Reset**: Time-limited (1 hour), one-time-use reset tokens
- **Password Change**: Authenticated users can change passwords (revokes all refresh tokens)
- **Password Requirements**: Minimum 8 characters

### API Security
- HTTPS-only communication in production
- Method-level authorization with `@PreAuthorize` annotations
- Public endpoints: login, register, refresh, password reset
- Protected endpoints: logout, password change, all data access

### Token Storage
- All tokens hashed with SHA-256 before database storage
- Automatic cleanup of expired tokens
- Separate tables for refresh tokens, password reset tokens, and blacklist

## 🚨 Error Handling

- **400 Bad Request**: Invalid payloads with detailed error messages
- **401/403**: Unauthorized requests
- **500 Internal Server Error**: System errors
- **Offline Handling**: Client-side queuing with retry logic

## 🔍 Observability

- **Metrics**: Prometheus integration (request count, error rate, DB latency)
- **Health Checks**: Kubernetes liveness and readiness probes
- **Logging**: Centralized logging for debugging
- **Monitoring**: Grafana dashboards for system insights

## 🚀 Getting Started

### Prerequisites
- Java 21
- Maven 3.6+
- Docker (optional)

### Building the Project
```bash
# Build all modules
mvn clean install

# Build specific module
mvn clean install -pl commons
mvn clean install -pl tracker-command
mvn clean install -pl tracker-query
```

### Running the Applications

#### Auth Service (Port 8083)
```bash
mvn spring-boot:run -pl tracker-auth
```

#### Command Service (Port 8081)
```bash
mvn spring-boot:run -pl tracker-command
```

#### Query Service (Port 8082)
```bash
mvn spring-boot:run -pl tracker-query
```

### Testing
```bash
# Run all tests
mvn test

# Test specific module
mvn test -pl tracker-command
mvn test -pl tracker-query
```

## 🐳 Docker Deployment

### Building Docker Images
```bash
# Command service
mvn clean compile jib:dockerBuild -pl tracker-command

# Query service
mvn clean compile jib:dockerBuild -pl tracker-query
```

### Docker Compose (Coming Soon)
```yaml
# docker-compose.yml will include:
# - PostgreSQL database
# - tracker-command service
# - tracker-query service
# - Prometheus & Grafana
```

## ☸️ Kubernetes Deployment

Helm charts will be provided for:
- PostgreSQL database
- Command and Query services
- ConfigMaps and Secrets
- Service definitions and Ingress
- Monitoring stack (Prometheus/Grafana)

## 🖥️ Frontend Views

The companion frontend application will feature:
- **Dashboard**: Complete Camino route with current position
- **Day Plan**: Today's planned route
- **Surroundings**: Detailed local area view
- **Messages**: Live status updates feed
- **Achievements**: Progress badges and milestones
- **Weather**: Current conditions at location

## 📊 Project Status

- ✅ Multi-module CQRS structure
- ✅ Basic Spring Boot applications
- ✅ Application configuration
- ✅ Domain entities and CQRS infrastructure
- ✅ REST API implementation (User, Trip, TripPlan, TripUpdate)
- ✅ Database integration (PostgreSQL with JPA/Hibernate)
- ✅ Security implementation (JWT authentication with refresh tokens, Role-based authorization)
- ✅ Authentication service (Login, Register, Logout, Token refresh, Password reset/change)
- ✅ User management (Create, Query by ID/username, Current user context)
- ✅ Trip CRUD operations (Create, Read, Update, Delete)
- ✅ Trip status and visibility management
- ✅ Trip Plans (Create, Update, Delete)
- ✅ Trip Updates with location tracking
- ✅ Friend request system (Send, Accept, Decline)
- ✅ User friendships with bidirectional relationships
- ✅ User follow system
- ✅ Trip visibility based on friendships (PROTECTED trips)
- ✅ Trip prioritization based on follows
- ✅ Global exception handling
- ✅ Docker configuration (Jib plugin, docker-compose)
- ✅ CI/CD pipeline (GitHub Actions)
- ✅ API documentation (Swagger/OpenAPI)
- ✅ Unit and Integration tests
- ✅ MapStruct DTO mapping
- ✅ Code formatting (Spotless with Google Java Format)
- ⏳ Trip Updates Query API
- ⏳ Comments system (CRUD operations)
- ⏳ Reactions system
- ⏳ Weather API integration
- ⏳ Achievement system
- ⏳ Kubernetes manifests
- ⏳ Real-time updates (WebSocket/SSE)
- ⏳ Search and filtering
- ⏳ Pagination support

## 🤝 Contributing

This is a personal project for my pilgrimage, but suggestions and improvements are welcome!

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

You are free to use, modify, and distribute this software, including for commercial purposes.

---

**¡Buen Camino!** 🥾⛪
