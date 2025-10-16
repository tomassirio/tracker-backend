# Trip Update API

The Trip Update API allows you to post location updates, battery status, and messages during a trip. These updates track your journey in real-time.

## Command Operations (Port 8081)

**Base URL**: `http://localhost:8081/api/1/trips`

**Authentication**: Required (USER or ADMIN role)

### Create Trip Update

Post a location update for a trip.

**Endpoint**: `POST /api/1/trips/{tripId}/updates`

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| tripId | UUID | Trip's unique identifier |

#### Request Body

```json
{
  "location": {
    "latitude": 42.8805,
    "longitude": -8.5457,
    "altitude": 365.2
  },
  "battery": 85,
  "message": "Just arrived in Santiago! What an amazing journey! 🎉"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| location | GeoLocation | Yes | Current location coordinates |
| location.latitude | number | Yes | Latitude (-90 to 90) |
| location.longitude | number | Yes | Longitude (-180 to 180) |
| location.altitude | number | No | Altitude in meters |
| battery | integer | No | Battery level (0-100) |
| message | string | No | Status message or update text |

#### Response

**Status**: `201 Created`

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
  "message": "Just arrived in Santiago! What an amazing journey! 🎉",
  "reactions": {
    "heart": 0,
    "smiley": 0,
    "sad": 0,
    "laugh": 0,
    "anger": 0
  },
  "timestamp": "2025-10-16T15:30:00Z"
}
```

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Update's unique identifier |
| tripId | UUID | Associated trip ID |
| location | GeoLocation | Location coordinates |
| battery | integer | Battery level |
| message | string | Status message |
| reactions | ReactionsDTO | Reaction counts |
| timestamp | ISO 8601 | When the update was created |

#### Example

```bash
curl -X POST http://localhost:8081/api/1/trips/660e8400-e29b-41d4-a716-446655440000/updates \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "location": {
      "latitude": 42.8805,
      "longitude": -8.5457,
      "altitude": 365.2
    },
    "battery": 85,
    "message": "Made it to Santiago!"
  }'
```

---

## Use Cases

### Location-Only Update

Post just your current location without additional information:

```bash
curl -X POST http://localhost:8081/api/1/trips/660e8400-e29b-41d4-a716-446655440000/updates \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "location": {
      "latitude": 42.8805,
      "longitude": -8.5457
    }
  }'
```

### Update with Message

Share a status message with your location:

```bash
curl -X POST http://localhost:8081/api/1/trips/660e8400-e29b-41d4-a716-446655440000/updates \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "location": {
      "latitude": 43.0000,
      "longitude": -8.2000
    },
    "message": "Taking a rest at this beautiful viewpoint! 🏔️",
    "battery": 65
  }'
```

### Full Update with All Details

Include location, battery, altitude, and message:

```bash
curl -X POST http://localhost:8081/api/1/trips/660e8400-e29b-41d4-a716-446655440000/updates \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "location": {
      "latitude": 42.9500,
      "longitude": -8.3000,
      "altitude": 450.5
    },
    "battery": 72,
    "message": "Climbing up the mountain pass. Great views!"
  }'
```

---

## Integration with OwnTracks

The Trip Update API is designed to work seamlessly with [OwnTracks](https://owntracks.org/), a popular location tracking app:

### OwnTracks Configuration

1. Configure OwnTracks to use HTTP mode
2. Set the endpoint URL to: `http://your-server:8081/api/1/trips/{tripId}/updates`
3. Add authentication header: `Authorization: Bearer <your-token>`

### OwnTracks Payload Mapping

OwnTracks sends location data that maps to the Trip Update format:
- `lat` → `location.latitude`
- `lon` → `location.longitude`
- `alt` → `location.altitude`
- `batt` → `battery`

---

## Error Responses

### 404 Not Found

Trip doesn't exist:
```json
{
  "timestamp": "2025-10-16T10:30:00.000Z",
  "status": 404,
  "error": "Not Found",
  "message": "Trip not found",
  "path": "/api/1/trips/660e8400-e29b-41d4-a716-446655440000/updates"
}
```

### 400 Bad Request

Invalid location data:
```json
{
  "timestamp": "2025-10-16T10:30:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Latitude must be between -90 and 90",
  "path": "/api/1/trips/660e8400-e29b-41d4-a716-446655440000/updates"
}
```

### 403 Forbidden

Cannot update someone else's trip:
```json
{
  "timestamp": "2025-10-16T10:30:00.000Z",
  "status": 403,
  "error": "Forbidden",
  "message": "You don't have permission to update this trip",
  "path": "/api/1/trips/660e8400-e29b-41d4-a716-446655440000/updates"
}
```

---

## Best Practices

### Update Frequency

- **Walking/Hiking**: Every 5-10 minutes
- **Driving**: Every 1-2 minutes
- **Stationary**: Only when location changes significantly

### Battery Efficiency

- Reduce update frequency when battery is low
- Use coarse location accuracy for longer battery life
- Consider disabling updates below certain battery threshold

### Message Guidelines

- Keep messages concise (under 280 characters recommended)
- Use emojis to convey emotion and save characters
- Include relevant context (weather, terrain, mood)

### Offline Handling

When internet connectivity is unavailable:
1. Queue updates locally on the device
2. Retry with exponential backoff
3. Send queued updates when connection is restored

---

## Query Operations (Coming Soon)

The following endpoints for querying trip updates are planned:

- `GET /api/1/trips/{tripId}/updates` - Get all updates for a trip
- `GET /api/1/trips/{tripId}/updates/latest` - Get the most recent update
- `GET /api/1/trips/{tripId}/updates/{updateId}` - Get a specific update

---

## Next Steps

- [Trip API](Trip-API) - Create and manage trips
- [Comment API](Comment-API) - Comment on trip updates
- [Getting Started Guide](Getting-Started-with-APIs) - Full workflow examples
