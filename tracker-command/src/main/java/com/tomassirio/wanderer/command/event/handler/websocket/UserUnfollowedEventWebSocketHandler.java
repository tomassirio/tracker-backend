package com.tomassirio.wanderer.command.event.handler.websocket;

import com.tomassirio.wanderer.command.event.EventHandler;
import com.tomassirio.wanderer.command.event.UserUnfollowedEvent;
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
        webSocketEventService.broadcastUserUnfollowed(payload);
    }
}
