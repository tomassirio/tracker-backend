# Frontend Integration Guide: Comment Reactions One-Per-User

## Overview
The backend now enforces a **one emoji reaction per user per comment** rule with automatic replacement. This document explains the new behavior and what the frontend team needs to implement.

## Backend Changes Summary

### What Changed
1. **Individual Reaction Tracking**: Each reaction is now stored as a separate entity with user information
2. **Automatic Replacement**: When a user adds a different reaction, the old one is automatically removed
3. **Same Reaction Prevention**: Attempting to add the same reaction twice is rejected with HTTP 409 Conflict
4. **Enhanced API Response**: Comments now include both aggregated counts AND individual reaction details

### Database Schema
New table: `comment_reactions`
- `id` (UUID)
- `comment_id` (UUID, FK to comments)
- `user_id` (UUID, FK to users)
- `reaction_type` (VARCHAR: HEART, SMILEY, SAD, LAUGH, ANGER)
- `timestamp` (TIMESTAMP WITH TIME ZONE)
- **Unique constraint on (comment_id, user_id)** - enforces one reaction per user

## API Changes

### Updated Response Format

#### Before (Aggregated Only)
```json
{
  "id": "comment-uuid",
  "userId": "user-uuid",
  "username": "john_doe",
  "tripId": "trip-uuid",
  "parentCommentId": null,
  "message": "Great trip!",
  "reactions": {
    "heart": 5,
    "smiley": 2,
    "sad": 0,
    "laugh": 1,
    "anger": 0
  },
  "replies": [],
  "timestamp": "2026-02-28T16:00:00Z"
}
```

#### After (Aggregated + Individual)
```json
{
  "id": "comment-uuid",
  "userId": "user-uuid",
  "username": "john_doe",
  "tripId": "trip-uuid",
  "parentCommentId": null,
  "message": "Great trip!",
  "reactions": {
    "heart": 5,
    "smiley": 2,
    "sad": 0,
    "laugh": 1,
    "anger": 0
  },
  "individualReactions": [
    {
      "userId": "user1-uuid",
      "username": "alice",
      "reactionType": "HEART",
      "timestamp": "2026-02-28T15:30:00Z"
    },
    {
      "userId": "user2-uuid",
      "username": "bob",
      "reactionType": "HEART",
      "timestamp": "2026-02-28T15:45:00Z"
    },
    {
      "userId": "user3-uuid",
      "username": "charlie",
      "reactionType": "SMILEY",
      "timestamp": "2026-02-28T15:50:00Z"
    }
  ],
  "replies": [],
  "timestamp": "2026-02-28T16:00:00Z"
}
```

### Existing Endpoints (Behavior Updated)

#### POST /api/1/comments/{commentId}/reactions
Add a reaction to a comment.

**Request Body:**
```json
{
  "reactionType": "HEART"
}
```

**Behavior Changes:**
1. **New Reaction**: If user has no existing reaction → adds the reaction (HTTP 202)
2. **Same Reaction**: If user already has the same reaction → returns HTTP 409 Conflict
3. **Different Reaction**: If user has a different reaction → removes old, adds new (HTTP 202)

**Responses:**
- `202 Accepted`: Reaction added/changed successfully
- `400 Bad Request`: Invalid reaction type
- `404 Not Found`: Comment not found
- `409 Conflict`: User already has this reaction on the comment (new!)

**Example Error Response (409 Conflict):**
```json
{
  "timestamp": "2026-02-28T16:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "User already has this reaction on the comment"
}
```

#### DELETE /api/1/comments/{commentId}/reactions
Remove a reaction from a comment (unchanged behavior).

**Request Body:**
```json
{
  "reactionType": "HEART"
}
```

**Response:** `202 Accepted`

### WebSocket Events

#### COMMENT_REACTION_ADDED
Emitted when a reaction is added (and there was no previous reaction from this user).

```json
{
  "eventType": "COMMENT_REACTION_ADDED",
  "payload": {
    "tripId": "trip-uuid",
    "commentId": "comment-uuid",
    "reactionType": "HEART",
    "userId": "user-uuid"
  }
}
```

#### COMMENT_REACTION_REMOVED
Emitted when a reaction is explicitly removed by the user.

```json
{
  "eventType": "COMMENT_REACTION_REMOVED",
  "payload": {
    "tripId": "trip-uuid",
    "commentId": "comment-uuid",
    "reactionType": "SMILEY",
    "userId": "user-uuid"
  }
}
```

#### COMMENT_REACTION_REPLACED (NEW!)
Emitted when a user changes their reaction from one type to another. This is a **single event** that contains both the old and new reaction types.

```json
{
  "eventType": "COMMENT_REACTION_REPLACED",
  "payload": {
    "tripId": "trip-uuid",
    "commentId": "comment-uuid",
    "reactionType": "SMILEY",       // New reaction
    "previousReactionType": "HEART", // Old reaction
    "userId": "user-uuid"
  }
}
```

**Important:** When handling this event, the frontend should:
1. Decrement the count for `previousReactionType`
2. Increment the count for `reactionType`
3. Update the `individualReactions` array by replacing the user's old reaction with the new one

## Frontend Implementation Guide

### 1. Update Comment Model
```typescript
interface Reaction {
  userId: string;
  username: string;
  reactionType: 'HEART' | 'SMILEY' | 'SAD' | 'LAUGH' | 'ANGER';
  timestamp: string;
}

interface Comment {
  id: string;
  userId: string;
  username: string;
  tripId: string;
  parentCommentId: string | null;
  message: string;
  reactions: {
    heart: number;
    smiley: number;
    sad: number;
    laugh: number;
    anger: number;
  };
  individualReactions: Reaction[];  // NEW!
  replies: Comment[];
  timestamp: string;
}
```

### 2. Display User's Current Reaction
Use `individualReactions` to find the current user's reaction:

```typescript
function getUserReaction(comment: Comment, currentUserId: string): string | null {
  const userReaction = comment.individualReactions.find(
    reaction => reaction.userId === currentUserId
  );
  return userReaction ? userReaction.reactionType : null;
}
```

### 3. UI/UX Changes

#### Visual Indicator
- **Highlight** the emoji the current user has reacted with (e.g., different color, border, or fill)
- Show "you" or username in tooltip when hovering over reaction counts

#### Click Behavior
```typescript
async function handleReactionClick(
  commentId: string, 
  reactionType: string, 
  currentUserReaction: string | null
) {
  if (currentUserReaction === reactionType) {
    // User clicked their existing reaction → remove it
    await removeReaction(commentId, reactionType);
  } else if (currentUserReaction) {
    // User clicked a different reaction → backend will auto-replace
    await addReaction(commentId, reactionType);
  } else {
    // User has no reaction → add new one
    await addReaction(commentId, reactionType);
  }
}
```

#### Error Handling
Handle 409 Conflict gracefully (shouldn't happen in normal use due to UI logic):
```typescript
try {
  await addReaction(commentId, reactionType);
} catch (error) {
  if (error.status === 409) {
    console.warn('User already has this reaction');
    // Optionally: show a brief message or refresh comment data
  }
}
```

### 4. WebSocket Handling

Update your WebSocket handler to process all three event types:

```typescript
socket.on('COMMENT_REACTION_ADDED', (payload) => {
  // User added a new reaction (no previous reaction)
  const comment = findCommentById(payload.commentId);
  
  // Add to individual reactions
  comment.individualReactions.push({
    userId: payload.userId,
    username: payload.username, // You may need to fetch this
    reactionType: payload.reactionType,
    timestamp: new Date().toISOString()
  });
  
  // Increment aggregated count
  const reactionKey = payload.reactionType.toLowerCase();
  comment.reactions[reactionKey]++;
});

socket.on('COMMENT_REACTION_REMOVED', (payload) => {
  // User explicitly removed their reaction
  const comment = findCommentById(payload.commentId);
  
  // Remove from individual reactions
  comment.individualReactions = comment.individualReactions.filter(
    r => !(r.userId === payload.userId && r.reactionType === payload.reactionType)
  );
  
  // Decrement aggregated count
  const reactionKey = payload.reactionType.toLowerCase();
  comment.reactions[reactionKey]--;
});

socket.on('COMMENT_REACTION_REPLACED', (payload) => {
  // User changed their reaction from one type to another
  const comment = findCommentById(payload.commentId);
  
  // Update individual reactions - replace old with new
  const existingIndex = comment.individualReactions.findIndex(
    r => r.userId === payload.userId
  );
  
  if (existingIndex !== -1) {
    comment.individualReactions[existingIndex] = {
      userId: payload.userId,
      username: comment.individualReactions[existingIndex].username,
      reactionType: payload.reactionType,
      timestamp: new Date().toISOString()
    };
  }
  
  // Update aggregated counts
  const oldKey = payload.previousReactionType.toLowerCase();
  const newKey = payload.reactionType.toLowerCase();
  comment.reactions[oldKey]--;
  comment.reactions[newKey]++;
});
```

### 5. Reaction Tooltip/Popover
When user hovers over a reaction count, show who reacted:

```typescript
function getReactionUsers(comment: Comment, reactionType: string): string[] {
  return comment.individualReactions
    .filter(r => r.reactionType === reactionType)
    .map(r => r.username);
}

// Example: "Alice, Bob, Charlie, and 2 others reacted with ❤️"
```

## Testing Checklist

### Unit Tests
- [ ] getUserReaction returns correct reaction for current user
- [ ] getUserReaction returns null when user has no reaction
- [ ] handleReactionClick calls removeReaction when clicking existing reaction
- [ ] handleReactionClick calls addReaction when clicking different reaction

### Integration Tests
- [ ] Adding first reaction updates UI correctly
- [ ] Clicking same reaction removes it
- [ ] Clicking different reaction replaces old one
- [ ] WebSocket events update reactions in real-time
- [ ] Reaction counts match individual reactions
- [ ] User's reaction is visually highlighted

### Edge Cases
- [ ] Multiple users reacting at the same time
- [ ] User rapidly clicking different reactions
- [ ] Offline mode / reconnection handling
- [ ] 409 error handling (edge case)

## Migration Notes

### Backward Compatibility
- **Aggregated counts** (`reactions` field) continue to work exactly as before
- **New field** (`individualReactions`) is always present (empty array if no reactions)
- Old clients that don't use `individualReactions` will still function with aggregated counts

### Deployment
No special migration needed:
1. Deploy backend
2. Database migration runs automatically via Liquibase
3. Existing reactions remain as aggregated counts (no individual data for old reactions)
4. New reactions are tracked individually from deployment onwards

## Questions?

For questions or issues:
- Check the [GitHub Wiki](https://github.com/tomassirio/tracker-backend/wiki) for full API documentation
- Review test files in `tracker-command/src/test/java/.../CommentServiceImplTest.java`
- Open an issue in the repository

---

**Summary**: Users can now have exactly one emoji reaction per comment. The backend automatically replaces reactions when users select a different one, and prevents duplicate reactions. The frontend should visually indicate which reaction the user has selected and handle the remove-on-click behavior.
