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
- Stores data in PostgreSQL
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

### Trip
- `id`, `name`, `startDate`, `endDate`, `totalDistance`
- `startingLocation`, `endingLocation`

### Location
- `id`, `tripId`, `lat`, `lon`, `timestamp`
- `alt`, `acc`, `battery`, `source`

### Message
- `id`, `tripId`, `text`, `timestamp`
- `locationId` (optional reference)

### Achievement
- `id`, `tripId`, `type`, `unlockedAt`
- `description`, `distanceThreshold`

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

### Command Side (Write Operations) - Port 8081
```
POST /api/1/{tripId}/location     → Submit location update
POST /api/1/{tripId}/messages     → Submit status message
```

### Query Side (Read Operations) - Port 8082
```
GET /api/1/{tripId}/location/{locationId}  → Fetch specific location
GET /api/1/{tripId}/locations              → Location history (with date filters)
GET /api/1/{tripId}/location/latest        → Latest position
GET /api/1/{tripId}/messages               → List status messages
GET /api/1/{tripId}/achievements           → Unlocked achievements
GET /api/1/{tripId}/weather/latest         → Live weather data
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
- ⏳ Domain entities and CQRS infrastructure
- ⏳ REST API implementation
- ⏳ Database integration
- ⏳ Security implementation
- ⏳ Weather API integration
- ⏳ Achievement system
- ⏳ Docker configuration
- ⏳ Kubernetes manifests

## 🤝 Contributing

This is a personal project for my pilgrimage, but suggestions and improvements are welcome!

## 📝 License

This project is for personal use during my Santiago de Compostela pilgrimage.

---

**¡Buen Camino!** 🥾⛪
