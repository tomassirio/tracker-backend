package com.tomassirio.wanderer.commons.domain;

/**
 * Represents the type of a trip update entry.
 *
 * <ul>
 *   <li>{@link #REGULAR} – A normal location update (default).
 *   <li>{@link #DAY_START} – Marks the beginning of a new day on a multi-day trip.
 *   <li>{@link #DAY_END} – Marks the end of a day on a multi-day trip.
 *   <li>{@link #TRIP_STARTED} – Marks the start of the entire trip.
 *   <li>{@link #TRIP_ENDED} – Marks the end of the entire trip.
 * </ul>
 */
public enum UpdateType {
    REGULAR,
    DAY_START,
    DAY_END,
    TRIP_STARTED,
    TRIP_ENDED
}
