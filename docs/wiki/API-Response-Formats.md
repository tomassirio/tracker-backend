# API Response Formats

This page documents the common response structures, data types, and error handling used throughout the API.

## Standard Response Structures

### Success Responses

#### Single Resource

When returning a single resource (user, trip, comment, etc.):

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "field1": "value1",
  "field2": "value2",
  ...
}
```

#### Resource Collection

When returning multiple resources:

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "field1": "value1"
  },
  {
    "id": "660e8400-e29b-41d4-a716-446655440000",
    "field1": "value1"
  }
]
```

#### No Content

For successful DELETE operations:
- **Status**: `204 No Content`
- **Body**: Empty

---

## Data Types

### UUID

Universally Unique Identifier in standard format:

```
550e8400-e29b-41d4-a716-446655440000
```

**Pattern**: `[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}`

### ISO 8601 Timestamps

Date and time in ISO 8601 format with UTC timezone:

```
2025-10-16T15:30:00Z
```

**Format**: `YYYY-MM-DDTHH:mm:ss.sssZ`

Examples:
- `2025-10-16T15:30:00Z` - UTC time
- `2025-10-16T15:30:00.123Z` - With milliseconds

### GeoLocation

Geographic coordinates object:

```json
{
  "latitude": 42.8805,
  "longitude": -8.5457,
  "altitude": 365.2
}
```

| Field | Type | Range | Description |
|-------|------|-------|-------------|
| latitude | number | -90 to 90 | Latitude in decimal degrees |
| longitude | number | -180 to 180 | Longitude in decimal degrees |
| altitude | number | Optional | Altitude in meters above sea level |

---

## Common DTOs

### TripDTO

```json
{
  "id": "660e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "My Camino Journey",
  "tripSettings": {
    "visibility": "PUBLIC",
    "status": "IN_PROGRESS"
  },
  "tripDetails": {
    "startLocation": {
      "latitude": 43.2630,
      "longitude": -2.9350
    },
    "endLocation": {
      "latitude": 42.8805,
      "longitude": -8.5457
    },
    "startDate": "2025-11-01T08:00:00Z",
    "endDate": "2025-12-15T18:00:00Z",
    "estimatedDistance": 780.5
  },
  "tripPlanId": "880e8400-e29b-41d4-a716-446655440000",
  "creationTimestamp": "2025-10-16T10:30:00Z",
  "enabled": true
}
```

### TripUpdateDTO

```json
{
  "id": "770e8400-e29b-41d4-a716-446655440000",
  "tripId": "660e8400-e29b-41d4-a716-446655440000",
  "location": {
    "latitude": 42.8805,
    "longitude": -8.5457,
    "altitude": 365.2
  },
  "battery": 85,
  "message": "Arrived in Santiago!",
  "reactions": {
    "heart": 12,
    "smiley": 5,
    "sad": 0,
    "laugh": 3,
    "anger": 0
  },
  "timestamp": "2025-10-16T15:30:00Z"
}
```

### CommentDTO

```json
{
  "id": "aa0e8400-e29b-41d4-a716-446655440000",
  "tripId": "660e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "parentCommentId": null,
  "message": "What an amazing journey!",
  "reactions": {
    "heart": 5,
    "smiley": 2,
    "sad": 0,
    "laugh": 1,
    "anger": 0
  },
  "replies": [],
  "timestamp": "2025-10-16T15:30:00Z"
}
```

### TripPlanDTO

```json
{
  "id": "880e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Camino Francés Plan",
  "planType": "MULTI_DAY",
  "startLocation": {
    "latitude": 43.1631,
    "longitude": -1.2350
  },
  "endLocation": {
    "latitude": 42.8805,
    "longitude": -8.5457
  },
  "startDate": "2025-11-01T08:00:00Z",
  "endDate": "2025-12-15T18:00:00Z",
  "metadata": {
    "dailyDistance": 25,
    "waypoints": [...]
  },
  "creationTimestamp": "2025-10-16T10:30:00Z",
  "updateTimestamp": "2025-10-16T10:30:00Z"
}
```

### ReactionsDTO

```json
{
  "heart": 12,
  "smiley": 5,
  "sad": 1,
  "laugh": 8,
  "anger": 0
}
```

All reaction counts are non-negative integers.

---

## Enumerations

### TripVisibility

```
PUBLIC      - Visible to everyone (including unauthenticated users)
PRIVATE     - Only visible to the trip owner
PROTECTED   - Visible to authenticated users
```

### TripStatus

```
CREATED      - Trip has been created but not started
IN_PROGRESS  - Trip is currently active
PAUSED       - Trip is temporarily paused
FINISHED     - Trip has been completed
```

### TripPlanType

```
SIMPLE       - Simple point-to-point plan
MULTI_DAY    - Multi-day itinerary with waypoints
```

### ReactionType

```
HEART    - ❤️
SMILEY   - 😊
SAD      - 😢
LAUGH    - 😂
ANGER    - 😠
```

---

## Error Responses

### Standard Error Format

All error responses follow this structure:

```json
{
  "timestamp": "2025-10-16T15:30:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Detailed error message",
  "path": "/api/1/trips"
}
```

| Field | Type | Description |
|-------|------|-------------|
| timestamp | ISO 8601 | When the error occurred |
| status | integer | HTTP status code |
| error | string | HTTP status text |
| message | string | Detailed error description |
| path | string | Request path that caused the error |

### Common HTTP Status Codes

#### 2xx Success

| Code | Status | Description |
|------|--------|-------------|
| 200 | OK | Request succeeded |
| 201 | Created | Resource created successfully |
| 204 | No Content | Request succeeded, no content to return |

#### 4xx Client Errors

| Code | Status | Description | Example |
|------|--------|-------------|---------|
| 400 | Bad Request | Invalid request data | Validation errors, malformed JSON |
| 401 | Unauthorized | Authentication required | Missing or invalid JWT token |
| 403 | Forbidden | Access denied | Insufficient permissions |
| 404 | Not Found | Resource doesn't exist | Invalid ID |
| 409 | Conflict | Resource conflict | Duplicate username |

#### 5xx Server Errors

| Code | Status | Description |
|------|--------|-------------|
| 500 | Internal Server Error | Unexpected server error |
| 503 | Service Unavailable | Service temporarily unavailable |

---

## Error Examples

### 400 Bad Request - Validation Error

```json
{
  "timestamp": "2025-10-16T15:30:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: name must not be blank",
  "path": "/api/1/trips"
}
```

### 401 Unauthorized - Missing Token

```json
{
  "timestamp": "2025-10-16T15:30:00.000Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/1/trips/me"
}
```

### 401 Unauthorized - Invalid Credentials

```json
{
  "timestamp": "2025-10-16T15:30:00.000Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid username or password",
  "path": "/api/1/auth/login"
}
```

### 403 Forbidden - Insufficient Permissions

```json
{
  "timestamp": "2025-10-16T15:30:00.000Z",
  "status": 403,
  "error": "Forbidden",
  "message": "You don't have permission to modify this trip",
  "path": "/api/1/trips/660e8400-e29b-41d4-a716-446655440000"
}
```

### 404 Not Found - Resource Not Found

```json
{
  "timestamp": "2025-10-16T15:30:00.000Z",
  "status": 404,
  "error": "Not Found",
  "message": "Trip not found with id: 660e8400-e29b-41d4-a716-446655440000",
  "path": "/api/1/trips/660e8400-e29b-41d4-a716-446655440000"
}
```

### 409 Conflict - Duplicate Resource

```json
{
  "timestamp": "2025-10-16T15:30:00.000Z",
  "status": 409,
  "error": "Conflict",
  "message": "Username 'johndoe' already exists",
  "path": "/api/1/auth/register"
}
```

### 500 Internal Server Error

```json
{
  "timestamp": "2025-10-16T15:30:00.000Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred. Please try again later.",
  "path": "/api/1/trips"
}
```

---

## Pagination (Future)

When pagination is implemented, responses will include metadata:

```json
{
  "content": [...],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  }
}
```

---

## Headers

### Request Headers

| Header | Required | Description | Example |
|--------|----------|-------------|---------|
| Authorization | Yes* | JWT bearer token | `Bearer eyJhbGc...` |
| Content-Type | Yes** | Request content type | `application/json` |

\* Required for authenticated endpoints  
\*\* Required for POST, PUT, PATCH requests

### Response Headers

| Header | Description |
|--------|-------------|
| Content-Type | Response content type (always `application/json`) |
| Location | URL of newly created resource (201 responses) |

---

## Best Practices

### Error Handling

1. **Always check status code** - Don't assume success
2. **Parse error messages** - Display helpful messages to users
3. **Retry on 5xx errors** - With exponential backoff
4. **Don't retry on 4xx errors** - Fix the request instead

### Data Validation

1. **Validate before sending** - Check data on client side
2. **Handle validation errors** - Display field-specific errors
3. **Check required fields** - Ensure all required fields are present
4. **Validate data types** - Match expected types (UUID, ISO 8601, etc.)

### Working with UUIDs

```javascript
// JavaScript example
const uuidPattern = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/;

function isValidUUID(uuid) {
  return uuidPattern.test(uuid);
}
```

### Working with Timestamps

```javascript
// JavaScript example
const timestamp = new Date().toISOString();
// "2025-10-16T15:30:00.123Z"

const date = new Date("2025-10-16T15:30:00Z");
// Parse ISO 8601 timestamp
```

---

## Next Steps

- [Getting Started Guide](Getting-Started-with-APIs) - See response formats in action
- [Security & Authorization](Security-and-Authorization) - Learn about authentication
- [API Overview](API-Overview) - Understand the API architecture
