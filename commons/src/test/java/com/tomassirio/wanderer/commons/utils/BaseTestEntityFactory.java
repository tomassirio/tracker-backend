package com.tomassirio.wanderer.commons.utils;

import com.tomassirio.wanderer.commons.domain.Comment;
import com.tomassirio.wanderer.commons.domain.GeoLocation;
import com.tomassirio.wanderer.commons.domain.Reactions;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripDetails;
import com.tomassirio.wanderer.commons.domain.TripPlan;
import com.tomassirio.wanderer.commons.domain.TripPlanType;
import com.tomassirio.wanderer.commons.domain.TripSettings;
import com.tomassirio.wanderer.commons.domain.TripStatus;
import com.tomassirio.wanderer.commons.domain.TripUpdate;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import com.tomassirio.wanderer.commons.domain.User;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
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
                        .waypoints(new ArrayList<>())
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
                        .waypoints(new ArrayList<>())
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

    public static Comment createComment(UUID commentId, User user, Trip trip) {
        return Comment.builder()
                .id(commentId)
                .user(user)
                .trip(trip)
                .message("Test comment")
                .reactions(new Reactions())
                .replies(List.of())
                .timestamp(Instant.now())
                .build();
    }

    public static TripPlan createTripPlan(
            UUID tripPlanId, UUID userId, String name, LocalDate startDate, LocalDate endDate) {
        return TripPlan.builder()
                .id(tripPlanId)
                .userId(userId)
                .name(name)
                .planType(TripPlanType.SIMPLE)
                .startDate(startDate)
                .endDate(endDate)
                .startLocation(GeoLocation.builder().lat(40.7128).lon(-74.0060).build())
                .endLocation(GeoLocation.builder().lat(34.0522).lon(-118.2437).build())
                .waypoints(new ArrayList<>())
                .metadata(new HashMap<>())
                .createdTimestamp(Instant.now())
                .build();
    }
}
