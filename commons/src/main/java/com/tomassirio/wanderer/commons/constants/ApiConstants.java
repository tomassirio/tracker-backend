package com.tomassirio.wanderer.commons.constants;

/**
 * Constants for API endpoints and paths.
 *
 * @since 0.3.0
 */
public final class ApiConstants {

    private ApiConstants() {
        throw new UnsupportedOperationException(
                "This is a utility class and cannot be instantiated");
    }

    // UUID regex pattern
    public static final String UUID_REGEX = "[0-9a-fA-F\\-]{36}";
    public static final String UUID_PATH_VARIABLE = "/{id:" + UUID_REGEX + "}";

    // Base API version
    public static final String API_V1 = "/api/1";

    // Main resource paths
    public static final String AUTH_PATH = API_V1 + "/auth";
    public static final String USERS_PATH = API_V1 + "/users";
    public static final String TRIPS_PATH = API_V1 + "/trips";
    public static final String TRIP_PLANS_PATH = TRIPS_PATH + "/plans";

    // Auth endpoints
    public static final String LOGIN_ENDPOINT = "/login";
    public static final String REGISTER_ENDPOINT = "/register";

    // User endpoints
    public static final String ME_ENDPOINT = "/me";
    public static final String USERNAME_ENDPOINT = "/username/{username}";
    public static final String USER_BY_ID_ENDPOINT = UUID_PATH_VARIABLE;

    // Trip endpoints
    public static final String TRIP_BY_ID_ENDPOINT = UUID_PATH_VARIABLE;
    public static final String TRIP_VISIBILITY_ENDPOINT = "/{id}/visibility";
    public static final String TRIP_STATUS_ENDPOINT = "/{id}/status";
    public static final String TRIP_UPDATES_ENDPOINT = "/{tripId}/updates";
    public static final String TRIPS_BY_USER_ENDPOINT = "/users/{userId}";
    public static final String TRIPS_PUBLIC_ENDPOINT = "/public";

    // Comment endpoints base paths
    public static final String TRIP_COMMENTS_ENDPOINT = "/{tripId}/comments";
    public static final String COMMENT_REACTIONS_ENDPOINT = "/comments/{commentId}/reactions";

    // Friend Request endpoints
    public static final String FRIEND_REQUESTS_PATH = USERS_PATH + "/friend-requests";
    public static final String FRIEND_REQUESTS_RECEIVED_ENDPOINT = "/received";
    public static final String FRIEND_REQUESTS_SENT_ENDPOINT = "/sent";

    // Trip Plan endpoints
    public static final String TRIP_PLAN_BY_ID_ENDPOINT = "/{planId}";
}
