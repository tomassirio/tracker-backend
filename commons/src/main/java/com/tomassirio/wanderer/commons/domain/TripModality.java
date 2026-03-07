package com.tomassirio.wanderer.commons.domain;

/**
 * Represents the modality of a trip, indicating whether it is a single-day or multi-day journey.
 *
 * <ul>
 *   <li>{@link #SIMPLE} – A single-day trip with no overnight stays.
 *   <li>{@link #MULTI_DAY} – A trip spanning multiple days.
 * </ul>
 */
public enum TripModality {
    SIMPLE,
    MULTI_DAY
}
