# User API

The User API provides endpoints for creating and querying user information.

## Command Operations (Port 8081)

**Base URL**: `http://localhost:8081/api/1/users`

### Create User

Create a new user in the system.

**Endpoint**: `POST /api/1/users`

**Authentication**: Not required (public endpoint)

#### Request Body

```json
{
  "username": "string"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| username | string | Yes | Unique username |

#### Response

**Status**: `201 Created`

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "johndoe"
}
```

#### Example

```bash
curl -X POST http://localhost:8081/api/1/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe"
  }'
```

---

## Query Operations (Port 8082)

**Base URL**: `http://localhost:8082/api/1/users`

### Get User by ID

Retrieve a specific user by their UUID.

**Endpoint**: `GET /api/1/users/{id}`

**Authentication**: Required (USER or ADMIN role)

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| id | UUID | User's unique identifier |

#### Response

**Status**: `200 OK`

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "johndoe"
}
```

#### Example

```bash
curl -X GET http://localhost:8082/api/1/users/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <your-token>"
```

---

### Get User by Username

Retrieve a user by their username.

**Endpoint**: `GET /api/1/users/username/{username}`

**Authentication**: Not required (public endpoint)

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| username | string | User's username |

#### Response

**Status**: `200 OK`

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "johndoe"
}
```

#### Example

```bash
curl -X GET http://localhost:8082/api/1/users/username/johndoe
```

---

### Get Current User

Retrieve the profile of the currently authenticated user.

**Endpoint**: `GET /api/1/users/me`

**Authentication**: Required (USER or ADMIN role)

#### Response

**Status**: `200 OK`

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "johndoe"
}
```

#### Example

```bash
curl -X GET http://localhost:8082/api/1/users/me \
  -H "Authorization: Bearer <your-token>"
```

---

## Error Responses

### 404 Not Found

User doesn't exist:
```json
{
  "timestamp": "2025-10-16T10:30:00.000Z",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: 550e8400-e29b-41d4-a716-446655440000",
  "path": "/api/1/users/550e8400-e29b-41d4-a716-446655440000"
}
```

### 401 Unauthorized

Missing or invalid authentication token:
```json
{
  "timestamp": "2025-10-16T10:30:00.000Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/1/users/550e8400-e29b-41d4-a716-446655440000"
}
```

---

## Usage Examples

### Complete User Workflow

```bash
# 1. Register a new user (creates user and returns token)
RESPONSE=$(curl -X POST http://localhost:8083/api/1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"johndoe","password":"secret123"}')

# Extract token
TOKEN=$(echo $RESPONSE | jq -r '.token')

# 2. Get your own profile
curl -X GET http://localhost:8082/api/1/users/me \
  -H "Authorization: Bearer $TOKEN"

# 3. Look up another user by username
curl -X GET http://localhost:8082/api/1/users/username/janedoe
```

---

## Next Steps

- [Trip API](Trip-API) - Create and manage trips
- [Authentication API](Authentication-API) - Learn about authentication
- [Getting Started Guide](Getting-Started-with-APIs) - Full workflow examples
