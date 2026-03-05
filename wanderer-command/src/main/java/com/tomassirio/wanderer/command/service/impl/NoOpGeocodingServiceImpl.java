package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.command.service.GeocodingService;
import com.tomassirio.wanderer.commons.domain.GeoLocation;

/**
 * No-op geocoding service that always returns {@code null}.
 *
 * <p>Used when the Google Maps API key is not configured or geocoding is disabled.
 */
public class NoOpGeocodingServiceImpl implements GeocodingService {

    @Override
    public GeocodingResult reverseGeocode(GeoLocation location) {
        return null;
    }
}
