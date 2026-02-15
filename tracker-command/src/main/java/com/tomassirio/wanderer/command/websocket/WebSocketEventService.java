package com.tomassirio.wanderer.command.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for broadcasting WebSocket events to subscribers.
 *
 * <p>This service provides a simple, functional interface for WebSocket handlers to broadcast
 * events to trip or user topics. Each handler is responsible for creating its own payload and
 * calling the appropriate broadcast method.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketEventService {

    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    /**
     * Broadcasts an event to all subscribers of a trip topic.
     *
     * @param tripId the trip ID to broadcast to
     * @param eventType the type of event (use constants from {@link WebSocketEventType})
     * @param payload the event payload
     */
    public void broadcastToTrip(UUID tripId, String eventType, Object payload) {
        String topic = "/topic/trips/" + tripId;
        WebSocketEvent event = WebSocketEvent.create(eventType, tripId, payload);
        broadcast(topic, event, "trip", tripId);
    }

    /**
     * Broadcasts an event to all subscribers of a user topic.
     *
     * @param userId the user ID to broadcast to
     * @param eventType the type of event (use constants from {@link WebSocketEventType})
     * @param payload the event payload
     */
    public void broadcastToUser(UUID userId, String eventType, Object payload) {
        String topic = "/topic/users/" + userId;
        WebSocketEvent event = WebSocketEvent.create(eventType, userId, payload);
        broadcast(topic, event, "user", userId);
    }

    private void broadcast(String topic, WebSocketEvent event, String targetType, UUID targetId) {
        try {
            String message = objectMapper.writeValueAsString(event);
            sessionManager.broadcast(topic, message);
            log.info(
                    "Broadcast {} event to {} {} ({} subscribers)",
                    event.getType(),
                    targetType,
                    targetId,
                    sessionManager.getSubscribersCount(topic));
        } catch (JsonProcessingException e) {
            log.error("Error serializing WebSocket event: {}", event.getType(), e);
        }
    }
}
