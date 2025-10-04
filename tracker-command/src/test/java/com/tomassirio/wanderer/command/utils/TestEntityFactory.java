package com.tomassirio.wanderer.command.utils;

import com.tomassirio.wanderer.commons.domain.Location;
import com.tomassirio.wanderer.commons.domain.Trip;

import java.time.Instant;
import java.util.UUID;

public class TestEntityFactory {

    public static final double LATITUDE = 33.95036882906084;
    public static final double LONGITUDE = -105.33119262037046;

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
        return Trip.builder()
                .id(tripId)
                .name("Test Trip")
                .build();
    }
}
