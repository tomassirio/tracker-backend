# Comment API

The Comment API enables users to add comments and reactions to trips. Comments support one level of nesting (replies to comments, but not replies to replies).

## Command Operations (Port 8081)

**Base URL**: `http://localhost:8081/api/1`

**Authentication**: Required (USER or ADMIN role)

### Add Comment to Trip

Add a top-level comment or reply to an existing comment on a trip.

**Endpoint**: `POST /api/1/trips/{tripId}/comments`

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| tripId | UUID | Trip's unique identifier |

#### Request Body

**Top-level comment:**
```json
{
  "message": "What an amazing journey! Good luck! 🍀"
}
```

**Reply to a comment:**
```json
{
  "message": "Thank you for the support! 🙏",
  "parentCommentId": "990e8400-e29b-41d4-a716-446655440000"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| message | string | Yes | Comment text (max 1000 characters) |
| parentCommentId | UUID | No | ID of parent comment (for replies) |

**Nesting Rules:**
- If `parentCommentId` is null/omitted: Creates a top-level comment
- If `parentCommentId` is provided: Creates a reply to that comment
- Cannot reply to a reply (max depth is 1)

#### Response

**Status**: `201 Created`

```json
{
  "id": "aa0e8400-e29b-41d4-a716-446655440000",
  "tripId": "660e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "parentCommentId": null,
  "message": "What an amazing journey! Good luck! 🍀",
  "reactions": {
    "heart": 0,
    "smiley": 0,
    "sad": 0,
    "laugh": 0,
    "anger": 0
  },
  "replies": [],
  "timestamp": "2025-10-16T15:30:00Z"
}
```

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Comment's unique identifier |
| tripId | UUID | Associated trip ID |
| userId | UUID | Comment author's ID |
| parentCommentId | UUID | Parent comment ID (null for top-level) |
| message | string | Comment text |
| reactions | ReactionsDTO | Reaction counts |
| replies | array | Array of reply CommentDTOs |
| timestamp | ISO 8601 | When the comment was created |

#### Examples

**Add a top-level comment:**
```bash
curl -X POST http://localhost:8081/api/1/trips/660e8400-e29b-41d4-a716-446655440000/comments \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "What an amazing journey! Good luck! 🍀"
  }'
```

**Reply to a comment:**
```bash
curl -X POST http://localhost:8081/api/1/trips/660e8400-e29b-41d4-a716-446655440000/comments \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Thank you for the support! 🙏",
    "parentCommentId": "990e8400-e29b-41d4-a716-446655440000"
  }'
```

---

### Add Reaction to Comment

Add an emoji reaction to a comment or reply.

**Endpoint**: `POST /api/1/comments/{commentId}/reactions`

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| commentId | UUID | Comment's unique identifier |

#### Request Body

```json
{
  "reactionType": "HEART"
}
```

| Field | Type | Required | Options |
|-------|------|----------|---------|
| reactionType | enum | Yes | HEART, SMILEY, SAD, LAUGH, ANGER |

**Reaction Types:**
- **HEART**: ❤️ - Show love and appreciation
- **SMILEY**: 😊 - Express happiness and positivity
- **SAD**: 😢 - Show sympathy or sadness
- **LAUGH**: 😂 - React to something funny
- **ANGER**: 😠 - Express frustration or anger

#### Response

**Status**: `200 OK`

Returns the updated comment object with incremented reaction count.

```json
{
  "id": "aa0e8400-e29b-41d4-a716-446655440000",
  "tripId": "660e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "parentCommentId": null,
  "message": "What an amazing journey! Good luck! 🍀",
  "reactions": {
    "heart": 5,
    "smiley": 2,
    "sad": 0,
    "laugh": 1,
    "anger": 0
  },
  "replies": [],
  "timestamp": "2025-10-16T15:30:00Z"
}
```

#### Example

```bash
curl -X POST http://localhost:8081/api/1/comments/aa0e8400-e29b-41d4-a716-446655440000/reactions \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "reactionType": "HEART"
  }'
```

---

### Remove Reaction from Comment

Remove a previously added reaction from a comment.

**Endpoint**: `DELETE /api/1/comments/{commentId}/reactions`

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| commentId | UUID | Comment's unique identifier |

#### Request Body

```json
{
  "reactionType": "HEART"
}
```

| Field | Type | Required | Options |
|-------|------|----------|---------|
| reactionType | enum | Yes | HEART, SMILEY, SAD, LAUGH, ANGER |

#### Response

**Status**: `200 OK`

Returns the updated comment object with decremented reaction count.

#### Example

```bash
curl -X DELETE http://localhost:8081/api/1/comments/aa0e8400-e29b-41d4-a716-446655440000/reactions \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "reactionType": "HEART"
  }'
```

---

## Query Operations (Port 8082)

**Base URL**: `http://localhost:8082/api/1`

### Get Comment by ID

Retrieve a specific comment by its UUID.

**Endpoint**: `GET /api/1/comments/{id}`

**Authentication**: Required (USER or ADMIN role)

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| id | UUID | Comment's unique identifier |

#### Response

**Status**: `200 OK`

Returns the comment object including its replies.

```json
{
  "id": "aa0e8400-e29b-41d4-a716-446655440000",
  "tripId": "660e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "parentCommentId": null,
  "message": "What an amazing journey! Good luck! 🍀",
  "reactions": {
    "heart": 5,
    "smiley": 2,
    "sad": 0,
    "laugh": 1,
    "anger": 0
  },
  "replies": [
    {
      "id": "bb0e8400-e29b-41d4-a716-446655440000",
      "tripId": "660e8400-e29b-41d4-a716-446655440000",
      "userId": "660e8400-e29b-41d4-a716-446655440000",
      "parentCommentId": "aa0e8400-e29b-41d4-a716-446655440000",
      "message": "Thank you! 🙏",
      "reactions": {...},
      "replies": [],
      "timestamp": "2025-10-16T15:35:00Z"
    }
  ],
  "timestamp": "2025-10-16T15:30:00Z"
}
```

#### Example

```bash
curl -X GET http://localhost:8082/api/1/comments/aa0e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <your-token>"
```

---

### Get All Comments for Trip

Retrieve all top-level comments and their replies for a specific trip.

**Endpoint**: `GET /api/1/trips/{tripId}/comments`

**Authentication**: Required (USER or ADMIN role)

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| tripId | UUID | Trip's unique identifier |

#### Response

**Status**: `200 OK`

Returns an array of top-level comment objects, each containing its replies.

```json
[
  {
    "id": "aa0e8400-e29b-41d4-a716-446655440000",
    "tripId": "660e8400-e29b-41d4-a716-446655440000",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "parentCommentId": null,
    "message": "What an amazing journey! Good luck! 🍀",
    "reactions": {
      "heart": 5,
      "smiley": 2,
      "sad": 0,
      "laugh": 1,
      "anger": 0
    },
    "replies": [
      {
        "id": "bb0e8400-e29b-41d4-a716-446655440000",
        "message": "Thank you! 🙏",
        ...
      }
    ],
    "timestamp": "2025-10-16T15:30:00Z"
  },
  {
    "id": "cc0e8400-e29b-41d4-a716-446655440000",
    "tripId": "660e8400-e29b-41d4-a716-446655440000",
    "userId": "770e8400-e29b-41d4-a716-446655440000",
    "parentCommentId": null,
    "message": "Stay safe out there! 🌟",
    "reactions": {
      "heart": 3,
      "smiley": 1,
      "sad": 0,
      "laugh": 0,
      "anger": 0
    },
    "replies": [],
    "timestamp": "2025-10-16T16:00:00Z"
  }
]
```

#### Example

```bash
curl -X GET http://localhost:8082/api/1/trips/660e8400-e29b-41d4-a716-446655440000/comments \
  -H "Authorization: Bearer <your-token>"
```

---

## Comment Structure

### Nesting Hierarchy

```
Trip
├── Comment 1 (top-level)
│   ├── Reply 1.1
│   ├── Reply 1.2
│   └── Reply 1.3
├── Comment 2 (top-level)
│   └── Reply 2.1
└── Comment 3 (top-level)
```

**Important:** Replies cannot have replies (max depth = 1).

### Reaction Counts

Each comment tracks reaction counts separately:

```json
{
  "reactions": {
    "heart": 12,    // Number of ❤️ reactions
    "smiley": 5,    // Number of 😊 reactions
    "sad": 1,       // Number of 😢 reactions
    "laugh": 8,     // Number of 😂 reactions
    "anger": 0      // Number of 😠 reactions
  }
}
```

---

## Error Responses

### 404 Not Found

Comment or trip doesn't exist:
```json
{
  "timestamp": "2025-10-16T10:30:00.000Z",
  "status": 404,
  "error": "Not Found",
  "message": "Comment not found",
  "path": "/api/1/comments/aa0e8400-e29b-41d4-a716-446655440000"
}
```

### 400 Bad Request

Cannot reply to a reply (exceeds nesting depth):
```json
{
  "timestamp": "2025-10-16T10:30:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Cannot reply to a reply. Maximum nesting depth is 1",
  "path": "/api/1/trips/660e8400-e29b-41d4-a716-446655440000/comments"
}
```

Message too long:
```json
{
  "timestamp": "2025-10-16T10:30:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Comment message must not exceed 1000 characters",
  "path": "/api/1/trips/660e8400-e29b-41d4-a716-446655440000/comments"
}
```

---

## Best Practices

### Comment Guidelines

- **Be Respectful**: Keep comments positive and supportive
- **Stay On-Topic**: Comments should relate to the trip
- **Length**: Keep comments concise (under 500 characters recommended)
- **Emojis**: Use emojis to express emotions and save characters

### Reaction Usage

- **Quick Feedback**: Reactions are faster than comments
- **Multiple Reactions**: Users can add multiple different reactions
- **Changing Reactions**: Remove old reaction, then add new one
- **Reaction Etiquette**: Use appropriate reactions for the context

### Threading

- **Top-Level Comments**: Use for new topics or observations
- **Replies**: Use to respond to specific comments
- **Mention Users**: Consider adding @username in replies
- **Context**: Keep reply context clear since depth is limited

---

## Usage Examples

### Complete Comment Workflow

```bash
# 1. Get trip comments
curl -X GET http://localhost:8082/api/1/trips/660e8400-e29b-41d4-a716-446655440000/comments \
  -H "Authorization: Bearer <your-token>"

# 2. Add a comment
COMMENT_RESPONSE=$(curl -X POST http://localhost:8081/api/1/trips/660e8400-e29b-41d4-a716-446655440000/comments \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{"message": "Great progress today!"}')

COMMENT_ID=$(echo $COMMENT_RESPONSE | jq -r '.id')

# 3. Add a reaction
curl -X POST http://localhost:8081/api/1/comments/$COMMENT_ID/reactions \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{"reactionType": "HEART"}'

# 4. Reply to the comment
curl -X POST http://localhost:8081/api/1/trips/660e8400-e29b-41d4-a716-446655440000/comments \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d "{\"message\": \"Thanks for the support!\", \"parentCommentId\": \"$COMMENT_ID\"}"
```

---

## Next Steps

- [Trip API](Trip-API) - Create and manage trips
- [Trip Update API](Trip-Update-API) - Add location updates
- [Getting Started Guide](Getting-Started-with-APIs) - Full workflow examples
