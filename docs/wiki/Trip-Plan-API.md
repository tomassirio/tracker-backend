# Trip Plan API

The Trip Plan API allows you to create, update, and delete trip plans that define your intended route and waypoints.

## Command Operations (Port 8081)

**Base URL**: `http://localhost:8081/api/1/trips/plans`

**Authentication**: Required (USER or ADMIN role)

### Create Trip Plan

Create a new trip plan with route details and waypoints.

**Endpoint**: `POST /api/1/trips/plans`

#### Request Body

```json
{
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
    "restDays": [7, 14, 21],
    "waypoints": [
      {
        "name": "Pamplona",
        "location": {"latitude": 42.8125, "longitude": -1.6458},
        "day": 3
      },
      {
        "name": "Logroño",
        "location": {"latitude": 42.4627, "longitude": -2.4450},
        "day": 7
      },
      {
        "name": "Burgos",
        "location": {"latitude": 42.3439, "longitude": -3.6967},
        "day": 12
      },
      {
        "name": "León",
        "location": {"latitude": 42.5987, "longitude": -5.5671},
        "day": 21
      }
    ]
  }
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | string | Yes | Plan name |
| planType | enum | Yes | SIMPLE or MULTI_DAY |
| startLocation | GeoLocation | Yes | Starting coordinates |
| endLocation | GeoLocation | Yes | Destination coordinates |
| startDate | ISO 8601 | No | Planned start date/time |
| endDate | ISO 8601 | No | Planned end date/time |
| metadata | JSON | No | Flexible plan data (waypoints, notes, etc.) |

**Plan Types:**
- **SIMPLE**: Direct point-to-point plan without waypoints
- **MULTI_DAY**: Multi-day journey with waypoints and daily stages

#### Response

**Status**: `201 Created`

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
    "restDays": [7, 14, 21],
    "waypoints": [...]
  },
  "creationTimestamp": "2025-10-16T10:30:00Z",
  "updateTimestamp": "2025-10-16T10:30:00Z"
}
```

#### Example

```bash
curl -X POST http://localhost:8081/api/1/trips/plans \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Camino Francés Plan",
    "planType": "MULTI_DAY",
    "startLocation": {"latitude": 43.1631, "longitude": -1.2350},
    "endLocation": {"latitude": 42.8805, "longitude": -8.5457},
    "startDate": "2025-11-01T08:00:00Z"
  }'
```

---

### Update Trip Plan

Update an existing trip plan.

**Endpoint**: `PUT /api/1/trips/plans/{planId}`

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| planId | UUID | Plan's unique identifier |

#### Request Body

Same structure as Create Trip Plan. All fields are optional, but at least one should be provided.

```json
{
  "name": "Updated Plan Name",
  "startDate": "2025-11-05T08:00:00Z",
  "metadata": {
    "dailyDistance": 30,
    "note": "Increased daily distance"
  }
}
```

#### Response

**Status**: `200 OK`

Returns the updated trip plan object.

#### Example

```bash
curl -X PUT http://localhost:8081/api/1/trips/plans/880e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Camino Plan",
    "startDate": "2025-11-05T08:00:00Z"
  }'
```

---

### Delete Trip Plan

Delete a trip plan and all associated data.

**Endpoint**: `DELETE /api/1/trips/plans/{planId}`

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| planId | UUID | Plan's unique identifier |

#### Response

**Status**: `204 No Content`

#### Example

```bash
curl -X DELETE http://localhost:8081/api/1/trips/plans/880e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <your-token>"
```

---

## Query Operations (Port 8082)

**Base URL**: `http://localhost:8082/api/1/trips/plans`

### Get Trip Plan by ID

Retrieve a specific trip plan.

**Endpoint**: `GET /api/1/trips/plans/{planId}`

**Authentication**: Required (USER or ADMIN role)

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| planId | UUID | Plan's unique identifier |

#### Response

**Status**: `200 OK`

Returns the trip plan object.

#### Example

```bash
curl -X GET http://localhost:8082/api/1/trips/plans/880e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <your-token>"
```

---

### Get Trip Plans for User

Retrieve all trip plans for the authenticated user.

**Endpoint**: `GET /api/1/trips/plans/me`

**Authentication**: Required (USER or ADMIN role)

#### Response

**Status**: `200 OK`

Returns an array of trip plan objects.

#### Example

```bash
curl -X GET http://localhost:8082/api/1/trips/plans/me \
  -H "Authorization: Bearer <your-token>"
```

---

## Metadata Structure

The `metadata` field is a flexible JSON object that can store any plan-specific data. Here are some common patterns:

### Simple Plan Metadata

```json
{
  "notes": "Direct route to Santiago",
  "estimatedDays": 30,
  "difficulty": "moderate"
}
```

### Multi-Day Plan Metadata

```json
{
  "dailyDistance": 25,
  "restDays": [7, 14, 21, 28],
  "accommodations": [
    {
      "name": "Albergue Municipal",
      "location": {"latitude": 42.8125, "longitude": -1.6458},
      "day": 3,
      "phone": "+34 948 123 456"
    }
  ],
  "waypoints": [
    {
      "name": "Pamplona",
      "location": {"latitude": 42.8125, "longitude": -1.6458},
      "day": 3,
      "description": "Historic city, running of the bulls"
    },
    {
      "name": "Logroño", 
      "location": {"latitude": 42.4627, "longitude": -2.4450},
      "day": 7,
      "description": "Wine region capital"
    }
  ],
  "notes": "Following the traditional Camino Francés route"
}
```

### Custom Metadata Examples

**For cyclists:**
```json
{
  "mode": "bicycle",
  "dailyDistance": 80,
  "elevation": {
    "totalAscent": 12000,
    "highestPoint": 1505
  },
  "bikeFriendly": true
}
```

**For hikers:**
```json
{
  "mode": "walking",
  "difficulty": "moderate",
  "terrain": ["mountains", "plains", "urban"],
  "equipment": ["hiking boots", "backpack", "poles"],
  "waterSources": [
    {"location": {...}, "type": "fountain"},
    {"location": {...}, "type": "stream"}
  ]
}
```

---

## Linking Trip Plans to Trips

When creating a trip, you can reference a trip plan using the `tripPlanId` field:

```bash
# 1. Create a trip plan
PLAN_RESPONSE=$(curl -X POST http://localhost:8081/api/1/trips/plans \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Camino Plan",
    "planType": "MULTI_DAY",
    "startLocation": {"latitude": 43.1631, "longitude": -1.2350},
    "endLocation": {"latitude": 42.8805, "longitude": -8.5457}
  }')

PLAN_ID=$(echo $PLAN_RESPONSE | jq -r '.id')

# 2. Create a trip using the plan
curl -X POST http://localhost:8081/api/1/trips \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"My Camino Journey\",
    \"tripPlanId\": \"$PLAN_ID\",
    \"visibility\": \"PUBLIC\"
  }"
```

---

## Error Responses

### 404 Not Found

Plan doesn't exist or you don't have access:
```json
{
  "timestamp": "2025-10-16T10:30:00.000Z",
  "status": 404,
  "error": "Not Found",
  "message": "Trip plan not found",
  "path": "/api/1/trips/plans/880e8400-e29b-41d4-a716-446655440000"
}
```

### 403 Forbidden

Cannot modify someone else's plan:
```json
{
  "timestamp": "2025-10-16T10:30:00.000Z",
  "status": 403,
  "error": "Forbidden",
  "message": "You don't have permission to modify this trip plan",
  "path": "/api/1/trips/plans/880e8400-e29b-41d4-a716-446655440000"
}
```

---

## Best Practices

### Plan Naming
- Use descriptive names that identify the route
- Include the route name or key destinations
- Examples: "Camino Francés", "Utrecht to Santiago", "Portuguese Coastal Route"

### Waypoint Selection
- Include major cities and landmarks
- Add accommodation stops
- Mark rest days
- Include points of interest

### Metadata Usage
- Store structured data that your application will use
- Include information useful for planning and navigation
- Document your metadata schema for consistency

---

## Next Steps

- [Trip API](Trip-API) - Create trips based on your plans
- [Trip Update API](Trip-Update-API) - Track progress along your plan
- [Getting Started Guide](Getting-Started-with-APIs) - Full workflow examples
