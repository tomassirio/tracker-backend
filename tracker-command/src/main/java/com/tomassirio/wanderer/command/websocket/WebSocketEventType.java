package com.tomassirio.wanderer.command.websocket;

/**
 * Constants for WebSocket event types used throughout the application.
 *
 * <p>These constants are used when broadcasting events to WebSocket subscribers to ensure
 * consistent event naming across the codebase.
 */
public final class WebSocketEventType {

    private WebSocketEventType() {
        // Prevent instantiation
    }

    // Trip lifecycle events
    public static final String TRIP_CREATED = "TRIP_CREATED";
    public static final String TRIP_DELETED = "TRIP_DELETED";
    public static final String TRIP_METADATA_UPDATED = "TRIP_METADATA_UPDATED";
    public static final String TRIP_STATUS_CHANGED = "TRIP_STATUS_CHANGED";
    public static final String TRIP_VISIBILITY_CHANGED = "TRIP_VISIBILITY_CHANGED";
    public static final String TRIP_UPDATED = "TRIP_UPDATED";

    // Comment events
    public static final String COMMENT_ADDED = "COMMENT_ADDED";
    public static final String COMMENT_REACTION_ADDED = "COMMENT_REACTION_ADDED";
    public static final String COMMENT_REACTION_REMOVED = "COMMENT_REACTION_REMOVED";

    // Friend request events
    public static final String FRIEND_REQUEST_SENT = "FRIEND_REQUEST_SENT";
    public static final String FRIEND_REQUEST_RECEIVED = "FRIEND_REQUEST_RECEIVED";
    public static final String FRIEND_REQUEST_ACCEPTED = "FRIEND_REQUEST_ACCEPTED";
    public static final String FRIEND_REQUEST_DECLINED = "FRIEND_REQUEST_DECLINED";

    // User follow events
    public static final String USER_FOLLOWED = "USER_FOLLOWED";
    public static final String USER_UNFOLLOWED = "USER_UNFOLLOWED";
}
