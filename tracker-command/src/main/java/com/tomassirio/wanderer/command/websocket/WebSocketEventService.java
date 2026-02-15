package com.tomassirio.wanderer.command.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketEventService {

    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    public void broadcastTripStatusChanged(UUID tripId, String newStatus, String previousStatus) {
        TripStatusChangedPayload payload =
                TripStatusChangedPayload.builder()
                        .tripId(tripId)
                        .newStatus(newStatus)
                        .previousStatus(previousStatus)
                        .build();

        WebSocketEvent event = WebSocketEvent.create("TRIP_STATUS_CHANGED", tripId, payload);
        broadcastToTrip(tripId, event);
    }

    public void broadcastTripUpdated(TripUpdatedPayload payload) {
        WebSocketEvent event = WebSocketEvent.create("TRIP_UPDATED", payload.getTripId(), payload);
        broadcastToTrip(payload.getTripId(), event);
    }

    public void broadcastCommentAdded(CommentAddedPayload payload) {
        WebSocketEvent event = WebSocketEvent.create("COMMENT_ADDED", payload.getTripId(), payload);
        broadcastToTrip(payload.getTripId(), event);
    }

    public void broadcastCommentReactionAdded(CommentReactionPayload payload) {
        WebSocketEvent event =
                WebSocketEvent.create("COMMENT_REACTION_ADDED", payload.getTripId(), payload);
        broadcastToTrip(payload.getTripId(), event);
    }

    public void broadcastCommentReactionRemoved(CommentReactionPayload payload) {
        WebSocketEvent event =
                WebSocketEvent.create("COMMENT_REACTION_REMOVED", payload.getTripId(), payload);
        broadcastToTrip(payload.getTripId(), event);
    }

    public void broadcastFriendRequestSent(FriendRequestPayload payload) {
        WebSocketEvent senderEvent =
                WebSocketEvent.create("FRIEND_REQUEST_SENT", payload.getSenderId(), payload);
        broadcastToUser(payload.getSenderId(), senderEvent);

        WebSocketEvent receiverEvent =
                WebSocketEvent.create("FRIEND_REQUEST_RECEIVED", payload.getReceiverId(), payload);
        broadcastToUser(payload.getReceiverId(), receiverEvent);
    }

    public void broadcastFriendRequestAccepted(FriendRequestPayload payload) {
        WebSocketEvent senderEvent =
                WebSocketEvent.create("FRIEND_REQUEST_ACCEPTED", payload.getSenderId(), payload);
        broadcastToUser(payload.getSenderId(), senderEvent);

        WebSocketEvent receiverEvent =
                WebSocketEvent.create("FRIEND_REQUEST_ACCEPTED", payload.getReceiverId(), payload);
        broadcastToUser(payload.getReceiverId(), receiverEvent);
    }

    public void broadcastFriendRequestDeclined(FriendRequestPayload payload) {
        WebSocketEvent senderEvent =
                WebSocketEvent.create("FRIEND_REQUEST_DECLINED", payload.getSenderId(), payload);
        broadcastToUser(payload.getSenderId(), senderEvent);

        WebSocketEvent receiverEvent =
                WebSocketEvent.create("FRIEND_REQUEST_DECLINED", payload.getReceiverId(), payload);
        broadcastToUser(payload.getReceiverId(), receiverEvent);
    }

    public void broadcastUserFollowed(UserFollowPayload payload) {
        WebSocketEvent event =
                WebSocketEvent.create("USER_FOLLOWED", payload.getFollowedId(), payload);
        broadcastToUser(payload.getFollowedId(), event);
    }

    public void broadcastUserUnfollowed(UserFollowPayload payload) {
        WebSocketEvent event =
                WebSocketEvent.create("USER_UNFOLLOWED", payload.getFollowedId(), payload);
        broadcastToUser(payload.getFollowedId(), event);
    }

    public void broadcastTripCreated(TripLifecyclePayload payload) {
        WebSocketEvent event = WebSocketEvent.create("TRIP_CREATED", payload.getTripId(), payload);
        broadcastToTrip(payload.getTripId(), event);
    }

    public void broadcastTripMetadataUpdated(TripLifecyclePayload payload) {
        WebSocketEvent event =
                WebSocketEvent.create("TRIP_METADATA_UPDATED", payload.getTripId(), payload);
        broadcastToTrip(payload.getTripId(), event);
    }

    public void broadcastTripDeleted(TripLifecyclePayload payload) {
        WebSocketEvent event = WebSocketEvent.create("TRIP_DELETED", payload.getTripId(), payload);
        broadcastToTrip(payload.getTripId(), event);
    }

    public void broadcastTripVisibilityChanged(
            UUID tripId, String newVisibility, String previousVisibility) {
        TripVisibilityChangedPayload payload =
                TripVisibilityChangedPayload.builder()
                        .tripId(tripId)
                        .newVisibility(newVisibility)
                        .previousVisibility(previousVisibility)
                        .build();

        WebSocketEvent event = WebSocketEvent.create("TRIP_VISIBILITY_CHANGED", tripId, payload);
        broadcastToTrip(tripId, event);
    }

    private void broadcastToUser(UUID userId, WebSocketEvent event) {
        String topic = "/topic/users/" + userId;

        try {
            String message = objectMapper.writeValueAsString(event);
            sessionManager.broadcast(topic, message);
            log.info(
                    "Broadcast {} event to user {} ({} subscribers)",
                    event.getType(),
                    userId,
                    sessionManager.getSubscribersCount(topic));
        } catch (JsonProcessingException e) {
            log.error("Error serializing WebSocket event", e);
        }
    }

    private void broadcastToTrip(UUID tripId, WebSocketEvent event) {
        String topic = "/topic/trips/" + tripId;

        try {
            String message = objectMapper.writeValueAsString(event);
            sessionManager.broadcast(topic, message);
            log.info(
                    "Broadcast {} event to trip {} ({} subscribers)",
                    event.getType(),
                    tripId,
                    sessionManager.getSubscribersCount(topic));
        } catch (JsonProcessingException e) {
            log.error("Error serializing WebSocket event", e);
        }
    }
}
