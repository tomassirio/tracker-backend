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

    // ============================================================
    // Common patterns and building blocks
    // ============================================================

    public static final String UUID_REGEX = "[0-9a-fA-F\\-]{36}";
    public static final String UUID_PATH_VARIABLE = "/{id:" + UUID_REGEX + "}";

    // Common suffixes
    public static final String ME_SUFFIX = "/me";
    public static final String PUBLIC_SUFFIX = "/public";
    public static final String REQUESTS_SUFFIX = "/requests";
    public static final String RECEIVED_SUFFIX = "/received";
    public static final String SENT_SUFFIX = "/sent";
    public static final String FOLLOWING_SUFFIX = "/following";
    public static final String FOLLOWERS_SUFFIX = "/followers";

    // Common path segments
    public static final String USERNAME_SEGMENT = "/username/{username}";
    public static final String USERS_SEGMENT = "/users/{userId}";

    // ============================================================
    // Base API version
    // ============================================================

    public static final String API_V1 = "/api/1";

    // ============================================================
    // Auth endpoints
    // ============================================================

    public static final String AUTH_PATH = API_V1 + "/auth";
    public static final String LOGIN_ENDPOINT = "/login";
    public static final String REGISTER_ENDPOINT = "/register";
    public static final String LOGOUT_ENDPOINT = "/logout";
    public static final String REFRESH_ENDPOINT = "/refresh";
    public static final String PASSWORD_RESET_ENDPOINT = "/password/reset";
    public static final String PASSWORD_CHANGE_ENDPOINT = "/password/change";

    // ============================================================
    // User endpoints
    // ============================================================

    public static final String USERS_PATH = API_V1 + "/users";
    public static final String USERS_ME_PATH = USERS_PATH + ME_SUFFIX;
    public static final String USER_BY_ID_ENDPOINT = UUID_PATH_VARIABLE;
    public static final String USERNAME_ENDPOINT = USERNAME_SEGMENT;
    public static final String USERNAME_PATH = USERS_PATH + "/username/";

    // ============================================================
    // Friends endpoints
    // ============================================================

    public static final String FRIENDS_PATH = USERS_PATH + "/friends";

    // Friend requests
    public static final String FRIEND_REQUESTS_PATH = FRIENDS_PATH + REQUESTS_SUFFIX;
    public static final String FRIEND_REQUESTS_RECEIVED_PATH =
            FRIEND_REQUESTS_PATH + RECEIVED_SUFFIX;
    public static final String FRIEND_REQUESTS_SENT_PATH = FRIEND_REQUESTS_PATH + SENT_SUFFIX;
    public static final String FRIEND_REQUESTS_RECEIVED_ENDPOINT = RECEIVED_SUFFIX;
    public static final String FRIEND_REQUESTS_SENT_ENDPOINT = SENT_SUFFIX;
    public static final String FRIEND_REQUEST_ACCEPT_ENDPOINT = "/{requestId}/accept";
    public static final String FRIEND_REQUEST_DECLINE_ENDPOINT = "/{requestId}/decline";

    // ============================================================
    // Follows endpoints
    // ============================================================

    public static final String FOLLOWS_PATH = USERS_PATH + "/follows";
    public static final String FOLLOWS_FOLLOWING_PATH = FOLLOWS_PATH + FOLLOWING_SUFFIX;
    public static final String FOLLOWS_FOLLOWERS_PATH = FOLLOWS_PATH + FOLLOWERS_SUFFIX;
    public static final String FOLLOWS_FOLLOWING_ENDPOINT = FOLLOWING_SUFFIX;
    public static final String FOLLOWS_FOLLOWERS_ENDPOINT = FOLLOWERS_SUFFIX;
    public static final String FOLLOW_BY_ID_ENDPOINT = "/{followedId}";

    // ============================================================
    // Trip endpoints
    // ============================================================

    public static final String TRIPS_PATH = API_V1 + "/trips";
    public static final String TRIPS_ME_PATH = TRIPS_PATH + ME_SUFFIX;
    public static final String TRIPS_PUBLIC_ENDPOINT = PUBLIC_SUFFIX;
    public static final String TRIPS_BY_USER_ENDPOINT = USERS_SEGMENT;
    public static final String TRIP_BY_ID_ENDPOINT = UUID_PATH_VARIABLE;
    public static final String TRIP_VISIBILITY_ENDPOINT = "/{id}/visibility";
    public static final String TRIP_STATUS_ENDPOINT = "/{id}/status";
    public static final String TRIP_UPDATES_ENDPOINT = "/{tripId}/updates";
    public static final String TRIP_COMMENTS_ENDPOINT = "/{tripId}/comments";

    // ============================================================
    // Trip Plan endpoints
    // ============================================================

    public static final String TRIP_PLANS_PATH = TRIPS_PATH + "/plans";
    public static final String TRIP_PLANS_ME_PATH = TRIP_PLANS_PATH + ME_SUFFIX;
    public static final String TRIP_PLAN_BY_ID_ENDPOINT = "/{planId}";

    // ============================================================
    // Comment endpoints
    // ============================================================

    public static final String COMMENTS_PATH = API_V1 + "/comments";
    public static final String COMMENT_REACTIONS_ENDPOINT = "/comments/{commentId}/reactions";
}
