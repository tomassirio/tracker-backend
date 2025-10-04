package com.tomassirio.wanderer.commons.utils;

import com.tomassirio.wanderer.commons.domain.Location;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import com.tomassirio.wanderer.commons.dto.LocationDTO;
import com.tomassirio.wanderer.commons.dto.TripDTO;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class BaseTestEntityFactory {

    public static final double LATITUDE = 33.95036882906084;
    public static final double LONGITUDE = -105.33119262037046;
    public static final double ALTITUDE = 1500.0;

    public static Location createLocation(UUID locationId, Trip trip) {
        return Location.builder()
                .id(locationId)
                .trip(trip)
                .latitude(LATITUDE)
                .longitude(LONGITUDE)
                .timestamp(Instant.now())
                .build();
    }

    public static Trip createTrip(UUID tripId) {
        return Trip.builder().id(tripId).name("Test Trip").build();
    }

    public static LocationDTO createLocationDTO(UUID locationId) {
        return new LocationDTO(
                locationId,
                LATITUDE,
                LONGITUDE,
                Instant.now(),
                ALTITUDE,
                10.0,
                85,
                "TRIP_ENDPOINT");
    }

    public static LocationDTO createLocationDTO(
            UUID locationId, double latitude, double longitude, Double altitude) {
        return new LocationDTO(
                locationId,
                latitude,
                longitude,
                Instant.now(),
                altitude,
                10.0,
                85,
                "TRIP_ENDPOINT");
    }

    public static TripDTO createTripDTO(
            UUID tripId, String name, LocationDTO startLocation, LocationDTO endLocation) {
        return new TripDTO(
                tripId,
                name,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(15),
                1250.5,
                startLocation,
                endLocation,
                TripVisibility.PUBLIC);
    }
}
