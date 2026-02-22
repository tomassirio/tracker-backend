# Admin Role Management

This document describes how to manage admin roles in the Tracker Backend application, including the bootstrap mechanism for creating the first admin user.

## Overview

The application uses a role-based access control (RBAC) system with two roles:
- **USER**: Standard user role (default for all new users)
- **ADMIN**: Administrator role with elevated privileges

## Bootstrap First Admin

Since only admins can promote other users to admin, there's a special bootstrap mechanism to create the first admin user when no admins exist in the system.

### Configuration

Set the following environment variables or application properties:

```properties
# Username of the user to auto-promote to admin (if no admins exist)
bootstrap.admin.username=your-admin-username

# Enable/disable the bootstrap mechanism (default: true)
bootstrap.admin.enabled=true
```

### Bootstrap Process

1. **Create a user account** via the registration endpoint (`POST /api/1/auth/register`)
2. **Set the bootstrap username** in your application configuration
3. **Restart the application** - the bootstrap mechanism will automatically:
   - Check if any admin users exist
   - If no admins found and `bootstrap.admin.username` is set, promote that user to admin
   - Log the promotion or any errors

### Example: Docker Compose

```yaml
services:
  tracker-auth:
    image: tracker-auth:latest
    environment:
      - BOOTSTRAP_ADMIN_USERNAME=admin
      - BOOTSTRAP_ADMIN_ENABLED=true
```

### Example: Kubernetes

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: tracker-auth-config
data:
  bootstrap.admin.username: "admin"
  bootstrap.admin.enabled: "true"
```

### Example: GitHub Actions (Per Environment)

Set environment-specific secrets or variables in your GitHub repository settings:

1. Navigate to **Settings** → **Environments** → **Select environment** (e.g., `production`, `staging`, `dev`)
2. Add the following environment variables:

| Variable | Description | Example |
|----------|-------------|---------|
| `BOOTSTRAP_ADMIN_USERNAME` | Username of the user to promote to admin | `admin` |
| `BOOTSTRAP_ADMIN_ENABLED` | Enable/disable bootstrap (default: true) | `true` |

Then reference them in your workflow:

```yaml
jobs:
  deploy:
    runs-on: ubuntu-latest
    environment: production  # Uses environment-specific variables
    steps:
      - name: Deploy to Kubernetes
        env:
          BOOTSTRAP_ADMIN_USERNAME: ${{ vars.BOOTSTRAP_ADMIN_USERNAME }}
          BOOTSTRAP_ADMIN_ENABLED: ${{ vars.BOOTSTRAP_ADMIN_ENABLED }}
        run: |
          # Your deployment command here
```

### Bootstrap Scenarios

#### Scenario 1: First deployment (no admins exist)
1. Register user `admin` via `/api/1/auth/register`
2. Set `BOOTSTRAP_ADMIN_USERNAME=admin`
3. Start the application
4. User `admin` is automatically promoted to ADMIN role
5. Login as `admin` - JWT token will include `ROLE_ADMIN`

#### Scenario 2: Admins already exist
- Bootstrap mechanism skips promotion
- Existing admins can promote other users

#### Scenario 3: Bootstrap user doesn't exist
- Application logs an error message
- No promotion occurs
- Create the user first, then restart

## Admin Promotion API

Once at least one admin exists, admins can promote/demote other users via REST API.

### Endpoints

All admin endpoints require authentication with `ROLE_ADMIN`.

#### Promote User to Admin

```http
POST /api/1/admin/users/{userId}/promote
Authorization: Bearer <admin-jwt-token>
```

**Response:**
- `204 No Content` - User promoted successfully
- `400 Bad Request` - User not found or already has admin role
- `401 Unauthorized` - Not authenticated
- `403 Forbidden` - Not an admin

**Example:**
```bash
curl -X POST "http://localhost:8083/api/1/admin/users/550e8400-e29b-41d4-a716-446655440000/promote" \
  -H "Authorization: Bearer eyJhbGc..."
```

#### Demote User from Admin

```http
DELETE /api/1/admin/users/{userId}/promote
Authorization: Bearer <admin-jwt-token>
```

**Response:**
- `204 No Content` - User demoted successfully
- `400 Bad Request` - User not found or doesn't have admin role
- `401 Unauthorized` - Not authenticated
- `403 Forbidden` - Not an admin

**Example:**
```bash
curl -X DELETE "http://localhost:8083/api/1/admin/users/550e8400-e29b-41d4-a716-446655440000/promote" \
  -H "Authorization: Bearer eyJhbGc..."
```

#### Get User Roles

```http
GET /api/1/admin/users/{userId}/roles
Authorization: Bearer <admin-jwt-token>
```

**Response:**
- `200 OK` - Returns array of roles (e.g., `["USER", "ADMIN"]`)
- `400 Bad Request` - User not found
- `401 Unauthorized` - Not authenticated
- `403 Forbidden` - Not an admin

**Example:**
```bash
curl -X GET "http://localhost:8083/api/1/admin/users/550e8400-e29b-41d4-a716-446655440000/roles" \
  -H "Authorization: Bearer eyJhbGc..."
```

**Response:**
```json
["USER", "ADMIN"]
```

## JWT Token Roles

After a user is promoted to admin, their JWT token will include both roles:

```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",
  "username": "admin",
  "roles": ["USER", "ADMIN"],
  "iat": 1234567890,
  "exp": 1234571490
}
```

**Important:** Users must re-login after promotion/demotion for role changes to take effect in their JWT tokens.

## Security Best Practices

1. **Secure the bootstrap username**: Don't use obvious usernames like "admin" in production
2. **Disable bootstrap after first admin**: Set `bootstrap.admin.enabled=false` after creating your first admin
3. **Use strong passwords**: Enforce strong password requirements for admin accounts
4. **Regular audits**: Periodically review admin users and demote inactive admins
5. **Separate admin accounts**: Don't use admin accounts for day-to-day operations

## Troubleshooting

### Bootstrap Not Working

**Problem**: Bootstrap admin user not promoted

**Solutions**:
1. Check logs for error messages
2. Verify user exists (registered via `/api/1/auth/register`)
3. Verify `bootstrap.admin.username` matches exact username
4. Ensure `bootstrap.admin.enabled=true`
5. Restart the application

**Logs to check**:
```
INFO  No admin users found. Attempting to bootstrap admin user: <username>
INFO  Successfully promoted user '<username>' to admin as first admin user
ERROR Bootstrap admin user '<username>' not found
```

### Promotion Returns 403

**Problem**: Admin endpoint returns 403 Forbidden

**Solutions**:
1. Verify JWT token is valid and not expired
2. Check token includes `ROLE_ADMIN` in roles claim
3. Re-login if recently promoted to refresh token
4. Verify Authorization header format: `Bearer <token>`

### User Already Promoted

**Problem**: Promotion returns 400 "User already has admin role"

**Solution**: User is already an admin. Use GET `/api/1/admin/users/{userId}/roles` to verify roles.

## Database Schema

Admin roles are stored in the `user_credentials` table:

```sql
CREATE TABLE user_credentials (
    user_id UUID PRIMARY KEY,
    password_hash VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true,
    email VARCHAR(255) NOT NULL UNIQUE,
    roles VARCHAR(1000)  -- Stores roles as comma-separated string (e.g., "USER,ADMIN")
);
```

Roles are serialized/deserialized using `RolesConverter` JPA converter.

## API Documentation

Full API documentation is available via Swagger UI:
- **Auth Service**: http://localhost:8083/swagger-ui.html

Look for the "Admin" tag in the API documentation.

---

**Version**: 0.5.2+  
**Last Updated**: 2026-02-22
