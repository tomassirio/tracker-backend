# WebSocket Implementation Documentation

## Overview

This document describes the WebSocket implementation in the tracker-command service, which provides real-time updates to connected clients (e.g., Flutter mobile app) when trip-related events occur.

## Architecture

### Components

1. **WebSocketConfig** - Spring configuration for WebSocket endpoint
2. **TripWebSocketHandler** - Handles WebSocket connections and messages
3. **WebSocketSessionManager** - Manages active sessions and topic subscriptions
4. **WebSocketEventService** - Service for broadcasting events to subscribers
5. **Event Payload DTOs** - Data structures for different event types

### Endpoint

```
ws://localhost:8081/ws?token=<jwt_access_token>
```

For production with TLS:
```
wss://<host>:<port>/ws?token=<jwt_access_token>
```

## Authentication

WebSocket connections are authenticated using JWT tokens passed as a query parameter:

```
ws://localhost:8081/ws?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

The handler:
1. Extracts the `token` query parameter on connection
2. Validates the JWT using the existing `JwtUtils` component
3. Accepts or rejects the connection based on token validity
4. Associates the WebSocket session with the authenticated user

## Message Protocol

All messages are JSON-encoded strings using a simple pub/sub pattern.

### Client → Server Messages

#### SUBSCRIBE
Subscribe to receive events for a specific trip:
```json
{
  "type": "SUBSCRIBE",
  "destination": "/topic/trips/{tripId}"
}
```

#### UNSUBSCRIBE
Stop receiving events for a trip:
```json
{
  "type": "UNSUBSCRIBE",
  "destination": "/topic/trips/{tripId}"
}
```

#### PING
Keep-alive ping (sent every 30 seconds by client):
```json
{
  "type": "PING"
}
```

### Server → Client Messages

#### PONG
Response to PING (plain text, not JSON):
```
PONG
```

## Event Types

All events follow this structure:

```json
{
  "type": "EVENT_TYPE",
  "tripId": "uuid-of-trip",
  "timestamp": "2026-02-12T10:30:00Z",
  "payload": {
    // Event-specific data
  }
}
```

### 1. TRIP_STATUS_CHANGED

Broadcast when a trip's status changes.

```json
{
  "type": "TRIP_STATUS_CHANGED",
  "tripId": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-02-12T10:30:00Z",
  "payload": {
    "tripId": "550e8400-e29b-41d4-a716-446655440000",
    "newStatus": "IN_PROGRESS",
    "previousStatus": "CREATED"
  }
}
```

**Status values:** `CREATED`, `IN_PROGRESS`, `PAUSED`, `FINISHED`

**Triggered by:** `TripServiceImpl.changeStatus()`

### 2. TRIP_UPDATED

Broadcast when a trip receives a new location update.

```json
{
  "type": "TRIP_UPDATED",
  "tripId": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-02-12T10:35:00Z",
  "payload": {
    "tripId": "550e8400-e29b-41d4-a716-446655440000",
    "latitude": 40.7128,
    "longitude": -74.0060,
    "batteryLevel": 85,
    "message": "Just arrived at Times Square!",
    "city": "New York",
    "country": "United States"
  }
}
```

**Triggered by:** `TripUpdateServiceImpl.createTripUpdate()`

### 3. COMMENT_ADDED

Broadcast when someone adds a comment to a trip.

```json
{
  "type": "COMMENT_ADDED",
  "tripId": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-02-12T11:00:00Z",
  "payload": {
    "tripId": "550e8400-e29b-41d4-a716-446655440000",
    "commentId": "660e8400-e29b-41d4-a716-446655440001",
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "userId": "770e8400-e29b-41d4-a716-446655440002",
    "username": "john_doe",
    "message": "Looks amazing! Have fun!",
    "parentCommentId": null
  }
}
```

**Note:** Both `commentId` and `id` fields are provided for frontend compatibility.

**Triggered by:** `CommentServiceImpl.createComment()`

### 4. COMMENT_REACTION_ADDED

Broadcast when someone reacts to a comment.

```json
{
  "type": "COMMENT_REACTION_ADDED",
  "tripId": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-02-12T11:05:00Z",
  "payload": {
    "tripId": "550e8400-e29b-41d4-a716-446655440000",
    "commentId": "660e8400-e29b-41d4-a716-446655440001",
    "reactionType": "LIKE",
    "userId": "880e8400-e29b-41d4-a716-446655440003"
  }
}
```

**Triggered by:** `CommentServiceImpl.addReactionToComment()`

### 5. COMMENT_REACTION_REMOVED

Broadcast when someone removes their reaction from a comment.

```json
{
  "type": "COMMENT_REACTION_REMOVED",
  "tripId": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-02-12T11:10:00Z",
  "payload": {
    "tripId": "550e8400-e29b-41d4-a716-446655440000",
    "commentId": "660e8400-e29b-41d4-a716-446655440001",
    "reactionType": "LIKE",
    "userId": "880e8400-e29b-41d4-a716-446655440003"
  }
}
```

**Triggered by:** `CommentServiceImpl.removeReactionFromComment()`

## Topic Structure

Clients subscribe to trip-specific topics:

```
/topic/trips/{tripId}
```

The backend:
- Tracks which WebSocket sessions are subscribed to which topics
- Broadcasts events to all sessions subscribed to `/topic/trips/{tripId}` when an event occurs for that trip

## Security

### CORS Configuration

WebSocket connections use the same CORS configuration as the REST API, defined in `application.properties`:

```properties
app.cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:51538,http://localhost:3000,https://wanderer.localwanderer-dev.com}
```

Only origins in this list can establish WebSocket connections.

### JWT Validation

- JWT tokens are validated using the same `JwtUtils` component used for REST API authentication
- Invalid tokens result in immediate connection closure with `CloseStatus.POLICY_VIOLATION`
- User ID is extracted from the `sub`, `userId`, or `user_id` claim

## Testing

### Manual Testing with wscat

```bash
# Install wscat
npm install -g wscat

# Connect (replace with your token)
wscat -c "ws://localhost:8081/ws?token=your_jwt_token"

# Subscribe to a trip
> {"type":"SUBSCRIBE","destination":"/topic/trips/550e8400-e29b-41d4-a716-446655440000"}

# Send ping
> {"type":"PING"}

# You should receive: PONG
```

### Unit Tests

- `WebSocketEventServiceTest.java` - Tests event broadcasting functionality
- `WebSocketSessionManagerTest.java` - Tests session and subscription management

Run tests with:
```bash
mvn test -pl tracker-command -Dtest=WebSocket*
```

## Monitoring

### Logging

The WebSocket components use SLF4J logging at INFO level:

- Connection establishment and closure
- Subscriptions and unsubscriptions
- Event broadcasting with subscriber counts
- Authentication failures

### Session Metrics

The `WebSocketSessionManager` provides methods for monitoring:

- `getActiveSessionsCount()` - Number of active WebSocket sessions
- `getSubscribersCount(String topic)` - Number of subscribers for a specific topic

## Performance Considerations

### Thread Safety

- `WebSocketSessionManager` uses thread-safe collections (`ConcurrentHashMap`, `CopyOnWriteArraySet`)
- Suitable for high-concurrency scenarios with many concurrent connections and subscriptions

### Connection Lifecycle

- Sessions are automatically cleaned up on disconnect
- Failed sessions are removed from all topic subscriptions
- Closed or invalid sessions are not sent messages

### Message Broadcasting

- Messages are sent synchronously to all subscribers
- Failed sends are logged but don't block other subscribers
- Dead sessions are automatically removed during broadcast

## Future Enhancements

Potential improvements for the WebSocket implementation:

1. **Rate Limiting** - Prevent message flooding from clients
2. **Message Queue** - Use async messaging for better scalability
3. **Redis Pub/Sub** - Enable WebSocket in multi-instance deployments
4. **Binary Protocol** - Support Protocol Buffers or MessagePack for efficiency
5. **Reconnection Strategy** - Server-side reconnection support with state recovery
6. **Session Compression** - Enable per-message deflate compression
7. **Authentication Refresh** - Support token refresh over WebSocket

## References

- [Spring WebSocket Documentation](https://docs.spring.io/spring-framework/reference/web/websocket.html)
- [WebSocket RFC 6455](https://datatracker.ietf.org/doc/html/rfc6455)
- [Frontend WebSocket Requirements](../docs/WebSocket-Frontend-Requirements.md)
