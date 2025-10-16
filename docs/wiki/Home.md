# Trip Tracker Backend API Documentation

Welcome to the **Trip Tracker Backend** API documentation! This wiki provides comprehensive information about all available REST APIs in the system.

## 📚 Quick Navigation

### Getting Started
- **[API Overview](API-Overview)** - Introduction to the API architecture and general concepts
- **[Getting Started](Getting-Started-with-APIs)** - Quick start guide with examples
- **[Authentication](Authentication-API)** - How to authenticate and obtain JWT tokens

### API References
- **[User API](User-API)** - User management endpoints
- **[Trip API](Trip-API)** - Trip creation, updates, and queries
- **[Trip Plan API](Trip-Plan-API)** - Trip planning and route management
- **[Comment API](Comment-API)** - Comments and reactions on trips
- **[Trip Update API](Trip-Update-API)** - Location updates and tracking

### Reference Guides
- **[API Response Formats](API-Response-Formats)** - Common response structures and error handling
- **[Security & Authorization](Security-and-Authorization)** - Authentication, roles, and permissions

## 🏗️ Architecture Overview

The Trip Tracker Backend follows a **CQRS (Command Query Responsibility Segregation)** architecture with three main services:

| Service | Port | Purpose | Base Path |
|---------|------|---------|-----------|
| **tracker-auth** | 8083 | Authentication & user registration | `/api/1/auth` |
| **tracker-command** | 8081 | Write operations (Create, Update, Delete) | `/api/1` |
| **tracker-query** | 8082 | Read operations (Queries) | `/api/1` |

## 🔐 Authentication

All API endpoints (except registration and login) require JWT authentication. Include the token in the `Authorization` header:

```
Authorization: Bearer <your-jwt-token>
```

Get your token by calling the [Login](Authentication-API#login) endpoint.

## 🚀 Quick Example

Here's a quick example to get you started:

```bash
# 1. Register a new user
curl -X POST http://localhost:8083/api/1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"secret123"}'

# 2. Use the returned token to create a trip
curl -X POST http://localhost:8081/api/1/trips \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"My Camino","visibility":"PUBLIC"}'

# 3. Query your trips
curl -X GET http://localhost:8082/api/1/trips/me \
  -H "Authorization: Bearer <your-token>"
```

## 📖 API Documentation

For interactive API documentation with try-it-out functionality, access the Swagger UI:

- **Auth Service**: http://localhost:8083/swagger-ui.html
- **Command Service**: http://localhost:8081/swagger-ui.html
- **Query Service**: http://localhost:8082/swagger-ui.html

## 🤝 Support

For issues, questions, or contributions:
- **GitHub Issues**: [Report a bug or request a feature](https://github.com/tomassirio/tracker-backend/issues)
- **Source Code**: [View on GitHub](https://github.com/tomassirio/tracker-backend)

---

**Ready to get started?** Check out the [Getting Started Guide](Getting-Started-with-APIs)!
