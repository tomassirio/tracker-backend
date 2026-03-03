package com.tomassirio.wanderer.command.service.impl;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.LatLng;
import com.tomassirio.wanderer.command.service.GeocodingService;
import com.tomassirio.wanderer.commons.domain.GeoLocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Reverse-geocodes coordinates using the Google Geocoding API.
 *
 * <p>Extracts {@code city} (from {@code locality}, {@code administrative_area_level_2}, or {@code
 * administrative_area_level_1}) and {@code country} from the first result's address components.
 *
 * <p>Returns {@code null} on any API or parsing failure so that callers can gracefully degrade.
 */
@Slf4j
@RequiredArgsConstructor
public class GoogleGeocodingServiceImpl implements GeocodingService {

    private final GeoApiContext geoApiContext;

    @Override
    public GeocodingService.GeocodingResult reverseGeocode(GeoLocation location) {
        if (location == null || location.getLat() == null || location.getLon() == null) {
            return null;
        }

        try {
            com.google.maps.model.GeocodingResult[] results =
                    GeocodingApi.reverseGeocode(
                                    geoApiContext, new LatLng(location.getLat(), location.getLon()))
                            .await();

            if (results == null || results.length == 0) {
                log.debug(
                        "No geocoding results for ({}, {})", location.getLat(), location.getLon());
                return null;
            }

            return extractCityAndCountry(results[0].addressComponents);
        } catch (Exception e) {
            log.warn(
                    "Geocoding failed for ({}, {}): {}",
                    location.getLat(),
                    location.getLon(),
                    e.getMessage());
            return null;
        }
    }

    private GeocodingService.GeocodingResult extractCityAndCountry(AddressComponent[] components) {
        if (components == null) {
            return null;
        }

        String city = null;
        String country = null;

        for (AddressComponent component : components) {
            if (hasType(component, AddressComponentType.LOCALITY)) {
                city = component.longName;
            } else if (city == null
                    && hasType(component, AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_2)) {
                city = component.longName;
            } else if (city == null
                    && hasType(component, AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1)) {
                city = component.longName;
            }

            if (hasType(component, AddressComponentType.COUNTRY)) {
                country = component.longName;
            }
        }

        if (city != null || country != null) {
            log.debug("Geocoded to city={}, country={}", city, country);
            return new GeocodingService.GeocodingResult(city, country);
        }

        return null;
    }

    private boolean hasType(AddressComponent component, AddressComponentType type) {
        if (component.types == null) {
            return false;
        }
        for (AddressComponentType t : component.types) {
            if (t == type) {
                return true;
            }
        }
        return false;
    }
}
