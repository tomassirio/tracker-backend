# Additional WebSocket Event Opportunities

## Overview

This document identifies additional operations in the tracker-command service that could benefit from real-time WebSocket notifications beyond the currently implemented events.

## Currently Implemented Events

1. **TRIP_STATUS_CHANGED** - Trip status changes (TripService.changeStatus)
2. **TRIP_UPDATED** - Location updates (TripUpdateService.createTripUpdate)
3. **COMMENT_ADDED** - Comment creation (CommentService.createComment)
4. **COMMENT_REACTION_ADDED** - Reaction added (CommentService.addReactionToComment)
5. **COMMENT_REACTION_REMOVED** - Reaction removed (CommentService.removeReactionFromComment)

## High Priority Event Opportunities

### 1. Friend Request Events (FriendRequestService)

**Services:** `tracker-command/service/FriendRequestService.java`

**Operations to broadcast:**
- `sendFriendRequest()` → **FRIEND_REQUEST_SENT** / **FRIEND_REQUEST_RECEIVED**
- `acceptFriendRequest()` → **FRIEND_REQUEST_ACCEPTED**
- `declineFriendRequest()` → **FRIEND_REQUEST_DECLINED**

**Rationale:** Users need real-time notifications when they receive friend requests or when their requests are accepted/declined. This is critical for social interaction flow.

**Event Structure:**
```json
{
  "type": "FRIEND_REQUEST_RECEIVED",
  "userId": "target-user-id",
  "timestamp": "2026-02-12T10:00:00Z",
  "payload": {
    "requestId": "uuid",
    "fromUserId": "sender-user-id",
    "fromUsername": "john_doe",
    "status": "PENDING"
  }
}
```

### 2. User Follow Events (UserFollowService)

**Services:** `tracker-command/service/UserFollowService.java`

**Operations to broadcast:**
- `followUser()` → **USER_FOLLOWED**
- `unfollowUser()` → **USER_UNFOLLOWED**

**Rationale:** Users want to know when someone starts following them. Also important for updating follower counts in real-time.

**Event Structure:**
```json
{
  "type": "USER_FOLLOWED",
  "userId": "followed-user-id",
  "timestamp": "2026-02-12T10:00:00Z",
  "payload": {
    "followerId": "follower-user-id",
    "followerUsername": "jane_smith"
  }
}
```

### 3. Trip Lifecycle Events (TripService)

**Services:** `tracker-command/service/TripService.java`

**Operations to broadcast:**
- `createTrip()` → **TRIP_CREATED**
- `updateTrip()` → **TRIP_UPDATED** (different from location update)
- `deleteTrip()` → **TRIP_DELETED**
- `changeVisibility()` → **TRIP_VISIBILITY_CHANGED**

**Rationale:**
- **TRIP_CREATED**: Followers/friends want to know when someone creates a new trip
- **TRIP_UPDATED**: Name changes or other metadata changes
- **TRIP_DELETED**: Collaborators/followers should know when a trip is deleted
- **TRIP_VISIBILITY_CHANGED**: Important for access control - users should know if a trip becomes private

**Event Structure:**
```json
{
  "type": "TRIP_CREATED",
  "userId": "trip-owner-id",
  "timestamp": "2026-02-12T10:00:00Z",
  "payload": {
    "tripId": "uuid",
    "tripName": "Santiago de Compostela",
    "visibility": "PUBLIC",
    "ownerId": "uuid",
    "ownerUsername": "pilgrim_john"
  }
}
```

## Medium Priority Event Opportunities

### 4. Friendship Events (FriendshipService)

**Services:** `tracker-command/service/FriendshipService.java`

**Operations to broadcast:**
- `createFriendship()` → **FRIENDSHIP_CREATED** (when request accepted)
- `removeFriendship()` → **FRIENDSHIP_REMOVED**

**Rationale:** Real-time friend list updates. Less critical than friend requests since these are usually triggered by the request events.

### 5. Trip Plan Events (TripPlanService)

**Services:** `tracker-command/service/TripPlanService.java`

**Operations to broadcast:**
- `createTripPlan()` → **TRIP_PLAN_CREATED**
- `updateTripPlan()` → **TRIP_PLAN_UPDATED**
- `deleteTripPlan()` → **TRIP_PLAN_DELETED**

**Rationale:** Useful if trip plans are collaborative or shared. Lower priority than trip events themselves.

## Implementation Pattern

With the new event-driven architecture, adding new WebSocket events follows this pattern:

### 1. Create Domain Event
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestReceivedEvent {
    private UUID requestId;
    private UUID fromUserId;
    private String fromUsername;
    private UUID toUserId;
    private String status;
}
```

### 2. Create WebSocket Payload
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestPayload {
    private UUID requestId;
    private UUID fromUserId;
    private String fromUsername;
    private String status;
}
```

### 3. Update WebSocketEventService
```java
public void broadcastFriendRequestReceived(FriendRequestPayload payload) {
    // Broadcast to the receiving user's topic
    String topic = "/topic/users/" + payload.getToUserId();
    WebSocketEvent event = WebSocketEvent.create(
        "FRIEND_REQUEST_RECEIVED", 
        payload.getToUserId(), 
        payload
    );
    broadcastToTopic(topic, event);
}
```

### 4. Add Event Listener Handler
```java
@Async
@EventListener
public void handleFriendRequestReceived(FriendRequestReceivedEvent event) {
    FriendRequestPayload payload = FriendRequestPayload.builder()
        .requestId(event.getRequestId())
        .fromUserId(event.getFromUserId())
        .fromUsername(event.getFromUsername())
        .status(event.getStatus())
        .build();
    webSocketEventService.broadcastFriendRequestReceived(payload);
}
```

### 5. Publish Event from Service
```java
@Transactional
public FriendRequestDTO sendFriendRequest(UUID fromUserId, UUID toUserId) {
    // ... business logic ...
    
    eventPublisher.publishEvent(
        FriendRequestReceivedEvent.builder()
            .requestId(request.getId())
            .fromUserId(fromUserId)
            .fromUsername(fromUser.getUsername())
            .toUserId(toUserId)
            .status("PENDING")
            .build()
    );
    
    return result;
}
```

## Topic Structure Recommendations

### Current: Trip-Based Topics
```
/topic/trips/{tripId}
```

### Proposed: User-Based Topics for Personal Notifications
```
/topic/users/{userId}          # Personal notifications (friend requests, follows)
/topic/trips/{tripId}          # Trip-specific updates (existing)
/topic/friendships/{userId}    # Friendship activity for user's network
```

This allows:
- Users to subscribe to their personal notification feed
- Users to subscribe to specific trips they're following
- Efficient broadcasting to relevant subscribers only

## Benefits of Event-Driven Architecture

The new Spring Events-based architecture makes adding these events straightforward:

1. **Decoupled**: Services don't depend on WebSocket implementation
2. **Extensible**: Add email, push notifications, or other handlers alongside WebSocket
3. **Testable**: Services can be tested without WebSocket infrastructure
4. **Async**: Event broadcasting doesn't block database transactions
5. **Maintainable**: Each event type is clearly defined with its own class

## Recommended Implementation Order

1. **Phase 1 (Immediate)**:
   - Friend request events (high user value)
   - User follow events (social engagement)

2. **Phase 2 (Near-term)**:
   - Trip lifecycle events (trip created, deleted)
   - Trip visibility changes

3. **Phase 3 (Future)**:
   - Trip plan events (if collaboration is added)
   - Friendship events (duplicate of request events)

## Testing Considerations

For each new event type:
1. Unit test the domain event class
2. Unit test the event listener handler
3. Integration test the full flow from service to WebSocket broadcast
4. Manual test with wscat or Flutter frontend

## Security Considerations

- Ensure events are only broadcast to authorized users
- User-specific topics (`/topic/users/{userId}`) should verify the subscriber's identity
- Trip topics should respect trip visibility settings
- Consider rate limiting to prevent event flooding
