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
    public static final UUID USER_ID = UUID.randomUUID();

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
        return createTrip(tripId, "Test Trip");
    }

    public static Trip createTrip(UUID tripId, String name) {
        return createTrip(tripId, name, TripVisibility.PUBLIC);
    }

    public static Trip createTrip(UUID tripId, String name, TripVisibility visibility) {
        return createTrip(
                tripId,
                name,
                LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(5),
                1500.0,
                visibility);
    }

    public static Trip createTrip(
            UUID tripId,
            String name,
            LocalDate startDate,
            LocalDate endDate,
            Double totalDistance,
            TripVisibility visibility) {
        return Trip.builder()
                .id(tripId)
                .name(name)
                .startDate(startDate)
                .endDate(endDate)
                .totalDistance(totalDistance)
                .visibility(visibility)
                .build();
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
            UUID tripId,
            UUID userId,
            String name,
            LocationDTO startLocation,
            LocationDTO endLocation) {
        return new TripDTO(
                tripId,
                name,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(15),
                1250.5,
                startLocation,
                endLocation,
                TripVisibility.PUBLIC,
                userId);
    }
}
