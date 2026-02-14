package com.tomassirio.wanderer.command.event.handler.websocket;

import com.tomassirio.wanderer.command.event.EventHandler;
import com.tomassirio.wanderer.command.event.FriendRequestSentEvent;
import com.tomassirio.wanderer.command.websocket.FriendRequestPayload;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * WebSocket event handler for broadcasting friend request sent events.
 *
 * <p>This handler broadcasts FriendRequestSentEvent to WebSocket clients after the transaction
 * commits.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(2) // Execute after persistence
public class FriendRequestSentEventWebSocketHandler
        implements EventHandler<FriendRequestSentEvent> {

    private final WebSocketEventService webSocketEventService;

    @Override
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(FriendRequestSentEvent event) {
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
}
