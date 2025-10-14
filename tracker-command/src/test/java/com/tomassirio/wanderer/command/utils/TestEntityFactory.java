package com.tomassirio.wanderer.command.utils;

import com.tomassirio.wanderer.command.dto.TripCreationRequest;
import com.tomassirio.wanderer.command.dto.TripUpdateRequest;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import com.tomassirio.wanderer.commons.utils.BaseTestEntityFactory;

/**
 * Test entity factory for tracker-command module. Extends the commons TestEntityFactory with
 * command-specific DTOs.
 */
public class TestEntityFactory extends BaseTestEntityFactory {

    // TripCreationRequest factory methods
    public static TripCreationRequest createTripCreationRequest(String name) {
        return createTripCreationRequest(name, TripVisibility.PUBLIC);
    }

    public static TripCreationRequest createTripCreationRequest(
            String name, TripVisibility tripVisibility) {
        return new TripCreationRequest(name, tripVisibility);
    }

    // TripUpdateRequest factory methods
    public static TripUpdateRequest createTripUpdateRequest(String name) {
        return createTripUpdateRequest(name, TripVisibility.PUBLIC);
    }

    public static TripUpdateRequest createTripUpdateRequest(
            String name, TripVisibility tripVisibility) {
        return new TripUpdateRequest(name, tripVisibility);
    }
}
