# Authentication API

The Authentication API handles user registration and login, providing JWT tokens for accessing protected endpoints.

**Base URL**: `http://localhost:8083/api/1/auth`

**Service**: tracker-auth (Port 8083)

## Endpoints

### Register

Create a new user account and receive a JWT token.

**Endpoint**: `POST /api/1/auth/register`

**Authentication**: Not required

#### Request Body

```json
{
  "username": "string",
  "password": "string"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| username | string | Yes | Unique username (min 3 characters) |
| password | string | Yes | Password (min 8 characters) |

#### Response

**Status**: `201 Created`

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600000
}
```

| Field | Type | Description |
|-------|------|-------------|
| token | string | JWT access token |
| tokenType | string | Token type (always "Bearer") |
| expiresIn | number | Token expiration time in milliseconds |

#### Example

```bash
curl -X POST http://localhost:8083/api/1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "securePassword123"
  }'
```

#### Error Responses

**409 Conflict** - Username already exists
```json
{
  "timestamp": "2025-10-16T10:30:00.000Z",
  "status": 409,
  "error": "Conflict",
  "message": "Username already exists",
  "path": "/api/1/auth/register"
}
```

**400 Bad Request** - Validation error
```json
{
  "timestamp": "2025-10-16T10:30:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Username must be at least 3 characters",
  "path": "/api/1/auth/register"
}
```

---

### Login

Authenticate with username and password to receive a JWT token.

**Endpoint**: `POST /api/1/auth/login`

**Authentication**: Not required

#### Request Body

```json
{
  "username": "string",
  "password": "string"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| username | string | Yes | Your username |
| password | string | Yes | Your password |

#### Response

**Status**: `200 OK`

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600000
}
```

| Field | Type | Description |
|-------|------|-------------|
| token | string | JWT access token |
| tokenType | string | Token type (always "Bearer") |
| expiresIn | number | Token expiration time in milliseconds (default: 1 hour) |

#### Example

```bash
curl -X POST http://localhost:8083/api/1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "securePassword123"
  }'
```

#### Error Responses

**401 Unauthorized** - Invalid credentials
```json
{
  "timestamp": "2025-10-16T10:30:00.000Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid username or password",
  "path": "/api/1/auth/login"
}
```

---

## Using JWT Tokens

After successful registration or login, use the returned token in subsequent requests:

```bash
curl -X GET http://localhost:8082/api/1/trips/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Token Structure

The JWT token contains:
- **User ID**: Unique identifier for the user
- **Username**: User's username
- **Roles**: User's roles (USER, ADMIN)
- **Expiration**: Token expiration timestamp

### Token Expiration

Tokens expire after a configured time (default: 1 hour). When a token expires, you'll receive a `401 Unauthorized` response and need to login again.

### Token Storage

**Security Best Practices:**
- Store tokens securely (e.g., httpOnly cookies for web apps)
- Never expose tokens in URLs
- Don't store tokens in localStorage for sensitive applications
- Clear tokens on logout

## Security Considerations

- **HTTPS**: Always use HTTPS in production to protect credentials and tokens
- **Password Requirements**: Enforce strong passwords (minimum 8 characters)
- **Token Rotation**: Consider implementing refresh tokens for long-lived sessions
- **Rate Limiting**: Implement rate limiting on authentication endpoints to prevent brute force attacks

## Next Steps

- [User API](User-API) - Manage user profiles
- [Trip API](Trip-API) - Create and manage trips
- [Getting Started Guide](Getting-Started-with-APIs) - Full workflow examples
