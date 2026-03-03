package com.tomassirio.wanderer.command.service;

import com.tomassirio.wanderer.commons.domain.GeoLocation;

/**
 * Service for reverse-geocoding coordinates into human-readable place names (city, country).
 *
 * <p>Implementations may call external APIs (e.g. Google Geocoding API) or provide offline
 * fallbacks.
 */
public interface GeocodingService {

    /**
     * Reverse-geocodes the given location into a city/country result.
     *
     * @param location the geographic coordinates to geocode
     * @return a {@link GeocodingResult} with city and country, or {@code null} if geocoding fails
     */
    GeocodingResult reverseGeocode(GeoLocation location);

    /** Holds the city and country extracted from a reverse-geocoding response. */
    record GeocodingResult(String city, String country) {}
}
