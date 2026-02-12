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
