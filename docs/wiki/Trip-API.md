# Trip API

The Trip API provides endpoints for creating, updating, and querying trips. Operations are split between command (write) and query (read) services following CQRS architecture.

## Command Operations (Port 8081)

**Base URL**: `http://localhost:8081/api/1/trips`

**Authentication**: Required (USER or ADMIN role)

### Create Trip

Create a new trip.

**Endpoint**: `POST /api/1/trips`

#### Request Body

```json
{
  "name": "My Camino Journey",
  "visibility": "PUBLIC",
  "status": "CREATED",
  "tripPlanId": "550e8400-e29b-41d4-a716-446655440000",
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
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | string | Yes | Trip name |
| visibility | enum | No | PUBLIC, PRIVATE, or PROTECTED (default: PUBLIC) |
| status | enum | No | CREATED, IN_PROGRESS, PAUSED, or FINISHED (default: CREATED) |
| tripPlanId | UUID | No | Reference to a trip plan |
| startLocation | GeoLocation | No | Starting coordinates |
| endLocation | GeoLocation | No | Destination coordinates |
| startDate | ISO 8601 | No | Planned start date/time |
| endDate | ISO 8601 | No | Planned end date/time |
| estimatedDistance | number | No | Estimated distance in kilometers |

#### Response

**Status**: `201 Created`

```json
{
  "id": "660e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "My Camino Journey",
  "tripSettings": {
    "visibility": "PUBLIC",
    "status": "CREATED"
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
  "tripPlanId": "550e8400-e29b-41d4-a716-446655440000",
  "creationTimestamp": "2025-10-16T10:30:00Z",
  "enabled": true
}
```

#### Example

```bash
curl -X POST http://localhost:8081/api/1/trips \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Camino Journey",
    "visibility": "PUBLIC",
    "startDate": "2025-11-01T08:00:00Z"
  }'
```

---

### Update Trip

Update an existing trip's details.

**Endpoint**: `PUT /api/1/trips/{id}`

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| id | UUID | Trip's unique identifier |

#### Request Body

```json
{
  "name": "Updated Trip Name",
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
  "estimatedDistance": 800.0
}
```

#### Response

**Status**: `200 OK`

Returns the updated trip object (same structure as Create Trip response).

#### Example

```bash
curl -X PUT http://localhost:8081/api/1/trips/660e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Trip Name",
    "estimatedDistance": 800.0
  }'
```

---

### Change Trip Visibility

Update a trip's visibility setting.

**Endpoint**: `PATCH /api/1/trips/{id}/visibility`

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| id | UUID | Trip's unique identifier |

#### Request Body

```json
{
  "visibility": "PRIVATE"
}
```

| Field | Type | Required | Options |
|-------|------|----------|---------|
| visibility | enum | Yes | PUBLIC, PRIVATE, PROTECTED |

**Visibility Options:**
- **PUBLIC**: Visible to everyone (including unauthenticated users)
- **PRIVATE**: Only visible to the trip owner
- **PROTECTED**: Visible to authenticated users

#### Response

**Status**: `200 OK`

Returns the updated trip object.

#### Example

```bash
curl -X PATCH http://localhost:8081/api/1/trips/660e8400-e29b-41d4-a716-446655440000/visibility \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "visibility": "PRIVATE"
  }'
```

---

### Change Trip Status

Update a trip's status.

**Endpoint**: `PATCH /api/1/trips/{id}/status`

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| id | UUID | Trip's unique identifier |

#### Request Body

```json
{
  "status": "IN_PROGRESS"
}
```

| Field | Type | Required | Options |
|-------|------|----------|---------|
| status | enum | Yes | CREATED, IN_PROGRESS, PAUSED, FINISHED |

**Status Options:**
- **CREATED**: Trip has been created but not started
- **IN_PROGRESS**: Trip is currently active
- **PAUSED**: Trip is temporarily paused
- **FINISHED**: Trip has been completed

#### Response

**Status**: `200 OK`

Returns the updated trip object.

#### Example

```bash
curl -X PATCH http://localhost:8081/api/1/trips/660e8400-e29b-41d4-a716-446655440000/status \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "IN_PROGRESS"
  }'
```

---

### Delete Trip

Delete a trip permanently.

**Endpoint**: `DELETE /api/1/trips/{id}`

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| id | UUID | Trip's unique identifier |

#### Response

**Status**: `204 No Content`

#### Example

```bash
curl -X DELETE http://localhost:8081/api/1/trips/660e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <your-token>"
```

---

## Query Operations (Port 8082)

**Base URL**: `http://localhost:8082/api/1/trips`

### Get Trip by ID

Retrieve a specific trip by its UUID.

**Endpoint**: `GET /api/1/trips/{id}`

**Authentication**: Required (USER or ADMIN role)

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| id | UUID | Trip's unique identifier |

#### Response

**Status**: `200 OK`

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
  "tripPlanId": "550e8400-e29b-41d4-a716-446655440000",
  "creationTimestamp": "2025-10-16T10:30:00Z",
  "enabled": true
}
```

#### Example

```bash
curl -X GET http://localhost:8082/api/1/trips/660e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <your-token>"
```

---

### Get All Trips

Retrieve all trips in the system (admin only).

**Endpoint**: `GET /api/1/trips`

**Authentication**: Required (ADMIN role only)

#### Response

**Status**: `200 OK`

Returns an array of trip objects.

```json
[
  {
    "id": "660e8400-e29b-41d4-a716-446655440000",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "My Camino Journey",
    ...
  },
  {
    "id": "770e8400-e29b-41d4-a716-446655440000",
    "userId": "440e8400-e29b-41d4-a716-446655440000",
    "name": "Another Trip",
    ...
  }
]
```

#### Example

```bash
curl -X GET http://localhost:8082/api/1/trips \
  -H "Authorization: Bearer <admin-token>"
```

---

### Get My Trips

Retrieve all trips belonging to the authenticated user.

**Endpoint**: `GET /api/1/trips/me`

**Authentication**: Required (USER or ADMIN role)

#### Response

**Status**: `200 OK`

Returns an array of trip objects owned by the authenticated user.

#### Example

```bash
curl -X GET http://localhost:8082/api/1/trips/me \
  -H "Authorization: Bearer <your-token>"
```

---

### Get Trips by User

Retrieve public and protected trips of a specific user.

**Endpoint**: `GET /api/1/trips/users/{userId}`

**Authentication**: Required (USER or ADMIN role)

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| userId | UUID | User's unique identifier |

#### Response

**Status**: `200 OK`

Returns an array of trip objects visible to the requester (PUBLIC and PROTECTED trips).

#### Example

```bash
curl -X GET http://localhost:8082/api/1/trips/users/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <your-token>"
```

---

### Get Ongoing Public Trips

Retrieve all public trips that are currently in progress.

**Endpoint**: `GET /api/1/trips/public`

**Authentication**: Not required (public endpoint)

#### Response

**Status**: `200 OK`

Returns an array of public trips with status `IN_PROGRESS`.

#### Example

```bash
curl -X GET http://localhost:8082/api/1/trips/public
```

---

## Error Responses

### 404 Not Found

Trip doesn't exist or you don't have access:
```json
{
  "timestamp": "2025-10-16T10:30:00.000Z",
  "status": 404,
  "error": "Not Found",
  "message": "Trip not found",
  "path": "/api/1/trips/660e8400-e29b-41d4-a716-446655440000"
}
```

### 403 Forbidden

Insufficient permissions (e.g., trying to update someone else's trip):
```json
{
  "timestamp": "2025-10-16T10:30:00.000Z",
  "status": 403,
  "error": "Forbidden",
  "message": "You don't have permission to modify this trip",
  "path": "/api/1/trips/660e8400-e29b-41d4-a716-446655440000"
}
```

---

## Next Steps

- [Trip Update API](Trip-Update-API) - Add location updates to trips
- [Trip Plan API](Trip-Plan-API) - Plan your trip routes
- [Comment API](Comment-API) - Add comments and reactions to trips
