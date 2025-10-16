# Security and Authorization

This document explains the security model, authentication mechanisms, and authorization rules in the Trip Tracker Backend API.

## Authentication

### JWT Token-Based Authentication

The API uses **JSON Web Tokens (JWT)** for authentication. After successful registration or login, you receive a JWT token that must be included in subsequent requests.

#### Token Structure

A JWT token consists of three parts separated by dots:
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VySWQiLC...4SflKxwRJSM
   ↑ Header              ↑ Payload                     ↑ Signature
```

#### Token Claims

The JWT payload contains:
- **sub** (subject): User ID (UUID)
- **username**: User's username
- **roles**: Array of user roles (e.g., ["USER", "ADMIN"])
- **iat** (issued at): Token creation timestamp
- **exp** (expiration): Token expiration timestamp

### Obtaining a Token

#### Register New User

```bash
curl -X POST http://localhost:8083/api/1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_camino",
    "password": "securePassword123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600000
}
```

#### Login

```bash
curl -X POST http://localhost:8083/api/1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_camino",
    "password": "securePassword123"
  }'
```

Returns the same response structure as registration.

### Using the Token

Include the token in the `Authorization` header with the `Bearer` scheme:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Example Request

```bash
curl -X GET http://localhost:8082/api/1/trips/me \
  -H "Authorization: Bearer <your-token>"
```

### Token Expiration

Tokens expire after a configured duration (default: 1 hour / 3600000 ms).

When a token expires:
- API returns `401 Unauthorized`
- Client must login again to obtain a new token
- Refresh tokens are not currently implemented

**Best Practice**: Implement token refresh or re-authentication flow before expiration.

---

## Authorization

### User Roles

The system supports two roles:

#### USER
- Default role for all registered users
- Can create and manage their own resources
- Can view public and protected content
- Can comment on and react to trips

#### ADMIN
- Has all USER permissions
- Can view all trips (including private)
- Can manage any resource
- Administrative access to system features

### Role Assignment

Currently, all registered users receive the **USER** role by default. Admin role assignment must be done at the database level.

---

## Endpoint Authorization

### Public Endpoints (No Authentication Required)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/1/auth/register` | POST | User registration |
| `/api/1/auth/login` | POST | User login |
| `/api/1/users/username/{username}` | GET | Get user by username |
| `/api/1/trips/public` | GET | Get ongoing public trips |

### Authenticated Endpoints (USER or ADMIN Role)

Most endpoints require authentication. The user must own the resource or have appropriate visibility permissions.

#### User Endpoints
- `GET /api/1/users/{id}` - Get user by ID
- `GET /api/1/users/me` - Get current user profile

#### Trip Endpoints
- `POST /api/1/trips` - Create trip (owner only)
- `PUT /api/1/trips/{id}` - Update trip (owner only)
- `PATCH /api/1/trips/{id}/visibility` - Change visibility (owner only)
- `PATCH /api/1/trips/{id}/status` - Change status (owner only)
- `DELETE /api/1/trips/{id}` - Delete trip (owner only)
- `GET /api/1/trips/{id}` - Get trip (owner or based on visibility)
- `GET /api/1/trips/me` - Get current user's trips
- `GET /api/1/trips/users/{userId}` - Get user's visible trips

#### Trip Plan Endpoints
- `POST /api/1/trips/plans` - Create plan (authenticated users)
- `PUT /api/1/trips/plans/{planId}` - Update plan (owner only)
- `DELETE /api/1/trips/plans/{planId}` - Delete plan (owner only)
- `GET /api/1/trips/plans/{planId}` - Get plan (owner or based on linked trip visibility)
- `GET /api/1/trips/plans/me` - Get current user's plans

#### Trip Update Endpoints
- `POST /api/1/trips/{tripId}/updates` - Create update (owner only)

#### Comment Endpoints
- `POST /api/1/trips/{tripId}/comments` - Add comment (authenticated users with trip access)
- `POST /api/1/comments/{commentId}/reactions` - Add reaction (authenticated users)
- `DELETE /api/1/comments/{commentId}/reactions` - Remove reaction (authenticated users)
- `GET /api/1/comments/{id}` - Get comment (authenticated users with trip access)
- `GET /api/1/trips/{tripId}/comments` - Get trip comments (authenticated users with trip access)

### Admin-Only Endpoints (ADMIN Role Required)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `GET /api/1/trips` | GET | Get all trips in the system |

---

## Resource Ownership

### Ownership Rules

Users can only modify resources they own:

1. **Trips**: Only the trip creator can update, delete, or change trip settings
2. **Trip Plans**: Only the plan creator can modify or delete their plans
3. **Trip Updates**: Only the trip owner can post updates
4. **Comments**: Users can only edit/delete their own comments (future feature)

### Authorization Checks

The API performs ownership checks using the authenticated user's ID (extracted from JWT):

```
if (resource.userId != authenticatedUserId && !isAdmin) {
    return 403 Forbidden
}
```

---

## Resource Visibility

Trips support three visibility levels that control access:

### PUBLIC

- Visible to **everyone** (including unauthenticated users)
- Appears in public trip listings
- Anyone can view trip details and comments

**Use Case**: Share your journey with the world

### PROTECTED

- Visible to **authenticated users only**
- Requires valid JWT token
- Hidden from unauthenticated users

**Use Case**: Share with the tracker community, but not publicly

### PRIVATE

- Visible only to the **trip owner**
- Even authenticated users cannot see private trips
- Does not appear in any public listings

**Use Case**: Personal tracking without sharing

### Visibility Rules

| Viewer Type | PUBLIC | PROTECTED | PRIVATE |
|-------------|--------|-----------|---------|
| Unauthenticated | ✅ | ❌ | ❌ |
| Authenticated User | ✅ | ✅ | ❌ |
| Trip Owner | ✅ | ✅ | ✅ |
| Admin | ✅ | ✅ | ✅ |

---

## Security Best Practices

### For API Users

#### Token Security

1. **Never expose tokens**
   - Don't log tokens
   - Don't include in URLs
   - Don't commit to version control

2. **Secure storage**
   - Use secure storage mechanisms (httpOnly cookies, secure storage APIs)
   - Clear tokens on logout
   - Don't store in localStorage for sensitive apps

3. **Transmission**
   - Always use HTTPS in production
   - Never send tokens over unsecured connections

#### Password Security

1. **Strong passwords**
   - Minimum 8 characters
   - Mix of letters, numbers, and symbols
   - Avoid common passwords

2. **Don't reuse passwords**
   - Use unique passwords for each service
   - Consider using a password manager

### For API Providers

#### Current Security Measures

1. **JWT Authentication**
   - Tokens signed with secret key
   - Expiration enforced
   - Claims validated on each request

2. **Password Hashing**
   - Passwords hashed using BCrypt
   - Never stored in plain text
   - Salted hashes

3. **HTTPS Enforcement**
   - Configure reverse proxy (nginx/Apache) with SSL/TLS
   - Redirect HTTP to HTTPS
   - Use strong cipher suites

4. **Input Validation**
   - Request body validation
   - Path parameter validation
   - SQL injection prevention (JPA/Hibernate)

5. **Authorization Checks**
   - Role-based access control
   - Resource ownership validation
   - Visibility enforcement

#### Recommended Production Security

1. **Environment Variables**
   ```properties
   # Don't hardcode secrets in application.properties
   jwt.secret=${JWT_SECRET}
   db.password=${DB_PASSWORD}
   ```

2. **Rate Limiting**
   - Implement request rate limiting
   - Prevent brute force attacks on auth endpoints
   - Consider tools like Resilience4j

3. **CORS Configuration**
   ```java
   @Configuration
   public class CorsConfig {
       @Bean
       public WebMvcConfigurer corsConfigurer() {
           return new WebMvcConfigurer() {
               @Override
               public void addCorsMappings(CorsRegistry registry) {
                   registry.addMapping("/api/**")
                       .allowedOrigins("https://yourdomain.com")
                       .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH");
               }
           };
       }
   }
   ```

4. **Security Headers**
   - X-Content-Type-Options: nosniff
   - X-Frame-Options: DENY
   - Content-Security-Policy
   - Strict-Transport-Security

5. **Monitoring**
   - Log authentication attempts
   - Monitor for suspicious activity
   - Alert on unusual patterns

---

## Common Security Scenarios

### Scenario 1: Token Theft

**If a token is compromised:**

1. Current system: Token remains valid until expiration
2. Mitigation: Short token expiration times (1 hour default)
3. Future: Implement token revocation/blacklist

**User Action:**
- Change password immediately
- Login again to get new token
- Old token expires after 1 hour

### Scenario 2: Unauthorized Access Attempt

**If someone tries to access your trip:**

```bash
# Attempt to access private trip
curl -X GET http://localhost:8082/api/1/trips/660e8400... \
  -H "Authorization: Bearer <other-user-token>"
```

**Response:**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Trip not found"
}
```

Note: Returns 404 (not 403) to avoid leaking information about resource existence.

### Scenario 3: Admin Access

**Admins can view all resources:**

```bash
# Admin gets all trips (including private)
curl -X GET http://localhost:8082/api/1/trips \
  -H "Authorization: Bearer <admin-token>"
```

---

## Authentication Flow Diagram

```
┌─────────┐                 ┌──────────┐                 ┌─────────┐
│ Client  │                 │  Auth    │                 │ Command │
│         │                 │ Service  │                 │ Service │
└────┬────┘                 └────┬─────┘                 └────┬────┘
     │                           │                            │
     │  POST /auth/register      │                            │
     │ ─────────────────────────>│                            │
     │                           │                            │
     │  JWT Token                │                            │
     │ <─────────────────────────│                            │
     │                           │                            │
     │  POST /trips              │                            │
     │  Authorization: Bearer... │                            │
     │ ───────────────────────────────────────────────────────>│
     │                           │                            │
     │                           │   Validate JWT             │
     │                           │ <──────────────────────────│
     │                           │                            │
     │                           │   User ID, Roles           │
     │                           │ ───────────────────────────>│
     │                           │                            │
     │                           │                     Create Trip
     │                           │                     (check auth)
     │                           │                            │
     │  Trip Created             │                            │
     │ <───────────────────────────────────────────────────────│
     │                           │                            │
```

---

## Future Security Enhancements

### Planned Features

1. **Refresh Tokens**
   - Long-lived refresh tokens
   - Short-lived access tokens
   - Token rotation

2. **OAuth2 / OpenID Connect**
   - Social login (Google, GitHub)
   - Enterprise SSO integration

3. **API Keys**
   - For device authentication (OwnTracks)
   - Separate from user JWT tokens

4. **Two-Factor Authentication (2FA)**
   - TOTP-based 2FA
   - SMS or email verification

5. **Account Security**
   - Password reset flow
   - Email verification
   - Account lockout after failed attempts

6. **Audit Logging**
   - Track all security-relevant events
   - User activity logs
   - Admin action logs

---

## Compliance and Standards

The API follows these security standards:

- **OWASP Top 10**: Protection against common vulnerabilities
- **JWT Best Practices**: RFC 7519 compliance
- **Password Storage**: BCrypt with proper work factor
- **HTTPS**: TLS 1.2+ in production

---

## Getting Help

If you discover a security vulnerability:

1. **Do NOT** open a public issue
2. Email: [Security contact - add email]
3. Include:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact

We take security seriously and will respond promptly to verified reports.

---

## Next Steps

- [Authentication API](Authentication-API) - Learn about auth endpoints
- [Getting Started Guide](Getting-Started-with-APIs) - See authentication in action
- [API Overview](API-Overview) - Understand the API architecture
