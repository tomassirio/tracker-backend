package com.tomassirio.wanderer.command.handler.websocket;

import com.tomassirio.wanderer.command.event.UserUnfollowedEvent;
import com.tomassirio.wanderer.command.handler.EventHandler;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import com.tomassirio.wanderer.command.websocket.WebSocketEventType;
import com.tomassirio.wanderer.command.websocket.payload.UserFollowPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * WebSocket event handler for broadcasting user unfollowed events.
 *
 * <p>This handler broadcasts UserUnfollowedEvent to WebSocket clients after the transaction
 * commits.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(2) // Execute after persistence
public class UserUnfollowedEventWebSocketHandler implements EventHandler<UserUnfollowedEvent> {

    private final WebSocketEventService webSocketEventService;

    @Override
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(UserUnfollowedEvent event) {
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
        // Notify the unfollowed user
        webSocketEventService.broadcastToUser(
                event.getFollowedId(), WebSocketEventType.USER_UNFOLLOWED, payload);
    }
}
