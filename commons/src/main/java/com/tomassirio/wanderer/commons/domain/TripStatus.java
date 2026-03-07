package com.tomassirio.wanderer.commons.domain;

/**
 * Represents the lifecycle status of a trip.
 *
 * <ul>
 *   <li>{@link #CREATED} – Trip has been created but not yet started.
 *   <li>{@link #IN_PROGRESS} – Trip is actively underway.
 *   <li>{@link #PAUSED} – Trip has been temporarily paused mid-journey.
 *   <li>{@link #RESTING} – Pilgrim has completed the day's stage and is resting overnight before
 *       continuing the next day. Specific to multi-day trips.
 *   <li>{@link #FINISHED} – Trip has been completed.
 * </ul>
 */
public enum TripStatus {
    CREATED,
    IN_PROGRESS,
    PAUSED,
    RESTING,
    FINISHED
}
