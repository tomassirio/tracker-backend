package com.tomassirio.wanderer.command.handler;

import com.tomassirio.wanderer.command.event.FriendRequestSentEvent;
import com.tomassirio.wanderer.commons.domain.FriendRequest;
import com.tomassirio.wanderer.commons.domain.FriendRequestStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event handler for persisting friend request sent events to the database.
 *
 * <p>This handler implements the CQRS write side by handling FriendRequestSentEvent and persisting
 * the friend request to the database. WebSocket broadcasting is handled centrally by {@link
 * com.tomassirio.wanderer.command.websocket.BroadcastableEventListener}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FriendRequestSentEventHandler implements EventHandler<FriendRequestSentEvent> {

    private final EntityManager entityManager;

    @Override
    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handle(FriendRequestSentEvent event) {
        log.debug("Persisting FriendRequestSentEvent for request: {}", event.getRequestId());

        FriendRequest request =
                FriendRequest.builder()
                        .id(event.getRequestId())
                        .senderId(event.getSenderId())
                        .receiverId(event.getReceiverId())
                        .status(FriendRequestStatus.valueOf(event.getStatus()))
                        .createdAt(event.getCreatedAt())
                        .build();

        entityManager.persist(request);
        log.info("Friend request created and persisted: {}", event.getRequestId());
    }
}
