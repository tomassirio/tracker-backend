package com.tomassirio.wanderer.commons.utils;

import com.tomassirio.wanderer.commons.domain.*;
import com.tomassirio.wanderer.commons.dto.TripDTO;
import com.tomassirio.wanderer.commons.dto.TripDetailsDTO;
import com.tomassirio.wanderer.commons.dto.TripSettingsDTO;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class BaseTestEntityFactory {

    public static final double LATITUDE = 33.95036882906084;
    public static final double LONGITUDE = -105.33119262037046;
    public static final UUID USER_ID = UUID.randomUUID();
    public static final String USERNAME = "testuser";

    public static User createUser() {
        return createUser(USER_ID, USERNAME);
    }

    public static User createUser(UUID userId, String username) {
        return User.builder().id(userId).username(username).build();
    }

    public static GeoLocation createGeoLocation() {
        return GeoLocation.builder().lat(LATITUDE).lon(LONGITUDE).build();
    }

    public static TripUpdate createTripUpdate(UUID tripUpdateId, Trip trip) {
        return TripUpdate.builder()
                .id(tripUpdateId)
                .trip(trip)
                .location(createGeoLocation())
                .battery(85)
                .message("Test update")
                .reactions(new Reactions())
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
        TripSettings tripSettings =
                TripSettings.builder()
                        .tripStatus(TripStatus.CREATED)
                        .visibility(visibility)
                        .updateRefresh(null)
                        .build();

        TripDetails tripDetails =
                TripDetails.builder()
                        .startTimestamp(Instant.now())
                        .endTimestamp(null)
                        .startLocation(null)
                        .endLocation(null)
                        .build();

        return Trip.builder()
                .id(tripId)
                .name(name)
                .userId(USER_ID)
                .tripSettings(tripSettings)
                .tripDetails(tripDetails)
                .tripPlanId(null)
                .creationTimestamp(Instant.now())
                .enabled(true)
                .build();
    }

    public static Trip createTripWithUser(UUID tripId, UUID userId, String name) {
        return createTripWithUser(tripId, userId, name, TripVisibility.PUBLIC);
    }

    public static Trip createTripWithUser(
            UUID tripId, UUID userId, String name, TripVisibility visibility) {
        TripSettings tripSettings =
                TripSettings.builder()
                        .tripStatus(TripStatus.CREATED)
                        .visibility(visibility)
                        .updateRefresh(null)
                        .build();

        TripDetails tripDetails =
                TripDetails.builder()
                        .startTimestamp(Instant.now())
                        .endTimestamp(null)
                        .startLocation(null)
                        .endLocation(null)
                        .build();

        return Trip.builder()
                .id(tripId)
                .name(name)
                .userId(userId)
                .tripSettings(tripSettings)
                .tripDetails(tripDetails)
                .tripPlanId(null)
                .creationTimestamp(Instant.now())
                .enabled(true)
                .build();
    }

    public static TripDTO createTripDTO(UUID tripId, UUID userId, String username, String name) {
        TripSettingsDTO tripSettings =
                new TripSettingsDTO(TripStatus.CREATED, TripVisibility.PUBLIC, null);

        TripDetailsDTO tripDetails = new TripDetailsDTO(Instant.now(), null, null, null);

        return new TripDTO(
                tripId.toString(),
                name,
                userId.toString(),
                username,
                tripSettings,
                tripDetails,
                null,
                List.of(),
                List.of(),
                Instant.now(),
                true);
    }

    public static Comment createComment(UUID commentId, UUID userId, Trip trip) {
        return Comment.builder()
                .id(commentId)
                .userId(userId)
                .trip(trip)
                .message("Test comment")
                .reactions(new Reactions())
                .replies(List.of())
                .timestamp(Instant.now())
                .build();
    }
}
