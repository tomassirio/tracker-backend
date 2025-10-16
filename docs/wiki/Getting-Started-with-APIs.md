# Getting Started with APIs

This guide will walk you through common workflows and examples to help you get started with the Trip Tracker Backend API.

## Prerequisites

- Basic understanding of REST APIs
- Ability to make HTTP requests (using curl, Postman, or your preferred tool)
- (Optional) `jq` for parsing JSON in command-line examples

## Quick Start

### 1. Start the Services

Make sure all services are running:

```bash
# Auth Service (Port 8083)
cd tracker-auth && mvn spring-boot:run

# Command Service (Port 8081)
cd tracker-command && mvn spring-boot:run

# Query Service (Port 8082)
cd tracker-query && mvn spring-boot:run
```

Or using Docker:

```bash
docker-compose up
```

### 2. Register a New User

Create your account and receive a JWT token:

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

Save the token - you'll need it for authenticated requests!

### 3. Create Your First Trip

```bash
export TOKEN="<your-token-from-step-2>"

curl -X POST http://localhost:8081/api/1/trips \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Camino Journey",
    "visibility": "PUBLIC",
    "startDate": "2025-11-01T08:00:00Z"
  }'
```

**Response:**
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440000",
  "name": "My Camino Journey",
  "tripSettings": {
    "visibility": "PUBLIC",
    "status": "CREATED"
  },
  ...
}
```

Save the trip ID for the next steps!

### 4. Post Your First Location Update

```bash
export TRIP_ID="<trip-id-from-step-3>"

curl -X POST http://localhost:8081/api/1/trips/$TRIP_ID/updates \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "location": {
      "latitude": 43.2630,
      "longitude": -2.9350
    },
    "message": "Starting my journey! 🎒",
    "battery": 95
  }'
```

### 5. Query Your Trips

```bash
curl -X GET http://localhost:8082/api/1/trips/me \
  -H "Authorization: Bearer $TOKEN"
```

Congratulations! You've completed your first API workflow. 🎉

---

## Complete Workflows

### Workflow 1: Plan and Execute a Trip

#### Step 1: Create a Trip Plan

```bash
curl -X POST http://localhost:8081/api/1/trips/plans \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Camino Francés Route",
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
      "waypoints": [
        {
          "name": "Pamplona",
          "location": {"latitude": 42.8125, "longitude": -1.6458},
          "day": 3
        }
      ]
    }
  }' | jq -r '.id'
```

#### Step 2: Create a Trip Using the Plan

```bash
export PLAN_ID="<plan-id-from-step-1>"

curl -X POST http://localhost:8081/api/1/trips \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"My Camino 2025\",
    \"tripPlanId\": \"$PLAN_ID\",
    \"visibility\": \"PUBLIC\"
  }" | jq -r '.id'
```

#### Step 3: Start the Trip

```bash
export TRIP_ID="<trip-id-from-step-2>"

curl -X PATCH http://localhost:8081/api/1/trips/$TRIP_ID/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "IN_PROGRESS"
  }'
```

#### Step 4: Post Regular Updates

```bash
# Day 1 - Starting point
curl -X POST http://localhost:8081/api/1/trips/$TRIP_ID/updates \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "location": {"latitude": 43.1631, "longitude": -1.2350},
    "message": "Day 1: Starting from Saint-Jean-Pied-de-Port! 🥾",
    "battery": 100
  }'

# Day 3 - Reached waypoint
curl -X POST http://localhost:8081/api/1/trips/$TRIP_ID/updates \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "location": {"latitude": 42.8125, "longitude": -1.6458},
    "message": "Day 3: Made it to Pamplona! 🎉",
    "battery": 65
  }'
```

#### Step 5: Complete the Trip

```bash
curl -X PATCH http://localhost:8081/api/1/trips/$TRIP_ID/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "FINISHED"
  }'
```

---

### Workflow 2: Social Interaction

#### Step 1: View Public Trips

```bash
curl -X GET http://localhost:8082/api/1/trips/public
```

#### Step 2: Add a Comment

```bash
export OTHER_TRIP_ID="<trip-id-you-want-to-comment-on>"

curl -X POST http://localhost:8081/api/1/trips/$OTHER_TRIP_ID/comments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Great journey! Stay safe! 🌟"
  }' | jq -r '.id'
```

#### Step 3: Add a Reaction

```bash
export COMMENT_ID="<comment-id-from-step-2>"

curl -X POST http://localhost:8081/api/1/comments/$COMMENT_ID/reactions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "reactionType": "HEART"
  }'
```

#### Step 4: Reply to Comments

```bash
curl -X POST http://localhost:8081/api/1/trips/$OTHER_TRIP_ID/comments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"message\": \"Thank you for the support! 🙏\",
    \"parentCommentId\": \"$COMMENT_ID\"
  }"
```

#### Step 5: View Trip Comments

```bash
curl -X GET http://localhost:8082/api/1/trips/$OTHER_TRIP_ID/comments \
  -H "Authorization: Bearer $TOKEN"
```

---

### Workflow 3: Privacy Management

#### Step 1: Create a Private Trip

```bash
curl -X POST http://localhost:8081/api/1/trips \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Private Practice Hike",
    "visibility": "PRIVATE"
  }' | jq -r '.id'
```

#### Step 2: Change to Protected (Authenticated Users Only)

```bash
export TRIP_ID="<trip-id-from-step-1>"

curl -X PATCH http://localhost:8081/api/1/trips/$TRIP_ID/visibility \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "visibility": "PROTECTED"
  }'
```

#### Step 3: Make it Public

```bash
curl -X PATCH http://localhost:8081/api/1/trips/$TRIP_ID/visibility \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "visibility": "PUBLIC"
  }'
```

---

## Using with Different Tools

### cURL

We've been using cURL throughout this guide. Here's a template:

```bash
curl -X POST <url> \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '<json-body>'
```

### Postman

1. Create a new request
2. Set method and URL
3. Add Authorization header: `Bearer <token>`
4. Set body to raw JSON
5. Click Send

**Pro Tip**: Use Postman environment variables for `BASE_URL` and `TOKEN`.

### HTTPie

```bash
http POST localhost:8081/api/1/trips \
  Authorization:"Bearer $TOKEN" \
  name="My Trip" \
  visibility="PUBLIC"
```

### JavaScript (fetch)

```javascript
const token = 'your-jwt-token';

async function createTrip() {
  const response = await fetch('http://localhost:8081/api/1/trips', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      name: 'My Trip',
      visibility: 'PUBLIC'
    })
  });
  
  const trip = await response.json();
  console.log('Created trip:', trip);
}
```

### Python (requests)

```python
import requests

token = 'your-jwt-token'
headers = {
    'Authorization': f'Bearer {token}',
    'Content-Type': 'application/json'
}

response = requests.post(
    'http://localhost:8081/api/1/trips',
    headers=headers,
    json={
        'name': 'My Trip',
        'visibility': 'PUBLIC'
    }
)

trip = response.json()
print('Created trip:', trip)
```

---

## Shell Script for Complete Workflow

Save this as `trip-workflow.sh`:

```bash
#!/bin/bash
set -e

BASE_AUTH="http://localhost:8083/api/1"
BASE_CMD="http://localhost:8081/api/1"
BASE_QRY="http://localhost:8082/api/1"

# 1. Register
echo "Registering user..."
RESPONSE=$(curl -s -X POST $BASE_AUTH/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"traveler_'$RANDOM'","password":"secret123"}')
TOKEN=$(echo $RESPONSE | jq -r '.token')
echo "Token: $TOKEN"

# 2. Create trip
echo -e "\nCreating trip..."
TRIP_RESPONSE=$(curl -s -X POST $BASE_CMD/trips \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"My Journey","visibility":"PUBLIC"}')
TRIP_ID=$(echo $TRIP_RESPONSE | jq -r '.id')
echo "Trip ID: $TRIP_ID"

# 3. Start trip
echo -e "\nStarting trip..."
curl -s -X PATCH $BASE_CMD/trips/$TRIP_ID/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"IN_PROGRESS"}' > /dev/null

# 4. Post update
echo -e "\nPosting location update..."
curl -s -X POST $BASE_CMD/trips/$TRIP_ID/updates \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"location":{"latitude":42.8805,"longitude":-8.5457},"message":"Hello!","battery":85}' \
  > /dev/null

# 5. View trips
echo -e "\nYour trips:"
curl -s -X GET $BASE_QRY/trips/me \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo -e "\n✅ Workflow completed!"
```

Run it:
```bash
chmod +x trip-workflow.sh
./trip-workflow.sh
```

---

## Common Patterns

### Store Token in Environment

```bash
# After login/register
export TOKEN="<your-token>"

# Use in requests
curl -H "Authorization: Bearer $TOKEN" ...
```

### Extract IDs with jq

```bash
# Create and get ID
TRIP_ID=$(curl ... | jq -r '.id')

# Use in next request
curl .../trips/$TRIP_ID/...
```

### Check Response Status

```bash
HTTP_STATUS=$(curl -s -o response.json -w "%{http_code}" ...)

if [ $HTTP_STATUS -eq 200 ]; then
  echo "Success!"
else
  echo "Error: $HTTP_STATUS"
  cat response.json
fi
```

---

## Troubleshooting

### 401 Unauthorized

**Problem**: Missing or expired token

**Solution**: 
1. Check token is included in Authorization header
2. Token might have expired - login again
3. Format must be: `Bearer <token>` (with space)

### 404 Not Found

**Problem**: Resource doesn't exist

**Solution**:
1. Verify the ID is correct
2. Check you're using the right endpoint
3. Ensure the resource wasn't deleted

### 403 Forbidden

**Problem**: Insufficient permissions

**Solution**:
1. Make sure you're the owner of the resource
2. Check if the resource visibility allows access
3. Verify your user has the required role

### Connection Refused

**Problem**: Service not running

**Solution**:
1. Start the service: `mvn spring-boot:run`
2. Check the service is running on the correct port
3. Verify no firewall is blocking the port

---

## Next Steps

- [Authentication API](Authentication-API) - Deep dive into auth
- [Trip API](Trip-API) - Full trip management
- [API Response Formats](API-Response-Formats) - Understand responses
- [Security & Authorization](Security-and-Authorization) - Security details

## Additional Resources

- **Swagger UI**: 
  - Auth: http://localhost:8083/swagger-ui.html
  - Command: http://localhost:8081/swagger-ui.html
  - Query: http://localhost:8082/swagger-ui.html
- **Source Code**: https://github.com/tomassirio/tracker-backend
- **Issues**: https://github.com/tomassirio/tracker-backend/issues
