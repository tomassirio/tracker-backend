# Achievement System

## Overview

The achievement system allows users to unlock achievements based on their trip activities. Achievements are calculated event-driven and broadcasted via WebSocket to provide real-time notifications.

## Architecture

### CQRS Pattern
- **Command Side (tracker-command)**: Handles achievement unlocking via events
- **Query Side (tracker-query)**: Provides read access to achievements and user achievements

### Event-Driven Flow
1. User posts a trip update (location, message, etc.)
2. `TripUpdateServiceImpl` publishes `TripUpdatedEvent`
3. `TripUpdatedEventHandler` persists the update and calls `AchievementCalculationService`
4. `AchievementCalculationService` checks achievement criteria and publishes `AchievementUnlockedEvent` if thresholds are met
5. `AchievementUnlockedEventHandler` persists the user achievement
6. `BroadcastableEventListener` broadcasts the achievement via WebSocket to the user

## Achievement Types

### Distance Achievements
- **DISTANCE_100KM**: Walk 100km in a single trip
- **DISTANCE_500KM**: Walk 500km in a single trip
- **DISTANCE_800KM**: Walk 800km in a single trip (Camino Complete)
- **DISTANCE_1000KM**: Walk 1000km in a single trip

**Note**: Currently uses Haversine formula for distance calculation. Will be replaced with Google Maps Distance Matrix API for shortest path calculation.

### Update Count Achievements
- **UPDATES_10**: Post 10 updates on a single trip
- **UPDATES_50**: Post 50 updates on a single trip
- **UPDATES_100**: Post 100 updates on a single trip

### Duration Achievements
- **DURATION_7_DAYS**: Trip lasting 7 days
- **DURATION_30_DAYS**: Trip lasting 30 days
- **DURATION_60_DAYS**: Trip lasting 60 days

## API Endpoints

### Get All Available Achievements
```
GET /api/1/achievements
```
Returns all achievements that can be unlocked in the system.

### Get User Achievements
```
GET /api/1/achievements/users/{userId}/achievements
```
Returns all achievements unlocked by a specific user.

### Get Current User Achievements
```
GET /api/1/achievements/me/achievements
```
Returns all achievements unlocked by the authenticated user. Requires authentication.

### Get User Achievements for a Trip
```
GET /api/1/achievements/users/{userId}/trips/{tripId}/achievements
```
Returns all achievements unlocked by a user for a specific trip.

## WebSocket Events

Achievements are broadcasted to users via WebSocket:

**Topic**: `/topic/users/{userId}`

**Event Type**: `ACHIEVEMENT_UNLOCKED`

**Payload**:
```json
{
  "achievementId": "uuid",
  "achievementType": "DISTANCE_100KM",
  "achievementName": "First Century",
  "tripId": "uuid",
  "valueAchieved": 105.5,
  "unlockedAt": "2024-01-15T10:30:00Z"
}
```

## Database Schema

### achievements Table
- `id` (UUID, primary key)
- `type` (VARCHAR, unique) - AchievementType enum
- `name` (VARCHAR) - Display name
- `description` (TEXT) - Achievement description
- `threshold_value` (INTEGER) - Threshold to unlock
- `enabled` (BOOLEAN) - Whether achievement is active

### user_achievements Table
- `id` (UUID, primary key)
- `user_id` (UUID, foreign key to users)
- `achievement_id` (UUID, foreign key to achievements)
- `trip_id` (UUID, foreign key to trips)
- `unlocked_at` (TIMESTAMP) - When achievement was unlocked
- `value_achieved` (DOUBLE) - Actual value (e.g., 105.5 km for distance achievement)

## Future Enhancements

1. **Google Maps Integration**: Replace Haversine formula with Google Maps Distance Matrix API for accurate shortest path distance calculation
2. **Additional Achievement Types**: 
   - Social achievements (comments, reactions, followers)
   - Location-based achievements (specific landmarks)
   - Weather-based achievements (walking in rain, snow, etc.)
3. **Achievement Badges**: Visual representations for unlocked achievements
4. **Leaderboards**: Compare achievements with friends
5. **Achievement Progress**: Show progress toward unlocking achievements (e.g., 75/100 km)
