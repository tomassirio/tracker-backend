# API Overview

## Introduction

The Trip Tracker Backend provides a comprehensive RESTful API for managing trips, tracking locations, and engaging with trip updates through comments and reactions. The system is built using CQRS (Command Query Responsibility Segregation) architecture to optimize read and write operations independently.

## Architecture

### CQRS Pattern

The application is split into three separate services:

1. **tracker-auth (Port 8083)**: Handles authentication and user registration
2. **tracker-command (Port 8081)**: Processes all write operations (commands)
3. **tracker-query (Port 8082)**: Handles all read operations (queries)

### Why CQRS?

- **Performance**: Read and write operations can be optimized independently
- **Scalability**: Query and command services can be scaled separately based on load
- **Flexibility**: Different data models for reading and writing
- **Clear Separation**: Commands change state, queries don't

## Base URLs

When running locally with default configuration:

```
Authentication Service: http://localhost:8083
Command Service:        http://localhost:8081
Query Service:          http://localhost:8082
```

All API endpoints are versioned and prefixed with `/api/1`.

## Request/Response Format

### Content Type
All requests and responses use JSON format:
```
Content-Type: application/json
```

### Authentication Header
Most endpoints require JWT authentication:
```
Authorization: Bearer <jwt-token>
```

### Standard Response Structure

#### Success Response
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "My Trip",
  ...additional fields
}
```

#### Error Response
```json
{
  "timestamp": "2025-10-16T10:30:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for field 'name'",
  "path": "/api/1/trips"
}
```

## HTTP Methods

The API follows REST conventions:

| Method | Purpose | Idempotent |
|--------|---------|------------|
| `GET` | Retrieve resources | Yes |
| `POST` | Create new resources | No |
| `PUT` | Update entire resource | Yes |
| `PATCH` | Partial update | No |
| `DELETE` | Remove resource | Yes |

## HTTP Status Codes

The API uses standard HTTP status codes:

| Code | Meaning | When Used |
|------|---------|-----------|
| `200 OK` | Success | Successful GET, PUT, PATCH, or DELETE |
| `201 Created` | Resource created | Successful POST |
| `204 No Content` | Success with no body | Successful DELETE |
| `400 Bad Request` | Invalid input | Validation errors |
| `401 Unauthorized` | Not authenticated | Missing or invalid token |
| `403 Forbidden` | Not authorized | Insufficient permissions |
| `404 Not Found` | Resource not found | Invalid ID or resource |
| `409 Conflict` | Resource conflict | Duplicate resource |
| `500 Internal Server Error` | Server error | Unexpected error |

## Data Types

### Common Types

- **UUID**: Universally unique identifier, format: `550e8400-e29b-41d4-a716-446655440000`
- **ISO 8601 Timestamp**: `2025-10-16T10:30:00.000Z`
- **GeoLocation**: 
  ```json
  {
    "latitude": 41.8919,
    "longitude": -87.6051,
    "altitude": 200.5
  }
  ```

### Enumerations

#### Trip Visibility
- `PUBLIC`: Visible to everyone
- `PRIVATE`: Only visible to owner
- `PROTECTED`: Visible to authenticated users

#### Trip Status
- `CREATED`: Trip has been created
- `IN_PROGRESS`: Trip is currently active
- `PAUSED`: Trip is temporarily paused
- `FINISHED`: Trip has been completed

#### Trip Plan Type
- `SIMPLE`: Simple point-to-point plan
- `MULTI_DAY`: Multi-day itinerary with waypoints

#### Reaction Type
- `HEART`: ❤️
- `SMILEY`: 😊
- `SAD`: 😢
- `LAUGH`: 😂
- `ANGER`: 😠

## Pagination

Some endpoints support pagination (to be implemented):
```
GET /api/1/trips?page=0&size=20&sort=createdAt,desc
```

Parameters:
- `page`: Page number (0-indexed)
- `size`: Items per page
- `sort`: Sort field and direction

## Rate Limiting

Currently, there are no rate limits implemented. This may change in future versions.

## Versioning

The API is versioned through the URL path (`/api/1`). Breaking changes will increment the version number.

## Interactive Documentation

Each service provides interactive Swagger/OpenAPI documentation:

- Auth: http://localhost:8083/swagger-ui.html
- Command: http://localhost:8081/swagger-ui.html
- Query: http://localhost:8082/swagger-ui.html

These interfaces allow you to:
- Explore all endpoints
- View request/response schemas
- Test endpoints directly from the browser
- See example payloads

## Next Steps

- [Get Started with APIs](Getting-Started-with-APIs) - Quick start guide
- [Authentication API](Authentication-API) - Learn about authentication
- [User API](User-API) - Manage users
- [Trip API](Trip-API) - Work with trips
