# Promoted Trips API Documentation

## Overview

The **Promoted Trips** feature allows administrators to showcase selected trips on the platform. Promoted trips can include an optional donation link, making them ideal for charitable journeys or featured adventures. The feature follows a **CQRS (Command Query Responsibility Segregation)** architecture with event-driven persistence for eventual consistency.

### Key Features
- ✅ **Admin-only promotion operations** - Only users with ADMIN role can promote/unpromote trips
- ✅ **Optional donation links** - Include a donation URL for fundraising trips
- ✅ **URL validation** - Donation links must be valid URLs (max 500 characters)
- ✅ **Unique promotions** - Each trip can only be promoted once
- ✅ **Public read access** - Anyone can view promoted trips (no authentication required)
- ✅ **Event-driven architecture** - Uses CQRS pattern with asynchronous persistence
- ✅ **Cascade deletion** - Promotion is automatically removed when trip or admin user is deleted

---

## Architecture

### CQRS Pattern

The promoted trips feature follows CQRS architecture across multiple modules:

- **Command Side** (`tracker-command:8081`) - Write operations (promote, unpromote, update)
  - Services publish domain events immediately
  - Returns HTTP 202 Accepted (operation pending)
  - Returns the generated UUID for tracking
  - Persistence happens asynchronously via event handlers

- **Query Side** (`tracker-query:8082`) - Read operations (list, get)
  - Optimized read models
  - Returns HTTP 200 OK with data
  - Eventually consistent with command side

### Event Flow

```
1. Admin calls promote endpoint → PromotedTripService
2. Service validates trip existence
3. Service generates UUID and timestamp
4. Service publishes TripPromotedEvent
5. Returns 202 Accepted with UUID
6. Event handler persists to database (async)
7. WebSocket broadcasts change to connected clients
```

---

## Database Schema

**Table:** `promoted_trips`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY | Unique identifier of the promoted trip record |
| `trip_id` | UUID | NOT NULL, UNIQUE, FK→trips.id (CASCADE) | The trip being promoted |
| `donation_link` | VARCHAR(500) | Nullable | Optional donation URL |
| `promoted_by` | UUID | NOT NULL, FK→users.id (CASCADE) | Admin who promoted the trip |
| `promoted_at` | TIMESTAMP WITH TIME ZONE | NOT NULL | When the trip was promoted |

**Indexes:**
- Primary key index on `id`
- Unique constraint on `trip_id` (prevents duplicate promotions)

---

## API Endpoints

### Base URLs
- **Command Service:** `http://localhost:8081` (write operations)
- **Query Service:** `http://localhost:8082` (read operations)

All endpoints use the `/api/1` prefix.

---

## Command Endpoints (Write Operations)

### 1. Promote a Trip

**Endpoint:** `POST /api/1/trips/{id}/promote`

**Description:** Promotes a trip with an optional donation link. Admin only.

**Authentication:** Required - ADMIN role

**Path Parameters:**
- `id` (UUID, required) - The trip ID to promote

**Request Body:**
```json
{
  "donationLink": "https://example.com/donate"
}
```

**Request Schema:**
| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `donationLink` | String | No | Max 500 chars, Valid URL | Optional donation URL |

**Success Response:**
- **Status Code:** `202 Accepted`
- **Body:**
```json
"3fa85f64-5717-4562-b3fc-2c963f66afa6"
```
- **Description:** Returns the generated UUID of the promoted trip record. The operation completes asynchronously.

**Error Responses:**

| Status Code | Condition | Response Body |
|-------------|-----------|---------------|
| `400 Bad Request` | Invalid donation link URL | `"Donation link must be a valid URL"` |
| `400 Bad Request` | Donation link exceeds 500 characters | `"Donation link must not exceed 500 characters"` |
| `401 Unauthorized` | No authentication token | Empty |
| `403 Forbidden` | User is not ADMIN | `"Access Denied"` |
| `404 Not Found` | Trip ID does not exist | Empty |
| `409 Conflict` | Trip is already promoted | `"Trip is already promoted"` |

**cURL Example:**
```bash
curl -X POST "http://localhost:8081/api/1/trips/123e4567-e89b-12d3-a456-426614174000/promote" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "donationLink": "https://gofundme.com/my-camino-journey"
  }'
```

---

### 2. Unpromote a Trip

**Endpoint:** `DELETE /api/1/trips/{id}/promote`

**Description:** Removes promotion from a trip. Admin only.

**Authentication:** Required - ADMIN role

**Path Parameters:**
- `id` (UUID, required) - The trip ID to unpromote

**Request Body:** None

**Success Response:**
- **Status Code:** `202 Accepted`
- **Body:** Empty
- **Description:** The unpromotion operation is accepted and will complete asynchronously.

**Error Responses:**

| Status Code | Condition | Response Body |
|-------------|-----------|---------------|
| `401 Unauthorized` | No authentication token | Empty |
| `403 Forbidden` | User is not ADMIN | `"Access Denied"` |
| `404 Not Found` | Trip is not promoted | Empty |

**cURL Example:**
```bash
curl -X DELETE "http://localhost:8081/api/1/trips/123e4567-e89b-12d3-a456-426614174000/promote" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 3. Update Donation Link

**Endpoint:** `PUT /api/1/trips/{id}/promote`

**Description:** Updates the donation link for an already promoted trip. Admin only.

**Authentication:** Required - ADMIN role

**Path Parameters:**
- `id` (UUID, required) - The trip ID whose donation link to update

**Request Body:**
```json
{
  "donationLink": "https://example.com/new-donate-link"
}
```

**Request Schema:**
| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `donationLink` | String | No | Max 500 chars, Valid URL | New donation URL (can be null to remove) |

**Success Response:**
- **Status Code:** `202 Accepted`
- **Body:**
```json
"3fa85f64-5717-4562-b3fc-2c963f66afa6"
```
- **Description:** Returns the UUID of the promoted trip record. The update completes asynchronously.

**Error Responses:**

| Status Code | Condition | Response Body |
|-------------|-----------|---------------|
| `400 Bad Request` | Invalid donation link URL | `"Donation link must be a valid URL"` |
| `400 Bad Request` | Donation link exceeds 500 characters | `"Donation link must not exceed 500 characters"` |
| `401 Unauthorized` | No authentication token | Empty |
| `403 Forbidden` | User is not ADMIN | `"Access Denied"` |
| `404 Not Found` | Trip is not promoted | Empty |

**cURL Example:**
```bash
curl -X PUT "http://localhost:8081/api/1/trips/123e4567-e89b-12d3-a456-426614174000/promote" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "donationLink": "https://paypal.me/newlink"
  }'
```

---

## Query Endpoints (Read Operations)

### 4. Get All Promoted Trips

**Endpoint:** `GET /api/1/promoted-trips`

**Description:** Retrieves a list of all currently promoted trips.

**Authentication:** None required (public endpoint)

**Query Parameters:** None

**Success Response:**
- **Status Code:** `200 OK`
- **Body:**
```json
[
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "tripId": "123e4567-e89b-12d3-a456-426614174000",
    "donationLink": "https://example.com/donate",
    "promotedBy": "987e6543-e21b-12d3-a456-426614174000",
    "promotedAt": "2024-01-15T10:30:00Z"
  },
  {
    "id": "4gb96g75-6828-5673-c4gd-3d074g77bgb7",
    "tripId": "234f5678-f90c-23e4-b567-537725285111",
    "donationLink": null,
    "promotedBy": "987e6543-e21b-12d3-a456-426614174000",
    "promotedAt": "2024-01-20T14:45:00Z"
  }
]
```

**Response Schema:**
| Field | Type | Description |
|-------|------|-------------|
| `id` | String (UUID) | Unique identifier of the promoted trip record |
| `tripId` | String (UUID) | The trip being promoted |
| `donationLink` | String or null | Donation URL (null if not provided) |
| `promotedBy` | String (UUID) | Admin who promoted the trip |
| `promotedAt` | String (ISO 8601) | Timestamp when promoted |

**Notes:**
- Returns empty array `[]` if no trips are promoted
- Results are ordered by `promotedAt` (most recent first)

**cURL Example:**
```bash
curl -X GET "http://localhost:8082/api/1/promoted-trips"
```

---

### 5. Get Promotion Info for Specific Trip

**Endpoint:** `GET /api/1/trips/{id}/promotion`

**Description:** Retrieves promotion information for a specific trip.

**Authentication:** None required (public endpoint)

**Path Parameters:**
- `id` (UUID, required) - The trip ID to check

**Success Response:**
- **Status Code:** `200 OK`
- **Body:**
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "tripId": "123e4567-e89b-12d3-a456-426614174000",
  "donationLink": "https://example.com/donate",
  "promotedBy": "987e6543-e21b-12d3-a456-426614174000",
  "promotedAt": "2024-01-15T10:30:00Z"
}
```

**Response Schema:**
| Field | Type | Description |
|-------|------|-------------|
| `id` | String (UUID) | Unique identifier of the promoted trip record |
| `tripId` | String (UUID) | The trip being promoted |
| `donationLink` | String or null | Donation URL (null if not provided) |
| `promotedBy` | String (UUID) | Admin who promoted the trip |
| `promotedAt` | String (ISO 8601) | Timestamp when promoted |

**Error Responses:**

| Status Code | Condition | Response Body |
|-------------|-----------|---------------|
| `404 Not Found` | Trip is not promoted | Empty |

**cURL Example:**
```bash
curl -X GET "http://localhost:8082/api/1/trips/123e4567-e89b-12d3-a456-426614174000/promotion"
```

---

## Data Models

### PromoteTripRequest (Command)

Used for promoting a trip or updating donation link.

```json
{
  "donationLink": "string (optional, max 500 chars, must be valid URL)"
}
```

**Validation:**
- `donationLink` is optional (can be omitted or null)
- If provided, must be a valid URL format
- Maximum length: 500 characters

**Examples:**

Promote with donation link:
```json
{
  "donationLink": "https://www.gofundme.com/my-pilgrimage"
}
```

Promote without donation link:
```json
{
  "donationLink": null
}
```
or
```json
{}
```

---

### PromotedTripResponse (Query)

Returned when querying promoted trips.

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "tripId": "123e4567-e89b-12d3-a456-426614174000",
  "donationLink": "https://example.com/donate",
  "promotedBy": "987e6543-e21b-12d3-a456-426614174000",
  "promotedAt": "2024-01-15T10:30:00Z"
}
```

**Fields:**
- `id` - Unique identifier of the promoted trip record (not the trip itself)
- `tripId` - Reference to the promoted trip
- `donationLink` - Optional donation URL (null if not set)
- `promotedBy` - User ID of the admin who promoted the trip
- `promotedAt` - ISO 8601 timestamp of when the trip was promoted

---

## Authentication & Authorization

### Required Roles

| Endpoint | Method | Required Role | Notes |
|----------|--------|---------------|-------|
| `/trips/{id}/promote` | POST | ADMIN | Promote trip |
| `/trips/{id}/promote` | DELETE | ADMIN | Unpromote trip |
| `/trips/{id}/promote` | PUT | ADMIN | Update donation link |
| `/promoted-trips` | GET | None | Public access |
| `/trips/{id}/promotion` | GET | None | Public access |

### JWT Authentication

All command endpoints require a valid JWT Bearer token in the `Authorization` header:

```
Authorization: Bearer <your-jwt-token>
```

To obtain a JWT token:
1. Register: `POST /api/1/auth/register`
2. Login: `POST /api/1/auth/login`
3. Use the returned token in subsequent requests

**Note:** Only users with ADMIN role can perform write operations. Regular users will receive a `403 Forbidden` response.

---

## Error Handling

### HTTP Status Codes

| Status Code | Meaning | When It Occurs |
|-------------|---------|----------------|
| `200 OK` | Success | Query operations completed successfully |
| `202 Accepted` | Accepted | Command operation accepted (will complete asynchronously) |
| `400 Bad Request` | Invalid input | Validation failed (invalid URL, field too long, etc.) |
| `401 Unauthorized` | Not authenticated | Missing or invalid JWT token |
| `403 Forbidden` | Not authorized | User does not have ADMIN role |
| `404 Not Found` | Resource not found | Trip doesn't exist or trip is not promoted |
| `409 Conflict` | Business rule violation | Trip is already promoted (duplicate promotion) |
| `500 Internal Server Error` | Server error | Unexpected error occurred |

### Common Error Scenarios

#### 1. Trip Already Promoted
**Scenario:** Attempting to promote a trip that's already promoted

**Request:**
```bash
POST /api/1/trips/123e4567-e89b-12d3-a456-426614174000/promote
```

**Response:**
```
HTTP/1.1 409 Conflict
Content-Type: text/plain

Trip is already promoted
```

**Solution:** Use `PUT /api/1/trips/{id}/promote` to update the donation link, or unpromote first.

---

#### 2. Invalid Donation Link
**Scenario:** Providing an invalid URL format

**Request:**
```json
{
  "donationLink": "not-a-valid-url"
}
```

**Response:**
```
HTTP/1.1 400 Bad Request
Content-Type: text/plain

Donation link must be a valid URL
```

**Solution:** Ensure the donation link starts with `http://` or `https://` and is a valid URL.

---

#### 3. Trip Not Found
**Scenario:** Attempting to promote a non-existent trip

**Response:**
```
HTTP/1.1 404 Not Found
```

**Solution:** Verify the trip ID exists before attempting to promote it.

---

#### 4. Trip Not Promoted
**Scenario:** Attempting to unpromote or update a trip that's not promoted

**Response:**
```
HTTP/1.1 404 Not Found
```

**Solution:** Promote the trip first using `POST /api/1/trips/{id}/promote`.

---

#### 5. Unauthorized Access
**Scenario:** Non-admin user attempting to promote a trip

**Response:**
```
HTTP/1.1 403 Forbidden
Content-Type: text/plain

Access Denied
```

**Solution:** Ensure the user has ADMIN role.

---

## CQRS & Eventual Consistency

### Understanding Eventual Consistency

The promoted trips feature uses CQRS architecture, which means:

1. **Commands return 202 Accepted** - Write operations return immediately with a UUID
2. **Persistence happens asynchronously** - Event handlers process the operation in the background
3. **Query side may lag briefly** - There's a small delay between command acceptance and query visibility

### Typical Flow

```
Time: T0 - Admin calls POST /api/1/trips/{id}/promote
     ↓
Time: T0 + 1ms - Service returns 202 Accepted with UUID
     ↓
Time: T0 + 10ms - Event handler persists to database
     ↓
Time: T0 + 15ms - WebSocket broadcasts update
     ↓
Time: T0 + 20ms - GET /api/1/promoted-trips now includes the promoted trip
```

### Best Practices for Frontend

1. **Show loading state** - Display a loading indicator after commands
2. **Use WebSocket updates** - Subscribe to WebSocket events for real-time updates
3. **Poll if needed** - If WebSocket not available, poll query endpoints after 100-200ms
4. **Store returned UUID** - Use the returned UUID to track the operation if needed
5. **Handle 409 Conflict gracefully** - Check if trip is already promoted before retrying

### WebSocket Events

When a trip is promoted/unpromoted, the following events are broadcast:

**Topic:** `/topic/trips/{tripId}`

**Event Types:**
- `TripPromoted` - When a trip is promoted
- `TripUnpromoted` - When a trip is unpromoted
- `DonationLinkUpdated` - When donation link is updated

**Example Event Payload:**
```json
{
  "eventType": "TripPromoted",
  "tripId": "123e4567-e89b-12d3-a456-426614174000",
  "promotedTripId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

## Frontend Integration Guide

### Recommended Workflow

#### Displaying Promoted Trips

```javascript
// Fetch all promoted trips (public - no auth needed)
async function getPromotedTrips() {
  const response = await fetch('http://localhost:8082/api/1/promoted-trips');
  if (response.ok) {
    const promotedTrips = await response.json();
    return promotedTrips;
  }
  return [];
}

// Check if a specific trip is promoted
async function isTripPromoted(tripId) {
  const response = await fetch(`http://localhost:8082/api/1/trips/${tripId}/promotion`);
  return response.ok;
}

// Get promotion details for a trip
async function getPromotionInfo(tripId) {
  const response = await fetch(`http://localhost:8082/api/1/trips/${tripId}/promotion`);
  if (response.ok) {
    return await response.json();
  }
  return null;
}
```

#### Promoting a Trip (Admin Only)

```javascript
async function promoteTrip(tripId, donationLink, jwtToken) {
  const response = await fetch(`http://localhost:8081/api/1/trips/${tripId}/promote`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${jwtToken}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ donationLink })
  });

  if (response.status === 202) {
    const promotedTripId = await response.json();
    console.log('Promotion accepted:', promotedTripId);
    
    // Wait briefly for eventual consistency
    setTimeout(() => {
      // Refresh promoted trips list
      refreshPromotedTrips();
    }, 200);
    
    return promotedTripId;
  } else if (response.status === 409) {
    throw new Error('Trip is already promoted');
  } else if (response.status === 403) {
    throw new Error('Admin access required');
  } else if (response.status === 404) {
    throw new Error('Trip not found');
  } else {
    const error = await response.text();
    throw new Error(error || 'Failed to promote trip');
  }
}
```

#### Unpromoting a Trip (Admin Only)

```javascript
async function unpromoteTrip(tripId, jwtToken) {
  const response = await fetch(`http://localhost:8081/api/1/trips/${tripId}/promote`, {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${jwtToken}`
    }
  });

  if (response.status === 202) {
    console.log('Unpromotion accepted');
    
    // Wait briefly for eventual consistency
    setTimeout(() => {
      // Refresh promoted trips list
      refreshPromotedTrips();
    }, 200);
    
    return true;
  } else if (response.status === 404) {
    throw new Error('Trip is not promoted');
  } else if (response.status === 403) {
    throw new Error('Admin access required');
  } else {
    throw new Error('Failed to unpromote trip');
  }
}
```

#### Updating Donation Link (Admin Only)

```javascript
async function updateDonationLink(tripId, newDonationLink, jwtToken) {
  const response = await fetch(`http://localhost:8081/api/1/trips/${tripId}/promote`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${jwtToken}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ donationLink: newDonationLink })
  });

  if (response.status === 202) {
    const promotedTripId = await response.json();
    console.log('Update accepted:', promotedTripId);
    
    setTimeout(() => {
      refreshPromotedTrips();
    }, 200);
    
    return promotedTripId;
  } else if (response.status === 404) {
    throw new Error('Trip is not promoted');
  } else if (response.status === 400) {
    const error = await response.text();
    throw new Error(error || 'Invalid donation link');
  } else if (response.status === 403) {
    throw new Error('Admin access required');
  } else {
    throw new Error('Failed to update donation link');
  }
}
```

### Error Handling Example

```javascript
async function handlePromoteTrip(tripId, donationLink) {
  try {
    const jwtToken = getAuthToken(); // Your auth token retrieval method
    await promoteTrip(tripId, donationLink, jwtToken);
    
    showSuccessMessage('Trip promoted successfully!');
  } catch (error) {
    if (error.message === 'Trip is already promoted') {
      showWarningMessage('This trip is already promoted. Would you like to update it instead?');
    } else if (error.message === 'Admin access required') {
      showErrorMessage('You do not have permission to promote trips.');
    } else if (error.message.includes('valid URL')) {
      showErrorMessage('Please provide a valid donation URL.');
    } else {
      showErrorMessage('Failed to promote trip. Please try again.');
    }
  }
}
```

### UI Recommendations

1. **Promoted Trips Section**
   - Display promoted trips prominently on the home page
   - Show donation button if `donationLink` is not null
   - Display "Promoted" badge on trip cards
   - Sort by `promotedAt` (most recent first)

2. **Admin Controls**
   - Show promote/unpromote button only for ADMIN users
   - Display current promotion status (promoted/not promoted)
   - Provide inline donation link editor for promoted trips
   - Confirm before unpromoting

3. **Validation**
   - Validate donation URL format client-side before submitting
   - Show character count for donation link (max 500)
   - Disable submit button while request is in flight

4. **Loading States**
   - Show spinner after command operations
   - Display "Promoting..." / "Unpromoting..." messages
   - Auto-refresh list after operation completes

---

## Testing

### Manual Testing with cURL

#### 1. Promote a trip with donation link
```bash
# Replace <ADMIN_TOKEN> with actual JWT token
# Replace <TRIP_ID> with actual trip UUID

curl -X POST "http://localhost:8081/api/1/trips/<TRIP_ID>/promote" \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"donationLink": "https://gofundme.com/test"}'
```

#### 2. Get all promoted trips
```bash
curl -X GET "http://localhost:8082/api/1/promoted-trips"
```

#### 3. Get promotion info for specific trip
```bash
curl -X GET "http://localhost:8082/api/1/trips/<TRIP_ID>/promotion"
```

#### 4. Update donation link
```bash
curl -X PUT "http://localhost:8081/api/1/trips/<TRIP_ID>/promote" \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"donationLink": "https://paypal.me/newlink"}'
```

#### 5. Unpromote a trip
```bash
curl -X DELETE "http://localhost:8081/api/1/trips/<TRIP_ID>/promote" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

### Automated Tests

The following test cases are included in the test suite:

**PromotedTripServiceImplTest:**
- ✅ Successfully promote a trip with donation link
- ✅ Successfully promote a trip without donation link
- ✅ Throw EntityNotFoundException when trip doesn't exist
- ✅ Throw IllegalStateException when trip is already promoted
- ✅ Successfully unpromote a trip
- ✅ Throw EntityNotFoundException when unpromotingא non-promoted trip
- ✅ Successfully update donation link
- ✅ Throw EntityNotFoundException when updating non-promoted trip

**TripControllerTest:**
- ✅ Promote trip endpoint returns 202 Accepted
- ✅ Unpromote trip endpoint returns 202 Accepted
- ✅ Update donation link endpoint returns 202 Accepted
- ✅ Proper authorization checks for ADMIN role

**PromotedTripQueryServiceImplTest:**
- ✅ Get all promoted trips returns list
- ✅ Get promotion info returns correct data
- ✅ Returns empty list when no promotions exist

---

## Swagger/OpenAPI Documentation

Interactive API documentation is available at:

- **Command Service:** `http://localhost:8081/swagger-ui.html`
- **Query Service:** `http://localhost:8082/swagger-ui.html`

Look for:
- **"Trips"** tag - Contains promote/unpromote/update endpoints
- **"Promoted Trip Queries"** tag - Contains query endpoints

---

## FAQ

### Q: Can I promote a trip that doesn't exist?
**A:** No. The trip must exist in the database before it can be promoted. You'll receive a `404 Not Found` error.

### Q: Can the same trip be promoted multiple times?
**A:** No. Each trip can only be promoted once. Attempting to promote an already-promoted trip returns `409 Conflict`. Use the update endpoint to modify the donation link.

### Q: What happens if I delete a promoted trip?
**A:** The promotion is automatically removed due to cascade delete on the foreign key constraint.

### Q: Can I promote a trip without a donation link?
**A:** Yes. The donation link is optional. Simply omit it or set it to `null` in the request.

### Q: How long does it take for a promoted trip to appear in the query results?
**A:** Typically 10-50ms due to the asynchronous event processing. For best user experience, wait 100-200ms before refreshing.

### Q: Can regular users see promoted trips?
**A:** Yes. The query endpoints (`GET /promoted-trips` and `GET /trips/{id}/promotion`) are public and don't require authentication.

### Q: Can regular users promote trips?
**A:** No. Only users with ADMIN role can promote, unpromote, or update promoted trips.

### Q: What is the maximum length for a donation link?
**A:** 500 characters.

### Q: Can I remove a donation link from a promoted trip?
**A:** Yes. Use `PUT /trips/{id}/promote` with `donationLink: null` or an empty request body.

### Q: What URL formats are accepted for donation links?
**A:** Any valid URL that passes standard URL validation. Must start with `http://` or `https://`.

### Q: Can I get a list of trips promoted by a specific admin?
**A:** Not directly. You can fetch all promoted trips and filter by `promotedBy` field on the frontend.

---

## Support & Troubleshooting

### Common Issues

**Issue:** Getting 403 Forbidden when promoting a trip  
**Solution:** Ensure your user account has the ADMIN role

**Issue:** Trip promotion not appearing immediately in query results  
**Solution:** Wait 100-200ms for eventual consistency, or subscribe to WebSocket events

**Issue:** 400 Bad Request on donation link  
**Solution:** Ensure the URL is valid and starts with http:// or https://

**Issue:** 409 Conflict when promoting  
**Solution:** Trip is already promoted. Check promotion status first or use update endpoint

**Issue:** Cannot unpromote a trip  
**Solution:** Verify the trip is actually promoted by checking `GET /trips/{id}/promotion`

---

## Changelog

### Version 0.5.0
- ✅ Initial promoted trips implementation
- ✅ Admin-only promote/unpromote operations
- ✅ Optional donation link support
- ✅ URL validation for donation links
- ✅ Public query endpoints
- ✅ CQRS architecture with event-driven persistence
- ✅ WebSocket real-time updates
- ✅ Comprehensive test coverage

---

## Related Documentation

- [Trip API Documentation](https://github.com/tomassirio/tracker-backend/wiki/Trip-API)
- [Authentication Guide](https://github.com/tomassirio/tracker-backend/wiki/Authentication-API)
- [CQRS Architecture Overview](../README.md#architecture)
- [WebSocket Events](https://github.com/tomassirio/tracker-backend/wiki/WebSocket-Events)

---

**Need Help?** Open an issue on [GitHub](https://github.com/tomassirio/tracker-backend/issues) or check the [Wiki](https://github.com/tomassirio/tracker-backend/wiki).
