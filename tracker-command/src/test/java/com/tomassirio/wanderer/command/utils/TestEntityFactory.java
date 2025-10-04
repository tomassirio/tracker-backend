package com.tomassirio.wanderer.command.utils;

import com.tomassirio.wanderer.command.dto.LocationRequest;
import com.tomassirio.wanderer.command.dto.TripCreationRequest;
import com.tomassirio.wanderer.command.dto.TripUpdateRequest;
import com.tomassirio.wanderer.commons.domain.Location;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import com.tomassirio.wanderer.commons.dto.LocationDTO;
import com.tomassirio.wanderer.commons.dto.TripDTO;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class TestEntityFactory {

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

    public static LocationDTO createLocationDTO(UUID locationId, double latitude, double longitude, Double altitude) {
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

    public static LocationRequest createLocationRequest() {
        return new LocationRequest(LATITUDE, LONGITUDE, ALTITUDE);
    }

    public static LocationRequest createLocationRequest(double latitude, double longitude, Double altitude) {
        return new LocationRequest(latitude, longitude, altitude);
    }

    public static TripDTO createTripDTO(UUID tripId, String name, LocationDTO startLocation, LocationDTO endLocation) {
        return new TripDTO(
                tripId,
                name,
                LocalDate.now().minusDays(1), // Start date in the past (valid for creation)
                LocalDate.now().plusDays(15), // End date in the future (valid)
                1250.5,
                startLocation,
                endLocation,
                TripVisibility.PUBLIC);
    }

    public static TripCreationRequest createTripCreationRequest(String name, LocationRequest startLocation, LocationRequest endLocation) {
        return new TripCreationRequest(
                name,
                LocalDate.now(),
                LocalDate.now().plusDays(15), // End date in the future (valid with @Future)
                1250.5,
                startLocation,
                endLocation,
                TripVisibility.PUBLIC);
    }

    public static TripUpdateRequest createTripUpdateRequest(String name, LocationRequest startLocation, LocationRequest endLocation) {
        return new TripUpdateRequest(
                name,
                LocalDate.now(),
                LocalDate.now().plusDays(5), // End date in the future
                2000.0,
                startLocation,
                endLocation,
                TripVisibility.PRIVATE);
    }
}
