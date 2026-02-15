package com.tomassirio.wanderer.command.handler.websocket;

import com.tomassirio.wanderer.command.event.FriendRequestDeclinedEvent;
import com.tomassirio.wanderer.command.handler.EventHandler;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import com.tomassirio.wanderer.command.websocket.WebSocketEventType;
import com.tomassirio.wanderer.command.websocket.payload.FriendRequestPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * WebSocket event handler for broadcasting friend request declined events.
 *
 * <p>This handler broadcasts FriendRequestDeclinedEvent to WebSocket clients after the transaction
 * commits.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(2) // Execute after persistence
public class FriendRequestDeclinedEventWebSocketHandler
        implements EventHandler<FriendRequestDeclinedEvent> {

    private final WebSocketEventService webSocketEventService;

    @Override
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(FriendRequestDeclinedEvent event) {
        log.debug("Handling FriendRequestDeclinedEvent: {}", event.getRequestId());
        FriendRequestPayload payload =
                FriendRequestPayload.builder()
                        .requestId(event.getRequestId())
                        .senderId(event.getSenderId())
                        .receiverId(event.getReceiverId())
                        .status("DECLINED")
                        .build();
        // Notify the original sender that their request was declined
        webSocketEventService.broadcastToUser(
                event.getSenderId(), WebSocketEventType.FRIEND_REQUEST_DECLINED, payload);
    }
}
