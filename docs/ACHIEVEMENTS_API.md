# Achievements API - Frontend Integration Guide

## Overview

This guide provides comprehensive documentation for integrating the Achievements API into your frontend application. The API has been refactored to provide a cleaner, more intuitive endpoint structure.

## Endpoint Structure

### Base URL
All achievement endpoints are under: `https://api.tracker.com/api/1/`

---

## 1. Get All Available Achievements

**Endpoint:** `GET /achievements`  
**Authentication:** Not required  
**Description:** Retrieves all achievement types that can be unlocked in the system.

### Response

```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "type": "DISTANCE_100KM",
    "name": "First Century",
    "description": "Walk 100km in a single trip",
    "thresholdValue": 100
  },
  {
    "id": "123e4567-e89b-12d3-a456-426614174001",
    "type": "UPDATES_10",
    "name": "Getting Started",
    "description": "Post 10 updates on a single trip",
    "thresholdValue": 10
  }
]
```

### JavaScript Example

```javascript
async function getAllAchievements() {
  const response = await fetch('https://api.tracker.com/api/1/achievements');
  const achievements = await response.json();
  return achievements;
}
```

---

## 2. Get Current User's Achievements

**Endpoint:** `GET /users/me/achievements`  
**Authentication:** Required (Bearer Token)  
**Description:** Retrieves all achievements unlocked by the currently authenticated user.

### Headers

```
Authorization: Bearer <your-jwt-token>
```

### Response

```json
[
  {
    "id": "223e4567-e89b-12d3-a456-426614174000",
    "userId": "323e4567-e89b-12d3-a456-426614174000",
    "achievement": {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "type": "DISTANCE_100KM",
      "name": "First Century",
      "description": "Walk 100km in a single trip",
      "thresholdValue": 100
    },
    "tripId": "423e4567-e89b-12d3-a456-426614174000",
    "unlockedAt": "2026-02-24T10:30:00Z",
    "valueAchieved": 105.5
  }
]
```

### JavaScript Example

```javascript
async function getMyAchievements(token) {
  const response = await fetch('https://api.tracker.com/api/1/users/me/achievements', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  const achievements = await response.json();
  return achievements;
}
```

---

## 3. Get Achievements for a Specific User

**Endpoint:** `GET /users/{userId}/achievements`  
**Authentication:** Not required  
**Description:** Retrieves all achievements unlocked by a specific user.

### Path Parameters

- `userId` (UUID): The unique identifier of the user

### Response

Same structure as "Get Current User's Achievements"

### JavaScript Example

```javascript
async function getUserAchievements(userId) {
  const response = await fetch(`https://api.tracker.com/api/1/users/${userId}/achievements`);
  const achievements = await response.json();
  return achievements;
}
```

---

## 4. Get Achievements for a Specific Trip

**Endpoint:** `GET /trips/{tripId}/achievements`  
**Authentication:** Not required  
**Description:** Retrieves all achievements unlocked for a specific trip (by any user).

### Path Parameters

- `tripId` (UUID): The unique identifier of the trip

### Response

Same structure as "Get Current User's Achievements"

### JavaScript Example

```javascript
async function getTripAchievements(tripId) {
  const response = await fetch(`https://api.tracker.com/api/1/trips/${tripId}/achievements`);
  const achievements = await response.json();
  return achievements;
}
```

---

## Achievement Types

The system supports 23 achievement types across different categories:

### Distance Achievements
- `DISTANCE_100KM` - First Century (100km)
- `DISTANCE_200KM` - Double Century (200km)
- `DISTANCE_500KM` - Half a Thousand (500km)
- `DISTANCE_800KM` - Santiago de Compostela (800km)
- `DISTANCE_1000KM` - The Kilometer King (1000km)
- `DISTANCE_1600KM` - The Proclaimer (1600km)
- `DISTANCE_2200KM` - The Hobbit (2200km)

### Update Achievements
- `UPDATES_10` - Getting Started (10 updates)
- `UPDATES_50` - Dedicated Chronicler (50 updates)
- `UPDATES_100` - Master Storyteller (100 updates)

### Duration Achievements
- `DURATION_7_DAYS` - Week Warrior (7 days)
- `DURATION_30_DAYS` - Month Master (30 days)
- `DURATION_45_DAYS` - Long Hauler (45 days)
- `DURATION_60_DAYS` - Two Months Strong (60 days)

### Social Achievements (User-wide)
- `FOLLOWERS_10` - Popular (10 followers)
- `FOLLOWERS_50` - Influencer (50 followers)
- `FOLLOWERS_100` - Celebrity (100 followers)
- `FRIENDS_5` - Social Butterfly (5 friends)
- `FRIENDS_20` - Friend Collector (20 friends)
- `FRIENDS_50` - Community Leader (50 friends)

---

## Data Models

### Achievement DTO

```typescript
interface AchievementDTO {
  id: string;              // UUID
  type: string;            // Achievement type enum
  name: string;            // Display name
  description: string;     // Achievement description
  thresholdValue: number;  // Required value to unlock
}
```

### User Achievement DTO

```typescript
interface UserAchievementDTO {
  id: string;                // UUID
  userId: string;            // UUID of the user
  achievement: AchievementDTO; // Achievement details
  tripId: string | null;     // UUID of the trip (null for user-wide achievements)
  unlockedAt: string;        // ISO 8601 timestamp
  valueAchieved: number;     // Actual value achieved (e.g., 105.5km for a 100km achievement)
}
```

---

## Error Handling

### HTTP Status Codes

- `200 OK` - Request successful
- `401 Unauthorized` - Missing or invalid authentication token
- `404 Not Found` - Resource not found (user, trip, or achievement)
- `500 Internal Server Error` - Server error

### Error Response Format

```json
{
  "timestamp": "2026-02-24T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: 123e4567-e89b-12d3-a456-426614174000",
  "path": "/api/1/users/123e4567-e89b-12d3-a456-426614174000/achievements"
}
```

---

## React Integration Example

```jsx
import React, { useState, useEffect } from 'react';

function AchievementsPage({ tripId }) {
  const [achievements, setAchievements] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchAchievements() {
      try {
        const response = await fetch(
          `https://api.tracker.com/api/1/trips/${tripId}/achievements`
        );
        const data = await response.json();
        setAchievements(data);
      } catch (error) {
        console.error('Failed to fetch achievements:', error);
      } finally {
        setLoading(false);
      }
    }

    fetchAchievements();
  }, [tripId]);

  if (loading) return <div>Loading achievements...</div>;

  return (
    <div>
      <h2>Trip Achievements</h2>
      {achievements.map(achievement => (
        <div key={achievement.id} className="achievement-card">
          <h3>{achievement.achievement.name}</h3>
          <p>{achievement.achievement.description}</p>
          <p>Unlocked: {new Date(achievement.unlockedAt).toLocaleDateString()}</p>
          <p>Value Achieved: {achievement.valueAchieved}</p>
        </div>
      ))}
    </div>
  );
}
```

---

---

## Best Practices

1. **Cache Achievement Types**: The list of all available achievements rarely changes. Cache this data locally to reduce API calls.

2. **Handle Loading States**: Always show loading indicators while fetching achievements.

3. **Error Handling**: Implement proper error handling for all API calls.

4. **Authentication**: For authenticated endpoints, handle token expiration and refresh gracefully.

5. **Pagination**: Currently, all achievements are returned in a single response. If you have users with many achievements, consider implementing client-side pagination for better UX.

---

## Support

For issues, questions, or feature requests, please open a GitHub issue in the [tracker-backend repository](https://github.com/tomassirio/tracker-backend/issues).

---

**¡Buen Camino!** 🥾⛪
