# Achievement System

## Overview

The achievement system allows users to unlock achievements based on their trip activities and social interactions. Achievements are calculated event-driven and broadcasted via WebSocket to provide real-time notifications.

> **📚 Frontend Integration**: For API endpoint documentation and integration examples, see [ACHIEVEMENTS_API.md](ACHIEVEMENTS_API.md)

## Architecture

### CQRS Pattern
- **Command Side (tracker-command)**: Handles achievement unlocking via events
- **Query Side (tracker-query)**: Provides read access to achievements and user achievements

### Event-Driven Flow

**Trip Achievements**:
1. User posts a trip update (location, message, etc.)
2. `TripUpdateServiceImpl` publishes `TripUpdatedEvent`
3. `TripUpdatedEventHandler` persists the update and calls `AchievementCalculationService.checkAndUnlockAchievements(tripId)`
4. Service checks achievement criteria and publishes `AchievementUnlockedEvent` if thresholds are met
5. `AchievementUnlockedEventHandler` persists the user achievement
6. `BroadcastableEventListener` broadcasts the achievement via WebSocket to the user

**Social Achievements**:
1. User follows another user OR friend request is accepted
2. `UserFollowedEventHandler` or `FriendshipCreatedEventHandler` calls `AchievementCalculationService.checkAndUnlockSocialAchievements(userId)`
3. Service checks follower/friend counts and publishes `AchievementUnlockedEvent` if thresholds are met
4. Achievement is persisted and broadcasted

## Achievement Types

### Distance Achievements (Per Trip)
- **DISTANCE_100KM**: Walk 100km in a single trip
- **DISTANCE_200KM**: 4daagse - Walk 200km in a single trip
- **DISTANCE_500KM**: Walk 500km in a single trip
- **DISTANCE_800KM**: Camino Complete - Walk 800km in a single trip
- **DISTANCE_1000KM**: Walk 1000km in a single trip
- **DISTANCE_1600KM**: The Proclaimer - Walk 500 miles and 500 more (~1600km)
- **DISTANCE_2200KM**: The Hobbit - Walk from the Shire to Mordor (2200km)

**Note**: Currently uses Haversine formula for distance calculation. Will be replaced with Google Maps Distance Matrix API for shortest path calculation.

### Update Count Achievements (Per Trip)
- **UPDATES_10**: Post 10 updates on a single trip
- **UPDATES_50**: Post 50 updates on a single trip
- **UPDATES_100**: Post 100 updates on a single trip

### Duration Achievements (Per Trip)
- **DURATION_7_DAYS**: Trip lasting 7 days
- **DURATION_30_DAYS**: Trip lasting 30 days
- **DURATION_45_DAYS**: Six Week Explorer - Trip lasting 45 days
- **DURATION_60_DAYS**: Trip lasting 60 days

### Social Achievements - Followers (User-wide)
- **FOLLOWERS_10**: Popular Walker - Reach 10 followers
- **FOLLOWERS_50**: Influencer - Reach 50 followers
- **FOLLOWERS_100**: Community Leader - Reach 100 followers

### Social Achievements - Friends (User-wide)
- **FRIENDS_5**: Making Friends - Make 5 friends
- **FRIENDS_20**: Social Butterfly - Make 20 friends
- **FRIENDS_50**: Friend Collector - Make 50 friends

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
Returns all achievements unlocked by a specific user (both trip and social).

### Get Current User Achievements
```
GET /api/1/achievements/me/achievements
```
Returns all achievements unlocked by the authenticated user. Requires authentication.

### Get User Achievements for a Trip
```
GET /api/1/achievements/users/{userId}/trips/{tripId}/achievements
```
Returns all trip-specific achievements unlocked by a user for a specific trip.

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
  "tripId": "uuid or null for social achievements",
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
- `trip_id` (UUID, foreign key to trips, **nullable for social achievements**)
- `unlocked_at` (TIMESTAMP) - When achievement was unlocked
- `value_achieved` (DOUBLE) - Actual value (e.g., 105.5 km for distance achievement)

**Note**: `trip_id` is nullable to support social achievements (followers, friends) which are not tied to a specific trip.

## Future Enhancements

1. **Google Maps Integration**: Replace Haversine formula with Google Maps Distance Matrix API for accurate shortest path distance calculation
2. **Additional Achievement Types**: 
   - Weather-based achievements (walking in rain, snow, etc.)
   - Location-based achievements (specific landmarks, countries visited)
   - Time-based achievements (early bird, night owl)
3. **Achievement Badges**: Visual representations for unlocked achievements
4. **Leaderboards**: Compare achievements with friends
5. **Achievement Progress**: Show progress toward unlocking achievements (e.g., 75/100 km)
