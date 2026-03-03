# Weather Data on Trip Updates — Frontend Integration Guide

**Date:** March 3, 2026
**Backend Version:** 0.8.4-SNAPSHOT

---

## Summary

Every trip update now includes **weather data** (temperature and condition) captured at the time and location of the update. This data is available through both the REST API and WebSocket events.

---

## New Fields

Two new fields have been added to trip update responses:

| Field                | Type               | Nullable | Description                                         |
|----------------------|--------------------|----------|-----------------------------------------------------|
| `temperatureCelsius` | `number` (double)  | Yes      | Temperature in Celsius at the update location/time  |
| `weatherCondition`   | `string` (enum)    | Yes      | Weather condition enum value (see below)             |

Both fields are `null` when the weather API is unavailable or the lookup fails.

---

## `WeatherCondition` Enum Values

The `weatherCondition` field is a string enum with the following possible values:

| Value            | Description             |
|------------------|-------------------------|
| `CLEAR`          | Clear sky               |
| `MOSTLY_CLEAR`   | Mostly clear            |
| `PARTLY_CLOUDY`  | Partly cloudy           |
| `CLOUDY`         | Overcast / cloudy       |
| `FOG`            | Fog                     |
| `HAZE`           | Haze                    |
| `DRIZZLE`        | Light drizzle           |
| `LIGHT_RAIN`     | Light rain              |
| `RAIN`           | Rain                    |
| `HEAVY_RAIN`     | Heavy rain              |
| `LIGHT_SNOW`     | Light snow              |
| `SNOW`           | Snow                    |
| `HEAVY_SNOW`     | Heavy snow              |
| `SLEET`          | Sleet                   |
| `HAIL`           | Hail                    |
| `THUNDERSTORM`   | Thunderstorm            |
| `WINDY`          | Windy                   |
| `UNKNOWN`        | Unrecognized condition  |

> **Note:** The enum may grow over time if Google adds new condition types. The frontend should handle unknown values gracefully (e.g., display a generic weather icon or fall back to `UNKNOWN`).

---

## Affected Endpoints

### 1. REST API — Query Service (Port 8082)

#### `GET /api/1/trips/updates/{id}`

Returns a single trip update. Response now includes weather fields.

#### `GET /api/1/trips/{tripId}/updates`

Returns all trip updates for a trip (ordered by timestamp descending). Each item now includes weather fields.

**Example response body (single trip update):**

```json
{
  "id": "a1b2c3d4-...",
  "tripId": "e5f6g7h8-...",
  "location": {
    "lat": 42.8805,
    "lon": -8.5457
  },
  "battery": 85,
  "message": "Arrived at Santiago!",
  "reactions": {
    "heart": 0,
    "smiley": 0,
    "sad": 0,
    "laugh": 0,
    "anger": 0
  },
  "city": "Santiago de Compostela",
  "country": "Spain",
  "temperatureCelsius": 18.5,
  "weatherCondition": "PARTLY_CLOUDY",
  "timestamp": "2026-03-03T12:34:56.789Z"
}
```

**When weather is unavailable:**

```json
{
  "id": "a1b2c3d4-...",
  "tripId": "e5f6g7h8-...",
  "location": { "lat": 0.0, "lon": 0.0 },
  "battery": 50,
  "message": "Middle of the ocean",
  "reactions": { ... },
  "city": null,
  "country": null,
  "temperatureCelsius": null,
  "weatherCondition": null,
  "timestamp": "2026-03-03T12:34:56.789Z"
}
```

### 2. REST API — Trip Detail (nested)

When fetching a full trip via `GET /api/1/trips/{tripId}`, the `tripUpdates` array also contains the new fields on each item.

### 3. WebSocket — Real-time Updates

**Topic:** `/topic/trips/{tripId}`
**Event type:** `TRIP_UPDATED`

The WebSocket payload now includes weather fields. Fields with `null` values are **omitted** from the JSON payload (the payload uses `@JsonInclude(NON_NULL)`).

**Example WebSocket payload:**

```json
{
  "tripId": "e5f6g7h8-...",
  "latitude": 42.8805,
  "longitude": -8.5457,
  "batteryLevel": 85,
  "message": "Arrived at Santiago!",
  "city": "Santiago de Compostela",
  "country": "Spain",
  "temperatureCelsius": 18.5,
  "weatherCondition": "PARTLY_CLOUDY"
}
```

**When weather is unavailable**, `temperatureCelsius` and `weatherCondition` are simply absent from the payload.

---

## Frontend Implementation Checklist

### Data Model
- [ ] Add `temperatureCelsius: number | null` to your trip update type/interface
- [ ] Add `weatherCondition: WeatherCondition | null` to your trip update type/interface
- [ ] Define the `WeatherCondition` enum/union type with all 18 values listed above

### Display
- [ ] Show temperature (e.g., `18.5°C`) on trip update cards/markers
- [ ] Map each `WeatherCondition` value to an icon (sun, cloud, rain, snow, etc.)
- [ ] Handle `null` gracefully — hide the weather section or show a "Weather unavailable" placeholder
- [ ] Handle `UNKNOWN` — display a generic/fallback weather icon
- [ ] Consider formatting temperature for user locale (Celsius vs Fahrenheit toggle if desired)

### WebSocket
- [ ] Update the `TRIP_UPDATED` event handler to read the two new optional fields
- [ ] Remember these fields may be absent from the payload (not just `null`)

### Existing Data
- [ ] Trip updates created **before this release** will have `null` for both weather fields — the frontend must handle this for historical data

---

## Database Migration

Liquibase changeset `024-add-weather-to-trip-updates` adds:
- `temperature_celsius` (`DOUBLE PRECISION`, nullable)
- `weather_condition` (`VARCHAR(255)`, nullable, stores the enum name as a string)

No frontend action needed, but good to know for debugging.

---

## Icon Mapping Suggestion

| WeatherCondition  | Suggested Icon |
|-------------------|----------------|
| `CLEAR`           | ☀️ Sun          |
| `MOSTLY_CLEAR`    | 🌤️ Sun + small cloud |
| `PARTLY_CLOUDY`   | ⛅ Sun + cloud  |
| `CLOUDY`          | ☁️ Cloud        |
| `FOG`             | 🌫️ Fog          |
| `HAZE`            | 🌫️ Haze         |
| `DRIZZLE`         | 🌦️ Light rain   |
| `LIGHT_RAIN`      | 🌧️ Rain         |
| `RAIN`            | 🌧️ Rain         |
| `HEAVY_RAIN`      | 🌧️ Heavy rain   |
| `LIGHT_SNOW`      | 🌨️ Snow         |
| `SNOW`            | ❄️ Snow         |
| `HEAVY_SNOW`      | ❄️ Heavy snow   |
| `SLEET`           | 🌨️ Sleet        |
| `HAIL`            | 🌨️ Hail         |
| `THUNDERSTORM`    | ⛈️ Thunderstorm |
| `WINDY`           | 💨 Wind         |
| `UNKNOWN`         | ❓ Unknown      |

