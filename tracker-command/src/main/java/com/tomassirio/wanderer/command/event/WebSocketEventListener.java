package com.tomassirio.wanderer.command.event;

import com.tomassirio.wanderer.command.websocket.CommentAddedPayload;
import com.tomassirio.wanderer.command.websocket.CommentReactionPayload;
import com.tomassirio.wanderer.command.websocket.FriendRequestPayload;
import com.tomassirio.wanderer.command.websocket.TripLifecyclePayload;
import com.tomassirio.wanderer.command.websocket.TripUpdatedPayload;
import com.tomassirio.wanderer.command.websocket.UserFollowPayload;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event listener that handles domain events and broadcasts them via WebSocket.
 *
 * <p>This decouples the business logic in services from WebSocket broadcasting concerns. Services
 * publish domain events, persistence handlers write to DB, and this listener broadcasts to
 * WebSocket clients after the transaction commits.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(2) // Execute after persistence
public class WebSocketEventListener {

    private final WebSocketEventService webSocketEventService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTripStatusChanged(TripStatusChangedEvent event) {
        log.debug(
                "Handling TripStatusChangedEvent for trip: {}, status: {}",
                event.getTripId(),
                event.getNewStatus());
        webSocketEventService.broadcastTripStatusChanged(
                event.getTripId(), event.getNewStatus(), event.getPreviousStatus());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
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
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
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
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
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

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFriendRequestSent(FriendRequestSentEvent event) {
        log.debug(
                "Handling FriendRequestSentEvent: {} from {} to {}",
                event.getRequestId(),
                event.getSenderId(),
                event.getReceiverId());
        FriendRequestPayload payload =
                FriendRequestPayload.builder()
                        .requestId(event.getRequestId())
                        .senderId(event.getSenderId())
                        .receiverId(event.getReceiverId())
                        .status(event.getStatus())
                        .build();
        webSocketEventService.broadcastFriendRequestSent(payload);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFriendRequestAccepted(FriendRequestAcceptedEvent event) {
        log.debug("Handling FriendRequestAcceptedEvent: {}", event.getRequestId());
        FriendRequestPayload payload =
                FriendRequestPayload.builder()
                        .requestId(event.getRequestId())
                        .senderId(event.getSenderId())
                        .receiverId(event.getReceiverId())
                        .status("ACCEPTED")
                        .build();
        webSocketEventService.broadcastFriendRequestAccepted(payload);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFriendRequestDeclined(FriendRequestDeclinedEvent event) {
        log.debug("Handling FriendRequestDeclinedEvent: {}", event.getRequestId());
        FriendRequestPayload payload =
                FriendRequestPayload.builder()
                        .requestId(event.getRequestId())
                        .senderId(event.getSenderId())
                        .receiverId(event.getReceiverId())
                        .status("DECLINED")
                        .build();
        webSocketEventService.broadcastFriendRequestDeclined(payload);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserFollowed(UserFollowedEvent event) {
        log.debug(
                "Handling UserFollowedEvent: {} followed {}",
                event.getFollowerId(),
                event.getFollowedId());
        UserFollowPayload payload =
                UserFollowPayload.builder()
                        .followId(event.getFollowId())
                        .followerId(event.getFollowerId())
                        .followedId(event.getFollowedId())
                        .build();
        webSocketEventService.broadcastUserFollowed(payload);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserUnfollowed(UserUnfollowedEvent event) {
        log.debug(
                "Handling UserUnfollowedEvent: {} unfollowed {}",
                event.getFollowerId(),
                event.getFollowedId());
        UserFollowPayload payload =
                UserFollowPayload.builder()
                        .followId(null)
                        .followerId(event.getFollowerId())
                        .followedId(event.getFollowedId())
                        .build();
        webSocketEventService.broadcastUserUnfollowed(payload);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTripCreated(TripCreatedEvent event) {
        log.debug("Handling TripCreatedEvent: {}", event.getTripId());
        TripLifecyclePayload payload =
                TripLifecyclePayload.builder()
                        .tripId(event.getTripId())
                        .tripName(event.getTripName())
                        .ownerId(event.getOwnerId())
                        .visibility(event.getVisibility())
                        .build();
        webSocketEventService.broadcastTripCreated(payload);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTripMetadataUpdated(TripMetadataUpdatedEvent event) {
        log.debug("Handling TripMetadataUpdatedEvent: {}", event.getTripId());
        TripLifecyclePayload payload =
                TripLifecyclePayload.builder()
                        .tripId(event.getTripId())
                        .tripName(event.getTripName())
                        .ownerId(null)
                        .visibility(event.getVisibility())
                        .build();
        webSocketEventService.broadcastTripMetadataUpdated(payload);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTripDeleted(TripDeletedEvent event) {
        log.debug("Handling TripDeletedEvent: {}", event.getTripId());
        TripLifecyclePayload payload =
                TripLifecyclePayload.builder()
                        .tripId(event.getTripId())
                        .tripName(null)
                        .ownerId(event.getOwnerId())
                        .visibility(null)
                        .build();
        webSocketEventService.broadcastTripDeleted(payload);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTripVisibilityChanged(TripVisibilityChangedEvent event) {
        log.debug("Handling TripVisibilityChangedEvent: {}", event.getTripId());
        webSocketEventService.broadcastTripVisibilityChanged(
                event.getTripId(), event.getNewVisibility(), event.getPreviousVisibility());
    }
}
