package com.tomassirio.wanderer.command.event;

import com.tomassirio.wanderer.command.websocket.CommentAddedPayload;
import com.tomassirio.wanderer.command.websocket.CommentReactionPayload;
import com.tomassirio.wanderer.command.websocket.TripUpdatedPayload;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event listener that handles domain events and broadcasts them via WebSocket.
 *
 * <p>This decouples the business logic in services from WebSocket broadcasting concerns. Services
 * publish domain events, and this listener asynchronously broadcasts them to WebSocket clients.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final WebSocketEventService webSocketEventService;

    @Async
    @EventListener
    public void handleTripStatusChanged(TripStatusChangedEvent event) {
        log.debug(
                "Handling TripStatusChangedEvent for trip: {}, status: {}",
                event.getTripId(),
                event.getNewStatus());
        webSocketEventService.broadcastTripStatusChanged(
                event.getTripId(), event.getNewStatus(), event.getPreviousStatus());
    }

    @Async
    @EventListener
    public void handleTripUpdated(TripUpdatedEvent event) {
        log.debug("Handling TripUpdatedEvent for trip: {}", event.getTripId());
        TripUpdatedPayload payload =
                TripUpdatedPayload.builder()
                        .tripId(event.getTripId())
                        .latitude(event.getLatitude())
                        .longitude(event.getLongitude())
                        .batteryLevel(event.getBatteryLevel())
                        .message(event.getMessage())
                        .build();
        webSocketEventService.broadcastTripUpdated(payload);
    }

    @Async
    @EventListener
    public void handleCommentAdded(CommentAddedEvent event) {
        log.debug(
                "Handling CommentAddedEvent for trip: {}, comment: {}",
                event.getTripId(),
                event.getCommentId());
        CommentAddedPayload payload =
                CommentAddedPayload.create(
                        event.getTripId(),
                        event.getCommentId(),
                        event.getUserId(),
                        event.getUsername(),
                        event.getMessage(),
                        event.getParentCommentId());
        webSocketEventService.broadcastCommentAdded(payload);
    }

    @Async
    @EventListener
    public void handleCommentReaction(CommentReactionEvent event) {
        log.debug(
                "Handling CommentReactionEvent for trip: {}, comment: {}, added: {}",
                event.getTripId(),
                event.getCommentId(),
                event.isAdded());
        CommentReactionPayload payload =
                CommentReactionPayload.builder()
                        .tripId(event.getTripId())
                        .commentId(event.getCommentId())
                        .reactionType(event.getReactionType())
                        .userId(event.getUserId())
                        .build();

        if (event.isAdded()) {
            webSocketEventService.broadcastCommentReactionAdded(payload);
        } else {
            webSocketEventService.broadcastCommentReactionRemoved(payload);
        }
    }
}
