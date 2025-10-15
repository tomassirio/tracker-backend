# Trip Tracker Application

A comprehensive tracking system for my pilgrimage to Santiago de Compostela, built with CQRS architecture using Spring Boot and Java 21.

## 📚 Documentation

- **[Docker Guide](docs/DOCKER.md)** - Complete guide for building and running with Docker
- **[CI/CD Workflows](docs/CI-CD.md)** - GitHub Actions workflows and automation
- **[Release Notes](https://github.com/tomassirio/tracker-backend/releases)** - Version history and changelog

## 📖 Description

As part of my trip to Santiago de Compostela, I'm creating a set of applications for friends, family, and guests to check on my status. The journey will be approximately 48 days long, walking 50 km per day from Utrecht to Santiago de Compostela.

The system receives location updates from my phone via OwnTracks (or a custom Android app) and provides real-time tracking, messaging, achievements, and weather information.

## 🏗️ Architecture

### CQRS Multi-Module Structure
- **Commons**: Shared domain entities, DTOs, and CQRS infrastructure
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

## 🌐 API Endpoints

### Authentication API (tracker-auth) - Port 8083
```
POST /api/1/auth/login      → Login with username/password, returns JWT token
POST /api/1/auth/register   → Register new user, returns JWT token
```

### User APIs

#### Command (tracker-command) - Port 8081
```
POST /api/1/users           → Create new user
```

#### Query (tracker-query) - Port 8082
```
GET /api/1/users/{id}              → Get user by ID (Auth: ADMIN, USER)
GET /api/1/users/username/{username} → Get user by username (Public)
GET /api/1/users/me                → Get current authenticated user profile
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

- JWT Bearer token authentication for OwnTracks and frontend
- HTTPS-only communication
- API keys or per-device credentials
- Optional IP filtering for ingestion endpoints

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
- ✅ Security implementation (JWT authentication, Role-based authorization)
- ✅ User management (Create, Query by ID/username, Current user context)
- ✅ Trip CRUD operations (Create, Read, Update, Delete)
- ✅ Trip status and visibility management
- ✅ Trip Plans (Create, Update, Delete)
- ✅ Trip Updates with location tracking
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

This project is for personal use during my Santiago de Compostela pilgrimage.

---

**¡Buen Camino!** 🥾⛪
