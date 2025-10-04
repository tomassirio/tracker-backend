package com.tomassirio.wanderer.command.utils;

import com.tomassirio.wanderer.command.dto.LocationRequest;
import com.tomassirio.wanderer.command.dto.TripCreationRequest;
import com.tomassirio.wanderer.command.dto.TripUpdateRequest;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import com.tomassirio.wanderer.commons.utils.BaseTestEntityFactory;
import java.time.LocalDate;

/**
 * Test entity factory for tracker-command module. Extends the commons TestEntityFactory with
 * command-specific DTOs.
 */
public class TestEntityFactory extends BaseTestEntityFactory {

    // LocationRequest factory methods
    public static LocationRequest createLocationRequest() {
        return new LocationRequest(LATITUDE, LONGITUDE, ALTITUDE);
    }

    public static LocationRequest createLocationRequest(
            double latitude, double longitude, Double altitude) {
        return new LocationRequest(latitude, longitude, altitude);
    }

    // TripCreationRequest factory methods
    public static TripCreationRequest createTripCreationRequest(
            String name, LocationRequest startLocation, LocationRequest endLocation) {
        return createTripCreationRequest(
                name, startLocation, endLocation, 1250.5, TripVisibility.PUBLIC);
    }

    public static TripCreationRequest createTripCreationRequest(
            String name,
            LocationRequest startLocation,
            LocationRequest endLocation,
            Double totalDistance) {
        return createTripCreationRequest(
                name, startLocation, endLocation, totalDistance, TripVisibility.PUBLIC);
    }

    public static TripCreationRequest createTripCreationRequest(
            String name,
            LocationRequest startLocation,
            LocationRequest endLocation,
            Double totalDistance,
            TripVisibility tripVisibility) {
        return new TripCreationRequest(
                name,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(15),
                totalDistance,
                startLocation,
                endLocation,
                tripVisibility);
    }

    // TripUpdateRequest factory methods
    public static TripUpdateRequest createTripUpdateRequest(
            String name, LocationRequest startLocation, LocationRequest endLocation) {
        return new TripUpdateRequest(
                name,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(15),
                1250.5,
                startLocation,
                endLocation,
                TripVisibility.PUBLIC);
    }

    public static TripUpdateRequest createTripUpdateRequest(
            String name,
            LocationRequest startLocation,
            LocationRequest endLocation,
            Double totalDistance) {
        return createTripUpdateRequest(
                name, startLocation, endLocation, totalDistance, TripVisibility.PUBLIC);
    }

    public static TripUpdateRequest createTripUpdateRequest(
            String name,
            LocationRequest startLocation,
            LocationRequest endLocation,
            Double totalDistance,
            TripVisibility tripVisibility) {
        return new TripUpdateRequest(
                name,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(15),
                totalDistance,
                startLocation,
                endLocation,
                tripVisibility);
    }
}
